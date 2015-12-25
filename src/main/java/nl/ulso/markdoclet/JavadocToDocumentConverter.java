/*
 * Copyright 2015 Vincent Oostindie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License
 *
 */

package nl.ulso.markdoclet;

import com.sun.javadoc.*;
import nl.ulso.markdoclet.document.*;

import java.util.Properties;
import java.util.stream.Stream;

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
                .peek(c -> printNotice("interface", c.name()))
                .forEach(c -> {
                    final Interface.Builder builder = documentBuilder.withInterface(c.name());
                    buildParagraphs(builder, c.tags());
                    buildAttributes(builder, c);
                    buildOperations(builder, c);
                });
    }

    private void buildAttributes(Interface.Builder interfaceBuilder, ClassDoc clazz) {
        Stream.of(clazz.methods())
                .filter(m -> isAttribute(m.name()))
                .peek(m -> printNotice("attribute", m.name()))
                .forEach(m -> {
                    final Attribute.Builder builder = interfaceBuilder.withAttribute(
                            asAttributeName(m.name()), m.returnType().simpleTypeName());
                    buildParagraphs(builder, m.tags());
                });
    }

    private void buildOperations(Interface.Builder interfaceBuilder, ClassDoc clazz) {
        Stream.of(clazz.methods())
                .filter(m -> isOperation(m.name()))
                .peek(m -> printNotice("operation", m.name()))
                .forEach(m -> {
                    final Operation.Builder builder = interfaceBuilder.withOperation(
                            m.name(), m.returnType().simpleTypeName());
                    buildParagraphs(builder, m.tags());
                    Stream.of(m.parameters())
                            .forEach(p -> builder.withParameter(p.name(), p.type().simpleTypeName()));
                });
    }

    private boolean isAttribute(String name) {
        return name.startsWith("get") || name.startsWith("is");
    }

    private String asAttributeName(String name) {
        return name.substring(name.startsWith("is") ? 2 : 3);
    }

    private boolean isOperation(String name) {
        return !isAttribute(name);
    }

    private void buildEnumerations(Document.Builder documentBuilder) {
        Stream.of(root.classes())
                .filter(Doc::isEnum)
                .peek(c -> printNotice("enumeration", c.name()))
                .forEach(c -> {
                    final Enumeration.Builder builder = documentBuilder.withEnumeration(c.name());
                    buildParagraphs(builder, c.tags());
                    Stream.of(c.enumConstants())
                            .peek(f -> printNotice("constant", f.name()))
                            .forEach(f -> buildConstants(builder, f));
                });
    }

    private void buildConstants(Enumeration.Builder enumerationBuilder, FieldDoc field) {
        final Constant.Builder builder = enumerationBuilder.withConstant(field.name());
        buildParagraphs(builder, field.tags());
    }

    private void buildParagraphs(Section.Builder builder, Tag[] tags) {
        Stream.of(tags)
                .filter(t -> isDocumentationTag(t.name()))
                .forEach(t -> builder.withParagraph(t.name().substring(CUSTOM_TAG_PREFIX.length()), t.text()));
    }

    private boolean isDocumentationTag(String name) {
        return name.startsWith(CUSTOM_TAG_PREFIX);
    }

    private void printNotice(String type, String name) {
        root.printNotice("Construction documentation for " + type + " " + name);
    }
}
