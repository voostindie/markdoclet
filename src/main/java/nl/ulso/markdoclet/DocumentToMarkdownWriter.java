package nl.ulso.markdoclet;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import nl.ulso.markdoclet.document.Document;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Writes a Document to a Markdown file using a Freemarker template.
 */
public class DocumentToMarkdownWriter {

    private final PrintWriter writer;
    private final Configuration configuration;

    public DocumentToMarkdownWriter(PrintWriter writer) {
        this.writer = writer;
        configuration = new Configuration(Configuration.VERSION_2_3_31);
        configuration.setClassLoaderForTemplateLoading(
                Thread.currentThread().getContextClassLoader(),
                "nl/ulso/markdoclet");
        configuration.setDefaultEncoding("UTF-8");
    }


    public String writeDocument(Document document) {
        try {
            var template = configuration.getTemplate("document.ftl");
            var model = new HashMap<String, Object>();
            model.put("document", document);
            model.put("dateGenerated", new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
            template.process(model, writer);
        } catch (IOException | TemplateException e) {
            return "Could not generate Markdown due to error: " + e.getMessage();
        }
        return null;
    }
}
