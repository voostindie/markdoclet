package nl.ulso.markdoclet;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.util.SimpleDocTreeVisitor;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import nl.ulso.markdoclet.document.Enumeration;
import nl.ulso.markdoclet.document.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.ElementKind.*;

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
 * <p>
 * In February 2021 I updated the implementation for JDK 9+'s newer Doclet API. I "made it work". I'm sure
 * the code could be largely rewritten using new API's, with visitors and such. I didn't.
 * </p>
 */
public class JavadocToDocumentConverter {

    private static final String CUSTOM_TAG_PREFIX = "md.";
    private static final String HIDE_TAG = "@" + CUSTOM_TAG_PREFIX + "hide";
    private static final Pattern LINE_PATTERN = Pattern.compile("\\v", Pattern.MULTILINE);
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private final DocletEnvironment environment;
    private final Reporter reporter;
    private final String title;
    private final Properties properties;

    JavadocToDocumentConverter(DocletEnvironment environment, Reporter reporter, String title, Properties properties) {
        this.environment = environment;
        this.reporter = reporter;
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
        environment.getIncludedElements()
                .stream()
                .filter(this::isInterface)
                .filter(this::isVisible)
                .peek(this::log)
                .forEach(type -> {
                    final Interface.Builder builder = documentBuilder.withInterface(type.toString());
                    buildParagraphs(builder, type);
                    buildAttributes(builder, type);
                    buildOperations(builder, type);
                });
    }

    private void buildAttributes(Interface.Builder interfaceBuilder, Element element) {
        element.getEnclosedElements().stream()
                .filter(this::isAttribute)
                .filter(this::isVisible)
                .peek(this::log)
                .forEach(method -> {
                    var executableElement = (ExecutableElement) method;
                    final Attribute.Builder builder = interfaceBuilder.withAttribute(
                            asAttributeName(executableElement.getSimpleName().toString()),
                                    executableElement.getReturnType().toString());
                    buildParagraphs(builder, method);
                });
    }

    private void buildOperations(Interface.Builder interfaceBuilder, Element element) {
        element.getEnclosedElements().stream()
                .filter(this::isOperation)
                .filter(this::isVisible)
                .peek(this::log)
                .forEach(method -> {
                    var executableElement = (ExecutableElement) method;
                    var builder = interfaceBuilder.withOperation(
                            executableElement.getSimpleName().toString(), executableElement.getReturnType().toString());
                    buildParagraphs(builder, method);
                    ((ExecutableElement) method).getParameters().forEach(parameter -> {
                        var variable = (VariableElement) parameter;
                        builder.withParameter(variable.getSimpleName().toString(), variable.asType().toString());
                    });
                });
    }

    private boolean isAttribute(Element element) {
        var name = element.getSimpleName().toString();
        return name.startsWith("get") || name.startsWith("is");
    }

    private String asAttributeName(String name) {
        return name.substring(name.startsWith("is") ? 2 : 3);
    }

    private boolean isOperation(Element element) {
        return !isAttribute(element);
    }

    private void buildEnumerations(Document.Builder documentBuilder) {
        environment.getIncludedElements()
                .stream()
                .filter(this::isEnumeration)
                .filter(this::isVisible)
                .peek(this::log)
                .forEach(e -> {
                    final Enumeration.Builder builder = documentBuilder.withEnumeration(e.toString());
                    buildParagraphs(builder, e);
                    buildConstants(builder, e);
                });
    }

    private void buildConstants(Enumeration.Builder enumerationBuilder, Element enumeration) {
        enumeration.getEnclosedElements().stream()
                .filter(this::isEnumConstant)
                .filter(this::isVisible)
                .peek(this::log)
                .forEach(constant -> {
                    final Constant.Builder builder = enumerationBuilder.withConstant(constant.toString());
                    buildParagraphs(builder, constant);
                });
    }

    private void buildParagraphs(Section.Builder builder, Element element) {
        var blockTags = findBlockTags(element);
        blockTags.forEach((name, text) -> {
            if (name.startsWith(CUSTOM_TAG_PREFIX)) {
                builder.withParagraph(
                        name.substring(CUSTOM_TAG_PREFIX.length()),
                        unindentJavadoc(String.join("\n", text)));
            }
        });
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

    private boolean isInterface(Element element) {
        return element.getKind().isInterface();
    }

    private boolean isEnumeration(Element element) {
        return element.getKind() == ENUM;
    }

    private boolean isEnumConstant(Element element) {
        return element.getKind() == ENUM_CONSTANT;
    }

    private boolean isVisible(Element element) {
        var commentTree = environment.getDocTrees().getDocCommentTree(element);
        if (commentTree == null) {
            return false;
        }
        return commentTree.getBlockTags().stream()
                .noneMatch(docTree -> docTree.toString().startsWith(HIDE_TAG));
    }

    private void log(Element element) {
        final String type;
        var kind = element.getKind();
        if (kind.isInterface()) {
            type = "interface";
        } else if (kind == ENUM) {
            type = "enumeration";
        } else if (kind == METHOD) {
            type = "method";
        } else if (kind == ENUM_CONSTANT) {
            type = "constant";
        } else {
            type = "type";
        }
        reporter.print(Diagnostic.Kind.NOTE,
                "Constructing documentation for " + type + " " + element.getSimpleName());
    }

    Map<String, List<String>> findBlockTags(Element element) {
        var docCommentTree = environment.getDocTrees().getDocCommentTree(element);
        var scanner = new BlockTagScanner();
        scanner.visit(docCommentTree, null);
        return scanner.tags;

    }

    private static class BlockTagScanner extends SimpleDocTreeVisitor<Void, Void> {
        private final Map<String, List<String>> tags = new TreeMap<>();

        @Override
        public Void visitDocComment(DocCommentTree tree, Void p) {
            return visit(tree.getBlockTags(), null);
        }

        @Override
        public Void visitUnknownBlockTag(UnknownBlockTagTree tree, Void p) {
            var name = tree.getTagName();
            var content = tree.getContent().toString();
            tags.computeIfAbsent(name, n -> new ArrayList<>()).add(content);
            return null;
        }
    }
}
