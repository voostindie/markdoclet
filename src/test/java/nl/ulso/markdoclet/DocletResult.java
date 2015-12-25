package nl.ulso.markdoclet;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class DocletResult {
    private final boolean success;
    private final String output;
    private final List<String> diagnostics;

    public DocletResult(boolean success, String output, List<String> diagnostics) {
        this.success = success;
        this.output = output;
        this.diagnostics = diagnostics;
    }

    public void assertSuccess() {
        assertTrue(success);
    }

    public void assertFailure() {
        assertFalse(success);
    }

    public void assertOutput(String text) {
        assertThat(output, containsString(text));
    }

    public void assertNoOutput() {
        assertThat(output, equalTo(""));
    }

    public void assertError(String text) {
        assertTrue(diagnostics.stream()
                .filter(m -> m.contains(text))
                .count() > 0);
    }

    public void assertNoErrors() {
        assertTrue(diagnostics.stream()
                .filter(m -> !m.startsWith("Note: "))
                .count() == 0);
    }
}
