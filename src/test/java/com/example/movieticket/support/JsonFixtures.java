package com.example.movieticket.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;

/**
 * Loads JSON fixtures from {@code src/test/resources/fixtures/} and compares responses against
 * them. Integration tests keep request/response bodies in fixture files rather than inline strings.
 */
public final class JsonFixtures {

    private static final String BASE = "fixtures/";

    private JsonFixtures() {
    }

    /**
     * Reads a fixture and substitutes {@code ${key}} placeholders with the given values — for
     * request bodies that reference dynamic ids (e.g. a foreign key created earlier in the test).
     */
    public static String read(String relativePath, Map<String, Object> values) {
        String content = read(relativePath);
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            content = content.replace("${" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return content;
    }

    /** Reads a fixture, e.g. {@code read("auth/request/register-valid.json")}. */
    public static String read(String relativePath) {
        String classpath = BASE + relativePath;
        try (InputStream is = JsonFixtures.class.getClassLoader().getResourceAsStream(classpath)) {
            if (is == null) {
                throw new IllegalArgumentException("Fixture not found: " + classpath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Asserts {@code actual} matches the expected fixture leniently (extra actual fields allowed,
     * order ignored). Paths in {@code ignoredPaths} (e.g. {@code "id"}, {@code "token"}) must be
     * present but their values are not compared — for volatile fields.
     */
    public static void assertMatches(String expectedFixture, String actual, String... ignoredPaths) {
        Customization[] customizations = Arrays.stream(ignoredPaths)
                .map(path -> new Customization(path, (a, b) -> true))
                .toArray(Customization[]::new);
        try {
            JSONAssert.assertEquals(read(expectedFixture), actual,
                    new CustomComparator(JSONCompareMode.LENIENT, customizations));
        } catch (org.json.JSONException e) {
            throw new IllegalStateException("Invalid JSON in fixture/response comparison", e);
        }
    }
}
