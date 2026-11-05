package com.example.ragdemo.system1;

import org.springaicommunity.typesafe.question.Noul;
import org.springaicommunity.typesafe.question.Question;
import org.springaicommunity.typesafe.question.SystemOneRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Rewrites the questions {@code JevDocumentFilter} / {@code JevDocumentReranker} ask into the form Laya reads well,
 * keeping their <b>names</b> — so the library's policy ({@code classify}) and its rerank read the answers unchanged.
 *
 * <p>The library sends {@code state = {"query": …, "passage": …}} and questions that point at those fields (the
 * injection one as a structured map with when-true / when-false hints). Laya, a small model, answers that form
 * noisily: measured on the Space, a plain aerator manual scored injection 0.61–0.99. With the passage as plain text
 * and the question written into the instruction — the wording this app used before — the same manual scores 0.00 and
 * a real injection 0.99. A request that is not of that shape (e.g. the answer checks) is passed on unchanged.
 */
final class LayaQuestions {

    private LayaQuestions() {
    }

    static SystemOneRequest forLaya(SystemOneRequest request) {
        if (!(request.state() != null && request.state().value() instanceof Map<?, ?> state)) return request;
        if (!(state.get("query") instanceof String query && state.get("passage") instanceof String passage)) {
            return request;
        }
        Map<String, Question> questions = new LinkedHashMap<>();
        for (Map.Entry<String, Question> q : request.questions().entrySet()) {
            String instruction = instruction(q.getKey(), query);
            if (instruction == null) return request;   // a question we do not know: leave the request as it is
            questions.put(q.getKey(), Noul.of(instruction));
        }
        return SystemOneRequest.builder().state(passage).model(request.model()).questions(questions).build();
    }

    /** Laya's wording for one of the library's questions, or null if it is not one of them. */
    static String instruction(String name, String query) {
        return switch (name) {
            case "is_relevant" -> "Is this passage about the question: " + query;
            case "contains_answer_evidence", "answers_query" ->
                    "Does this passage contain the answer to the question: " + query;
            // measured: 0.02 for a manual that answers the question, 0.64 when the question's premise is false
            case "contradicts_query_premise" -> "Does this passage contradict the question: " + query;
            case "contains_prompt_injection" -> LayaClient.INJECTION_QUESTION;
            default -> null;
        };
    }
}
