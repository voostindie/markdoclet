package nl.ulso.markdoclet.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Interface extends Section {

    private final List<Attribute> attributes;
    private final List<Operation> operations;

    private Interface(Builder builder) {
        super(builder);
        attributes = builder.attributes.stream()
                .map(Attribute.Builder::build)
                .collect(Collectors.toList());
        operations = builder.operations.stream()
                .map(Operation.Builder::build)
                .collect(Collectors.toList());
    }

    public List<Attribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    public List<Operation> getOperations() {
        return Collections.unmodifiableList(operations);
    }

    static Builder newBuilder(String name) {
        return new Builder(name);
    }

    public static final class Builder extends Section.Builder<Builder> {
        private List<Attribute.Builder> attributes;
        private List<Operation.Builder> operations;

        private Builder(String name) {
            super(name);
            attributes = new ArrayList<>();
            operations = new ArrayList<>();
        }

        public Interface build() {
            return new Interface(this);
        }

        public Attribute.Builder withAttribute(String name, String type) {
            final Attribute.Builder builder = Attribute.newBuilder(name, type);
            attributes.add(builder);
            return builder;
        }

        public Operation.Builder withOperation(String name, String returnType) {
            final Operation.Builder builder = Operation.newBuilder(name, returnType);
            operations.add(builder);
            return builder;
        }
    }
}
