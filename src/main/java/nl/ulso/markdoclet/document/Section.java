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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Section {

    private final String name;
    private final List<Paragraph> paragraphs;

    protected Section(Builder builder) {
        this.name = builder.name;
        this.paragraphs = Collections.unmodifiableList(new ArrayList<>(builder.paragraphs));
    }

    public String getName() {
        return name;
    }

    public List<Paragraph> getParagraphs() {
        return paragraphs;
    }

    public abstract static class Builder<B extends Builder> {

        private final String name;
        private final List<Paragraph> paragraphs;

        Builder(String name) {
            this.name = name;
            this.paragraphs = new ArrayList<>();
        }

        public Builder<B> withParagraph(String type, String contents) {
            paragraphs.add(new Paragraph(type, contents));
            return this;
        }

    }
}
