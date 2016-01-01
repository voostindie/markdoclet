package nl.ulso.markdoclet.document;

public class Attribute extends Section {

    private final String type;

    private Attribute(Builder builder) {
        super(builder);
        this.type = builder.type;
    }

    public String getType() {
        return type;
    }

    static Builder newBuilder(String name, String type) {
        return new Builder(name, type);
    }

    public static final class Builder extends Section.Builder<Builder> {

        private final String type;

        private Builder(String name, String type) {
            super(name);
            this.type = type;
        }

        public Attribute build() {
            return new Attribute(this);
        }
    }
}
