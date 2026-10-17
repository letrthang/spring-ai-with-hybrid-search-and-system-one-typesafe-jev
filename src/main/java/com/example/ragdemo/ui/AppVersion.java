package com.example.ragdemo.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The version this build runs: {@code build.version} from Spring Boot's {@code META-INF/build-info.properties}, which
 * the {@code build-info} goal of spring-boot-maven-plugin writes from the {@code pom.xml} version. On the
 * {@code release} branch (what Render deploys) that is the release version, e.g. 1.0.0; on {@code develop} a snapshot.
 */
final class AppVersion {

    static final String GITHUB = "https://github.com/letrthang/spring-ai-with-hybrid-search-and-system-one-typesafe-jev";

    /** e.g. "1.0.0" or "1.1.0-SNAPSHOT"; null when the build wrote no build-info (e.g. run from an IDE without Maven). */
    static final String VERSION = read();

    private AppVersion() {
    }

    /** A released version (no -SNAPSHOT). */
    static boolean released() {
        return VERSION != null && !VERSION.endsWith("-SNAPSHOT");
    }

    /** The GitHub release notes of this version; all releases for a snapshot. */
    static String notesUrl() {
        return released() ? GITHUB + "/releases/tag/v" + VERSION : GITHUB + "/releases";
    }

    private static String read() {
        try (InputStream in = AppVersion.class.getResourceAsStream("/META-INF/build-info.properties")) {
            if (in == null) return null;
            Properties build = new Properties();
            build.load(in);
            String version = build.getProperty("build.version");
            return version == null || version.isBlank() ? null : version.strip();
        } catch (IOException e) {
            return null;
        }
    }
}
