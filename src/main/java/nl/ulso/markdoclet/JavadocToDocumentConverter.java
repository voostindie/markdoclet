package nl.ulso.markdoclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import nl.ulso.markdoclet.document.*;

import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Generates functional documentation in Markdown format from Java API's.
 * <p>
 * This Doclet only extracts documentation that is bound to a tag starting with {@code @md.}. All other
 * documentation is skipped. (By the way: if you generate normal JavaDoc, these special tags are skipped.)
 * </p>
 * <p>
 * For normal documentation that must always show up in the Markdown document, use {@code @md.common}. Any
 * documentation under this tag will be unconditionally copied to the output.
 * </p>
 * To hide an interface, attribute, operation, enumeration, or constant, add a {@code md.hide} tag.
 * <p>
 * For special cases you can use any kind of tag, as long as it starts with {@code @md.}. But: when running
 * JavaDoc with this Doclet you should then also pass it a reference to a properties file that contains a
 * description of each of these tags. This documentation is then copied into the document as well. If you forget
 * to this this you will get an ugly reminder in the Markdown document.
 * </p>
 * <p>
 * Sample properties file:
 * </p>
 * <pre><code>
 * foo=In special situation "foo"
 * bar=In special situation "bar"
 * </code></pre>
 */
public class JavadocToDocumentConverter {

    private static final String CUSTOM_TAG_PREFIX = "@md.";
    private static final String HIDE_TAG = CUSTOM_TAG_PREFIX + "hide";
    private static final Pattern LINE_PATTERN = Pattern.compile("\\v", Pattern.MULTILINE);
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private final RootDoc root;
    private final String title;
    private final Properties properties;

    JavadocToDocumentConverter(RootDoc root, String title, Properties properties) {
        this.root = root;
        this.title = title != null ? title : "API documentation";
        this.properties = properties != null ? properties : new Properties();
    }

    public Document createDocument() {
        final Document.Builder builder = Document.newBuilder(title);
        properties.forEach((key, value) -> builder.withParagraphDescription((String) key, (String) value));
        buildInterfaces(builder);
        buildEnumerations(builder);
        return builder.build();
    }

    private void buildInterfaces(Document.Builder documentBuilder) {
        Stream.of(root.classes())
                .filter(Doc::isInterface)
                .filter(this::isVisible)
                .peek(this::log)
                .forEach(c -> {
                    final Interface.Builder builder = documentBuilder.withInterface(c.name());
                    buildParagraphs(builder, c);
                    buildAttributes(builder, c);
                    buildOperations(builder, c);
                });
    }

    private void buildAttributes(Interface.Builder interfaceBuilder, ClassDoc clazz) {
        Stream.of(clazz.methods())
                .filter(this::isAttribute)
                .filter(this::isVisible)
                .peek(this::log)
                .forEach(m -> {
                    final Attribute.Builder builder = interfaceBuilder.withAttribute(
                            asAttributeName(m.name()), m.returnType().simpleTypeName());
                    buildParagraphs(builder, m);
                });
    }

    private void buildOperations(Interface.Builder interfaceBuilder, ClassDoc clazz) {
        Stream.of(clazz.methods())
                .filter(this::isOperation)
                .filter(this::isVisible)
                .peek(this::log)
                .forEach(m -> {
                    final Operation.Builder builder = interfaceBuilder.withOperation(
                            m.name(), m.returnType().simpleTypeName());
                    buildParagraphs(builder, m);
                    Stream.of(m.parameters()).forEach(p -> builder.withParameter(p.name(), p.type().simpleTypeName()));
                });
    }

    private boolean isAttribute(Doc doc) {
        return doc.name().startsWith("get") || doc.name().startsWith("is");
    }

    private String asAttributeName(String name) {
        return name.substring(name.startsWith("is") ? 2 : 3);
    }

    private boolean isOperation(Doc doc) {
        return !isAttribute(doc);
    }

    private void buildEnumerations(Document.Builder documentBuilder) {
        Stream.of(root.classes())
                .filter(Doc::isEnum)
                .filter(this::isVisible)
                .peek(this::log)
                .forEach(c -> {
                    final Enumeration.Builder builder = documentBuilder.withEnumeration(c.name());
                    buildParagraphs(builder, c);
                    buildConstants(builder, c);
                });
    }

    private void buildConstants(Enumeration.Builder enumerationBuilder, ClassDoc clazz) {
        Stream.of(clazz.enumConstants())
                .filter(this::isVisible)
                .peek(this::log)
                .forEach(f -> {
                    final Constant.Builder builder = enumerationBuilder.withConstant(f.name());
                    buildParagraphs(builder, f);
                });
    }

    private void buildParagraphs(Section.Builder builder, Doc doc) {
        Stream.of(doc.tags())
                .filter(this::isDocumentation)
                .forEach(t -> builder.withParagraph(
                        t.name().substring(CUSTOM_TAG_PREFIX.length()),
                        unindentJavadoc(t.text())));
    }

    private String unindentJavadoc(String javadoc) {
        return LINE_PATTERN.splitAsStream(javadoc)
                .map(this::unindentJavadocLine)
                .collect(joining(LINE_SEPARATOR));
    }

    private String unindentJavadocLine(String line) {
        if (line.length() > 1 && Character.isWhitespace(line.charAt(0))) {
            return line.substring(1);
        } else {
            return line;
        }
    }

    private boolean isDocumentation(Tag tag) {
        return tag.name().startsWith(CUSTOM_TAG_PREFIX);
    }

    private boolean isVisible(Doc doc) {
        return Stream.of(doc.tags()).map(Tag::name).noneMatch(HIDE_TAG::equals);
    }

    private void log(Doc doc) {
        final String type;
        if (doc.isInterface()) {
            type = "interface";
        } else if (doc.isEnum()) {
            type = "enumeration";
        } else if (doc.isMethod()) {
            type = "method";
        } else if (doc.isEnumConstant()) {
            type = "constant";
        } else {
            type = "type";
        }
        root.printNotice("Constructing documentation for " + type + " " + doc.name());
    }
}
