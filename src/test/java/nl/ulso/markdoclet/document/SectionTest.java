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

package nl.ulso.markdoclet.document;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SectionTest {

    @Test
    public void createSectionWithMultipleParagraphs() throws Exception {
        final DummySection section = DummySection.newBuilder()
                .withParagraph("common", "paragraph 1")
                .withParagraph("foo", "paragraph 2")
                .withParagraph("bar", "paragraph 3")
                .build();
        final List<Paragraph> paragraphs = section.getParagraphs();
        assertThat(paragraphs.size(), is(3));
        assertThat(paragraphs.get(0).getContents(), is("paragraph 1"));
        assertThat(paragraphs.get(2).getType(), is("bar"));
    }

    private static class DummySection extends Section {

        private DummySection(DummySection.Builder builder) {
            super(builder);
        }

        static DummySection.Builder newBuilder() {
            return new DummySection.Builder();
        }

        public static final class Builder extends Section.Builder<DummySection.Builder> {

            private Builder() {
                super("dummy");
            }

            public DummySection build() {
                return new DummySection(this);
            }
        }
    }
}