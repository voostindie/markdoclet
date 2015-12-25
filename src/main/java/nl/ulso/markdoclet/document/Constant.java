package nl.ulso.markdoclet.document;

public class Constant extends Section {

    private Constant(Builder builder) {
        super(builder);
    }

    static Builder newBuilder(String name) {
        return new Builder(name);
    }

    public static final class Builder extends Section.Builder<Builder> {

        private Builder(String name) {
            super(name);
        }

        public Constant build() {
            return new Constant(this);
        }
    }
}
