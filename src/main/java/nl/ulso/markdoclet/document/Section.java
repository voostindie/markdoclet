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
            paragraphs.add(createParagraph(type, contents));
            return this;
        }

        private Paragraph createParagraph(String type, String contents) {
            return new Paragraph(type, contents);
        }
    }
}
