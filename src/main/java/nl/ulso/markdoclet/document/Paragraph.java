package nl.ulso.markdoclet.document;

public class Paragraph {
    private final String type;
    private final String contents;

    public Paragraph(String type, String contents) {
        this.type = type;
        this.contents = contents;
    }

    public String getType() {
        return type;
    }

    public String getContents() {
        return contents;
    }

}
