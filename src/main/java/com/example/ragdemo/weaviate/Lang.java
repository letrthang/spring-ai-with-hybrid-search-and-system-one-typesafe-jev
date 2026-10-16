package com.example.ragdemo.weaviate;

import java.util.regex.Pattern;

/**
 * The language of a question or passage: {@code en}, {@code vi} or {@code ja} — the three languages of the sample
 * data (folders {@code sample-data/en|vi|ja}).
 *
 * <p>Search keeps to the question's language. The sample data holds the same passages in three languages; without
 * this, an English question about PCR would get the English, Vietnamese and Japanese copies of one passage as its
 * top results — one fact three times instead of three facts. Detection is by script, simple and predictable:
 * Japanese kana/kanji → ja; Vietnamese letters (ă â đ ê ô ơ ư or a tone mark) → vi; otherwise en.
 */
public final class Lang {

    public static final String EN = "en";
    public static final String VI = "vi";
    public static final String JA = "ja";

    private static final Pattern JAPANESE = Pattern.compile("[\\u3040-\\u30ff\\u4e00-\\u9fff\\uff66-\\uff9f]");
    private static final Pattern VIETNAMESE = Pattern.compile(
            "[ăâđêôơưĂÂĐÊÔƠƯ\\u1ea0-\\u1ef9àáãèéìíòóõùúýÀÁÃÈÉÌÍÒÓÕÙÚÝ]");

    private Lang() {
    }

    public static String of(String text) {
        if (text == null) return EN;
        if (JAPANESE.matcher(text).find()) return JA;
        if (VIETNAMESE.matcher(text).find()) return VI;
        return EN;
    }
}
