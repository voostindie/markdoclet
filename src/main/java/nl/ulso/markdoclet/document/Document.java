package nl.ulso.markdoclet.document;

import java.util.*;
import java.util.stream.Collectors;

public class Document {

    private final String title;
    private final List<Interface> interfaces;
    private final List<Enumeration> enumerations;
    private final Map<String, String> paragraphDescriptions;

    private Document(Builder builder) {
        title = builder.title;
        // TODO: automatically order the interfaces in a smart way.
        interfaces = builder.interfaces.stream()
                .map(Interface.Builder::build)
                .collect(Collectors.toList());
        enumerations = builder.enumerations.stream()
                .map(Enumeration.Builder::build)
                .collect(Collectors.toList());
        paragraphDescriptions = new HashMap<>(builder.paragraphDescriptions);
    }

    public String getTitle() {
        return title;
    }

    public List<Interface> getInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }

    public List<Enumeration> getEnumerations() {
        return Collections.unmodifiableList(enumerations);
    }

    public String getHeader(String paragraphType) {
        if (paragraphDescriptions.containsKey(paragraphType)) {
            return paragraphDescriptions.get(paragraphType);
        }
        return "**Tag `" + paragraphType + "` is not documented! Please add it to the properties file!**";
    }

    public static Builder newBuilder(String title) {
        return new Builder(title);
    }

    public static class Builder {
        private final String title;
        private Map<String, String> paragraphDescriptions;
        private List<Interface.Builder> interfaces;
        private List<Enumeration.Builder> enumerations;

        private Builder(String title) {
            this.title = title;
            paragraphDescriptions = new HashMap<>();
            paragraphDescriptions.put("common", "");
            interfaces = new ArrayList<>();
            enumerations = new ArrayList<>();
        }

        public Document build() {
            return new Document(this);
        }

        public Builder withParagraphDescription(String type, String description) {
            paragraphDescriptions.put(type, description);
            return this;
        }

        public Interface.Builder withInterface(String name) {
            final Interface.Builder builder = Interface.newBuilder(name);
            interfaces.add(builder);
            return builder;
        }

        public Enumeration.Builder withEnumeration(String name) {
            final Enumeration.Builder builder = Enumeration.newBuilder(name);
            enumerations.add(builder);
            return builder;
        }
    }
}
