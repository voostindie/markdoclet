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

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import nl.ulso.markdoclet.document.Document;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class DocumentToMarkdownWriter {

    private final PrintWriter writer;
    private final Configuration configuration;

    public DocumentToMarkdownWriter(PrintWriter writer) {
        this.writer = writer;
        configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setClassLoaderForTemplateLoading(
                Thread.currentThread().getContextClassLoader(),
                "nl/ulso/markdoclet");
        configuration.setDefaultEncoding("UTF-8");
    }


    public String writeDocument(Document document) {
        try {
            final Template template = configuration.getTemplate("document.ftl");
            final HashMap<String, Object> model = new HashMap<>();
            model.put("document", document);
            model.put("dateGenerated", new SimpleDateFormat("dd-MM-YYYY").format(new Date()));
            template.process(model, writer);
        } catch (IOException | TemplateException e) {
            return "Could not generate Markdown due to error: " + e.getMessage();
        }
        return null;
    }
}
