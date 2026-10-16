package com.example.ragdemo.rag;

import java.util.List;

/**
 * Everything one question produced: the answer, where it came from (📚 Sources), the timings (🔬 Trace) and every hop
 * (🔀 Data flow). If a required service failed, {@code failure} says which one and why, {@code text} is null, and the
 * trace and flow show how far the question got.
 */
public record RagAnswer(String question, String text, List<Source> sources, List<TraceLine> trace,
                        List<FlowRecorder.FlowHop> flow, String model, long totalMs, Failure failure) {

    /** One passage found by the search, with how each step judged it. {@code citation} = its [n] in the prompt. */
    public record Source(String title, String source, Integer chunkNo, String text,
                         Integer bm25Rank, Integer vectorRank, Double rrfScore,
                         Double relevant, Double answers, Double injection, String verdict, Integer citation) {

        public boolean used() {
            return citation != null;
        }
    }

    /** One line of 🔬 Trace, e.g. "⚡ System 1" → "4 calls in parallel, 4.5 s — kept 2". */
    public record TraceLine(String step, String detail) {
    }

    /** Which required service failed, and why (never a key). */
    public record Failure(ServiceStatus.Service service, String reason) {
    }

    public boolean failed() {
        return failure != null;
    }

    public long usedCount() {
        return sources.stream().filter(Source::used).count();
    }
}
