package com.example.ragdemo.rag;

import java.util.List;

/**
 * Everything one question produced: the answer, where it came from (📚 Sources), the timings (🔬 Trace) and every hop
 * (🔀 Data flow). If a required service failed, {@code failure} says which one and why, {@code text} is null, and the
 * trace and flow show how far the question got. Readable texts are {@link Msg}s, translated by the UI.
 *
 * <p>Self-learning: {@code reuse} is set when a learned answer was reused (no System 2 call); {@code learning} says
 * whether this new answer was verified by System 1 and saved (null when it was not tried, e.g. nothing kept).
 *
 * <p>🛠 Agentic mode: {@code agent} says how System 2 got there — its rounds and tool calls (null in the fixed
 * pipeline, and in answers saved before agentic mode existed).
 */
public record RagAnswer(String question, String text, List<Source> sources, List<TraceLine> trace,
                        List<FlowRecorder.FlowHop> flow, String model, long totalMs, Failure failure,
                        Reuse reuse, Learning learning, Agent agent) {

    /** 🛠 How many times System 2 was asked (rounds), how many tools it called, and each call, e.g. "search_passages: PCR". */
    public record Agent(int rounds, int toolCalls, List<String> calls) {
    }

    public boolean agentic() {
        return agent != null;
    }

    /** A learned answer was reused: for which earlier question, how close it is, and System 1's "answers" score. */
    public record Reuse(String learnedQuestion, double distance, double answers) {
    }

    /** Was this answer learned? Why (not) — an i18n key — and the System 1 scores of the check. */
    public record Learning(boolean saved, Msg reason, Double answers, Double grounded, Double injection) {
    }

    public boolean reused() {
        return reuse != null;
    }

    /** One passage found by the search, with how each step judged it. {@code citation} = its [n] in the prompt. */
    public record Source(String title, String source, Integer chunkNo, String text,
                         Integer bm25Rank, Integer vectorRank, Double rrfScore,
                         Double relevant, Double answers, Double injection, String verdict, Integer citation) {

        public boolean used() {
            return citation != null;
        }
    }

    /** One line of 🔬 Trace: which step (an i18n key, e.g. "trace.step.SYSTEM1") and what it did. */
    public record TraceLine(String step, Msg detail) {
    }

    /** Which required service failed, and why (never a key). */
    public record Failure(ServiceStatus.Service service, Msg reason) {
    }

    public boolean failed() {
        return failure != null;
    }

    /** No passage was kept, so System 2 was not asked and there is no generated answer. */
    public boolean nothingKept() {
        return failure == null && model == null;
    }

    public long usedCount() {
        return sources.stream().filter(Source::used).count();
    }
}
