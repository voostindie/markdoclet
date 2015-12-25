/*
 * Copyright 2015 Vincent Oostindie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License
 *
 */

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
