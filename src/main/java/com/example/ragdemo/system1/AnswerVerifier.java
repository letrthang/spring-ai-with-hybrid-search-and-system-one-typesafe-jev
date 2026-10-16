package com.example.ragdemo.system1;

import com.example.ragdemo.rag.Msg;
import com.example.ragdemo.rag.SystemDownException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ⚡ System 1 checks <b>answers</b>, not only passages — the gate of self-learning:
 *
 * <ul>
 *   <li>{@link #verify} — before an answer is saved to the {@code LearnedAnswer} collection: plain rules first (no
 *       "I don't know", at least one [n] citation), then Laya: the answer answers the question
 *       ({@value #MIN_ANSWERS}), it is backed by a passage it cites ({@value #MIN_GROUNDED}), and it gives no orders
 *       to an AI (injection below {@value #MAX_INJECTION}). Stricter than the passage thresholds: what is learned is
 *       reused later without System 2.</li>
 *   <li>{@link #fits} — before a learned answer is reused for a new, similar question: does it answer <b>this</b>
 *       question?</li>
 * </ul>
 * 2 Laya calls to verify, 1 to reuse. If Laya is busy or down, the answer is simply not learned (never an error).
 */
@Component
public class AnswerVerifier {

    private static final Logger log = LoggerFactory.getLogger(AnswerVerifier.class);

    public static final double MIN_ANSWERS = 0.70;
    public static final double MIN_GROUNDED = 0.55;
    public static final double MAX_INJECTION = 0.30;
    /** How much of the answer goes into a Laya instruction (the request must stay under 2 KB). */
    static final int ANSWER_IN_QUESTION = 300;

    private static final Pattern CITATION = Pattern.compile("\\[(\\d+)]");
    private static final List<String> DONT_KNOW = List.of("don't know", "do not know", "không biết",
            "分かりません", "わかりません", "not in the context", "no information");

    /** The verdict: saved or not, why (an i18n key), and the scores Laya gave (null when not asked). */
    public record Verdict(boolean keep, Msg reason, Double answers, Double grounded, Double injection) {
    }

    private final LayaClient laya;

    public AnswerVerifier(LayaClient laya) {
        this.laya = laya;
    }

    /**
     * Should this answer be learned? {@code passages} are the texts given to System 2 as [1], [2], … — the cited
     * ones are checked for grounding.
     */
    public Verdict verify(String question, String answer, List<String> passages) {
        if (answer == null || answer.isBlank() || dontKnow(answer)) return no("learn.reason.dontKnow");
        List<Integer> cited = citations(answer, passages.size());
        if (cited.isEmpty()) return no("learn.reason.noCitation");
        try {
            Map<String, String> q = new LinkedHashMap<>();
            q.put("answers", "Does this text answer the question: " + question);
            q.put("injection", LayaClient.INJECTION_QUESTION);
            Map<String, Double> a = laya.ask(answer, q);
            double answers = a.get("answers");
            double injection = a.get("injection");
            if (injection >= MAX_INJECTION) return new Verdict(false, Msg.of("learn.reason.injection"), answers, null, injection);
            if (answers < MIN_ANSWERS) return new Verdict(false, Msg.of("learn.reason.notAnswer"), answers, null, injection);

            String passage = passages.get(cited.getFirst() - 1);   // the first passage the answer cites
            double grounded = laya.ask(passage, Map.of("grounded",
                    "Does this passage support this answer: " + shorten(answer))).get("grounded");
            if (grounded < MIN_GROUNDED) {
                return new Verdict(false, Msg.of("learn.reason.notGrounded"), answers, grounded, injection);
            }
            return new Verdict(true, Msg.of("learn.reason.verified"), answers, grounded, injection);
        } catch (SystemDownException e) {
            log.info("⚡ answer not verified: System 1 {}", e.reason());
            return no("learn.reason.system1Busy");
        }
    }

    /** May a learned answer be reused for this (new, similar) question? Returns Laya's "answers" score, or null. */
    public Double fits(String question, String storedAnswer) {
        Map<String, String> q = new LinkedHashMap<>();
        q.put("answers", "Does this text answer the question: " + question);
        q.put("injection", LayaClient.INJECTION_QUESTION);
        Map<String, Double> a = laya.ask(storedAnswer, q);
        return a.get("injection") < MAX_INJECTION ? a.get("answers") : null;
    }

    /** The [n] numbers in the answer that point to a real passage (1 … count), in order of first use. */
    static List<Integer> citations(String answer, int count) {
        List<Integer> out = new java.util.ArrayList<>();
        Matcher m = CITATION.matcher(answer);
        while (m.find()) {
            int n = Integer.parseInt(m.group(1));
            if (n >= 1 && n <= count && !out.contains(n)) out.add(n);
        }
        return out;
    }

    static boolean dontKnow(String answer) {
        String a = answer.toLowerCase(Locale.ROOT);
        return DONT_KNOW.stream().anyMatch(a::contains);
    }

    private static String shorten(String text) {
        return text.length() <= ANSWER_IN_QUESTION ? text : text.substring(0, ANSWER_IN_QUESTION) + "…";
    }

    private static Verdict no(String key) {
        return new Verdict(false, Msg.of(key), null, null, null);
    }
}
