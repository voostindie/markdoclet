package nl.ulso.markdoclet.document;

public class Parameter {
    private final String name;
    private final String type;

    public Parameter(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
