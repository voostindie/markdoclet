package nl.ulso.markdoclet.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Enumeration extends Section {

    private final List<Constant> constants;

    private Enumeration(Builder builder) {
        super(builder);
        constants = builder.constants.stream()
                .map(Constant.Builder::build)
                .collect(Collectors.toList());
    }

    public List<Constant> getConstants() {
        return Collections.unmodifiableList(constants);
    }

    static Builder newBuilder(String name) {
        return new Builder(name);
    }

    public static final class Builder extends Section.Builder<Builder> {

        private List<Constant.Builder> constants;

        private Builder(String name) {
            super(name);
            constants = new ArrayList<>();
        }

        public Constant.Builder withConstant(String name) {
            final Constant.Builder builder = Constant.newBuilder(name);
            constants.add(builder);
            return builder;
        }

        public Enumeration build() {
            return new Enumeration(this);
        }
    }
}
