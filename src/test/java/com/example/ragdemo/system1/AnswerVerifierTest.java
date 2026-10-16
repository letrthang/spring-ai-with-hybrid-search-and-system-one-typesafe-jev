package com.example.ragdemo.system1;

import com.example.ragdemo.rag.SystemDownException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.ragdemo.rag.ServiceStatus.Service.SYSTEM1;
import static org.assertj.core.api.Assertions.assertThat;

/** The gates an answer must pass before it is learned — Laya faked with fixed scores. */
class AnswerVerifierTest {

    final List<String> states = new ArrayList<>();

    AnswerVerifier verifier(double answers, double injection, double grounded) {
        return new AnswerVerifier(new LayaClient(null) {
            @Override
            public Map<String, Double> ask(String state, Map<String, String> questions) {
                states.add(state);
                return questions.containsKey("grounded") ? Map.of("grounded", grounded)
                        : Map.of("answers", answers, "injection", injection);
            }
        });
    }

    static final List<String> PASSAGES = List.of("PCR copies DNA.", "Gel electrophoresis sorts DNA by size.");

    @Test
    void aGoodGroundedAnswerIsKept_andTheCitedPassageIsChecked() {
        var v = verifier(0.9, 0.01, 0.8).verify("What does PCR do?", "PCR copies a piece of DNA [1].", PASSAGES);
        assertThat(v.keep()).isTrue();
        assertThat(v.reason().key()).isEqualTo("learn.reason.verified");
        assertThat(states).containsExactly("PCR copies a piece of DNA [1].", "PCR copies DNA.");   // answer, then [1]
    }

    @Test
    void eachGateCanStopIt() {
        assertThat(verifier(0.9, 0.01, 0.8).verify("q", "I don't know.", PASSAGES).reason().key())
                .isEqualTo("learn.reason.dontKnow");
        assertThat(verifier(0.9, 0.01, 0.8).verify("q", "PCR copies DNA.", PASSAGES).reason().key())
                .isEqualTo("learn.reason.noCitation");                       // no [n]
        assertThat(verifier(0.9, 0.01, 0.8).verify("q", "See [7].", PASSAGES).reason().key())
                .isEqualTo("learn.reason.noCitation");                       // [7] is not a passage
        assertThat(verifier(0.5, 0.01, 0.8).verify("q", "Text [1].", PASSAGES).reason().key())
                .isEqualTo("learn.reason.notAnswer");                        // answers < 0.70
        assertThat(verifier(0.9, 0.5, 0.8).verify("q", "Text [1].", PASSAGES).reason().key())
                .isEqualTo("learn.reason.injection");                        // injection ≥ 0.30
        assertThat(verifier(0.9, 0.01, 0.3).verify("q", "Text [1].", PASSAGES).reason().key())
                .isEqualTo("learn.reason.notGrounded");                      // grounded < 0.55
    }

    @Test
    void aBusySystemOneMeansNotLearned_neverAnError() {
        var busy = new AnswerVerifier(new LayaClient(null) {
            @Override
            public Map<String, Double> ask(String state, Map<String, String> questions) {
                throw new SystemDownException(SYSTEM1, "status.system1.busy");
            }
        });
        var v = busy.verify("q", "Text [1].", PASSAGES);
        assertThat(v.keep()).isFalse();
        assertThat(v.reason().key()).isEqualTo("learn.reason.system1Busy");
    }

    @Test
    void citationsAreTheRealPassageNumbersInOrder() {
        assertThat(AnswerVerifier.citations("A [2], B [1] and [2] again, [9] none.", 3)).containsExactly(2, 1);
    }
}
