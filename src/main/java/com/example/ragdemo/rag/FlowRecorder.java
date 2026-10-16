package com.example.ragdemo.rag;

import java.util.ArrayList;
import java.util.List;

/**
 * Records what happened during one question, hop by hop, for the 🔀 Data flow dialog: who called whom, when (ms
 * after the question) and for how long. Labels and summaries are {@link Msg}s (translated by the UI); data such as
 * the question, titles or the answer travel as {@link Msg#raw}. Never put a key in a hop.
 */
public class FlowRecorder {

    /**
     * The lifelines of the sequence diagram, left to right. Their names are i18n keys "actor.USER" etc. H2 (the app's
     * database) only appears when a hop uses it — 🛠 the agent's recent_questions tool.
     */
    public enum Actor { USER, APP, WEAVIATE, SYSTEM1, SYSTEM2, H2 }

    public enum Kind { CALL, RETURN, NOTE }

    /** One arrow (or note) of the diagram. {@code atMs} = start, ms after the question; {@code ms} = how long. */
    public record FlowHop(int seq, Actor from, Actor to, Kind kind, Msg label, long atMs, long ms, Msg summary) {
    }

    /** Longest raw summary kept per hop, so a trace stays small. */
    static final int MAX_SUMMARY = 300;

    private final long t0 = System.currentTimeMillis();
    private final List<FlowHop> hops = new ArrayList<>();

    /** Milliseconds since the question was asked. */
    public long now() {
        return System.currentTimeMillis() - t0;
    }

    public void call(Actor from, Actor to, Msg label, long atMs, long ms, Msg summary) {
        add(from, to, Kind.CALL, label, atMs, ms, summary);
    }

    public void reply(Actor from, Actor to, Msg label, long atMs, Msg summary) {
        add(from, to, Kind.RETURN, label, atMs, 0, summary);
    }

    public void note(Actor on, Msg label, Msg summary) {
        add(on, on, Kind.NOTE, label, now(), 0, summary);
    }

    public List<FlowHop> hops() {
        return List.copyOf(hops);
    }

    private void add(Actor from, Actor to, Kind kind, Msg label, long atMs, long ms, Msg summary) {
        Msg kept = Msg.RAW.equals(summary.key()) ? Msg.raw(shorten(summary.args().getFirst())) : summary;
        hops.add(new FlowHop(hops.size() + 1, from, to, kind, label, atMs, ms, kept));
    }

    public static String shorten(String text) {
        if (text == null) return "";
        String oneLine = text.replaceAll("\\s+", " ").strip();
        return oneLine.length() <= MAX_SUMMARY ? oneLine : oneLine.substring(0, MAX_SUMMARY - 1) + "…";
    }
}
