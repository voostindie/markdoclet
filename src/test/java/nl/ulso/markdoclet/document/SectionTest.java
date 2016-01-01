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