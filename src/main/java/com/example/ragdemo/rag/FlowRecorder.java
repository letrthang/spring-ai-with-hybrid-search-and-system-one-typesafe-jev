package com.example.ragdemo.rag;

import java.util.ArrayList;
import java.util.List;

/**
 * Records what happened during one question, hop by hop, for the 🔀 Data flow dialog: who called whom, when (ms
 * after the question) and for how long. Never put a key in a hop — only counts, titles, scores and short text.
 */
public class FlowRecorder {

    /** The lifelines of the sequence diagram, left to right. */
    public enum Actor {
        USER("👤 User"), APP("☕ App"), WEAVIATE("🗄️ Weaviate"), SYSTEM1("⚡ System 1"), SYSTEM2("🧠 System 2");

        public final String label;

        Actor(String label) {
            this.label = label;
        }
    }

    public enum Kind { CALL, RETURN, NOTE }

    /** One arrow (or note) of the diagram. {@code atMs} = start, ms after the question; {@code ms} = how long. */
    public record FlowHop(int seq, Actor from, Actor to, Kind kind, String label, long atMs, long ms, String summary) {
    }

    /** Longest summary kept per hop, so a trace stays small. */
    static final int MAX_SUMMARY = 300;

    private final long t0 = System.currentTimeMillis();
    private final List<FlowHop> hops = new ArrayList<>();

    /** Milliseconds since the question was asked. */
    public long now() {
        return System.currentTimeMillis() - t0;
    }

    public void call(Actor from, Actor to, String label, long atMs, long ms, String summary) {
        add(from, to, Kind.CALL, label, atMs, ms, summary);
    }

    public void reply(Actor from, Actor to, String label, long atMs, String summary) {
        add(from, to, Kind.RETURN, label, atMs, 0, summary);
    }

    public void note(Actor on, String label, String summary) {
        add(on, on, Kind.NOTE, label, now(), 0, summary);
    }

    public List<FlowHop> hops() {
        return List.copyOf(hops);
    }

    private void add(Actor from, Actor to, Kind kind, String label, long atMs, long ms, String summary) {
        hops.add(new FlowHop(hops.size() + 1, from, to, kind, label, atMs, ms, shorten(summary)));
    }

    public static String shorten(String text) {
        if (text == null) return "";
        String oneLine = text.replaceAll("\\s+", " ").strip();
        return oneLine.length() <= MAX_SUMMARY ? oneLine : oneLine.substring(0, MAX_SUMMARY - 1) + "…";
    }
}
