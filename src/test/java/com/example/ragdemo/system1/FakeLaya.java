package com.example.ragdemo.system1;

import org.springaicommunity.typesafe.RetryPolicy;
import org.springaicommunity.typesafe.TypeSafeClient;
import org.springaicommunity.typesafe.api.TypeSafeApi;
import org.springaicommunity.typesafe.exception.TypeSafeApiException;
import org.springaicommunity.typesafe.question.SystemOneRequest;
import org.springaicommunity.typesafe.response.Answer;
import org.springaicommunity.typesafe.response.NoulAnswer;
import org.springaicommunity.typesafe.response.SystemOneResponse;
import org.springframework.http.HttpHeaders;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * A Laya without HTTP for the tests: a {@link TypeSafeClient} that answers every question by the passage text, so the
 * real {@code JevDocumentFilter} / {@code JevDocumentReranker} run against it. An unknown passage → 503 (waking up).
 */
public class FakeLaya extends TypeSafeClient {

    /** Laya's scores for one passage; the rerank question ({@code answers_query}) gets {@code answers}. */
    public record Scores(double relevant, double answers, double injection, double contradicts) {

        public Scores(double relevant, double answers, double injection) {
            this(relevant, answers, injection, 0.0);
        }
    }

    private static final TypeSafeApi UNUSED = TypeSafeApi.builder().baseUrl("http://localhost").apiKey("unused").build();

    private final Function<String, Scores> byPassage;
    /** Every request this Laya answered or refused, in order. */
    public final List<SystemOneRequest> requests = new CopyOnWriteArrayList<>();

    public FakeLaya(Function<String, Scores> byPassage) {
        super(UNUSED, "laya", RetryPolicy.noRetry());
        this.byPassage = byPassage;
    }

    @Override
    public SystemOneResponse systemOne(SystemOneRequest request) {
        requests.add(request);
        Scores s = byPassage.apply(RecordingTypeSafeClient.passage(request.state()));
        if (s == null) throw status(503);
        Map<String, Answer> answers = new LinkedHashMap<>();
        for (String name : request.questions().keySet()) {
            answers.put(name, new NoulAnswer(switch (name) {
                case "is_relevant", "relevant" -> s.relevant();
                case "contains_answer_evidence", "answers_query", "answers" -> s.answers();
                case "contains_prompt_injection", "injection" -> s.injection();
                case "contradicts_query_premise" -> s.contradicts();
                default -> 0.0;
            }));
        }
        return new SystemOneResponse("laya", answers, null);
    }

    /** What the TypeSafe SDK throws for an HTTP status from Laya. */
    public static TypeSafeApiException status(int status) {
        return new TypeSafeApiException("HTTP " + status, status, "{}", new HttpHeaders(), "POST /v1/systemone");
    }

    /** A LayaClient on a fake Laya that scores by passage text; {@code failure} (if set) is thrown on every call. */
    public static LayaClient laya(Map<String, Scores> byText, RuntimeException failure) {
        return client(passage -> {
            if (failure != null) throw failure;
            return byText.get(passage);
        });
    }

    public static LayaClient client(Function<String, Scores> byPassage) {
        return client(new FakeLaya(byPassage));
    }

    public static LayaClient client(TypeSafeClient laya) {
        return new LayaClient(null) {
            @Override
            public TypeSafeClient typeSafe() {
                return laya;
            }
        };
    }
}
