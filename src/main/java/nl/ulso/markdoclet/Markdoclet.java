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

import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;
import nl.ulso.markdoclet.document.Document;

import java.io.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Implements the JavaDoc Doclet "API" (we shouldn't really call a set of static methods that must be implemented
 * an API...)
 * <p>
 * Instead of processing the passed options as it should (rootDoc.options() -> String[][]) and having to go
 * through the horror of crawling through nested arrays twice, I'm cheating a bit here. When validating the
 * parameters I'm setting static variables. These are then passed to the Doclet explicitly. That way I don't have to
 * do the same processing twice. Still, it's not how it was intended...
 */
public class Markdoclet {

    private static final String OPTION_OUTPUT = "-output";
    private static final String OPTION_PROPERTIES = "-properties";
    private static final String OPTION_TITLE = "-title";

    private static File outputFile = null;
    private static Properties properties = null;
    private static String title = null;

    public static boolean start(RootDoc root) {
        root.printNotice("Parsing Java code");
        final Document document = new JavadocToDocumentConverter(root, title, properties).createDocument();
        root.printNotice("Generating Markdown output to " + outputFile);
        final String error = new DocumentToMarkdownWriter(createOutputWriter()).writeDocument(document);
        final boolean result = (error == null);
        if (error != null) {
            root.printError(error);
        }
        // Because we're mucking around with static fields, we must ensure we reset them too. Yuck.
        title = null;
        properties = null;
        outputFile = null;
        root.printNotice("Done!");
        return result;
    }

    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }


    public static int optionLength(String option) {
        switch (option) {
            case OPTION_OUTPUT:
            case OPTION_PROPERTIES:
            case OPTION_TITLE:
                return 2;
            default:
                return 0;
        }
    }

    public static boolean validOptions(String[][] options, DocErrorReporter reporter) {
        final Set<String> supportedOptions = new HashSet<>(asList(OPTION_OUTPUT, OPTION_PROPERTIES, OPTION_TITLE));
        final Set<String> optionsFound = new HashSet<>();
        boolean valid = true;
        for (String[] option : options) {
            final String name = option[0];
            if (supportedOptions.contains(name)) {
                if (optionsFound.contains(name)) {
                    reporter.printError("Only one " + name + " option allowed.");
                    valid = false;
                }
                optionsFound.add(name);
                final String value = option[1];
                switch (name) {
                    case OPTION_OUTPUT:
                        valid = valid && verifyOutputFile(value, reporter);
                        break;
                    case OPTION_PROPERTIES:
                        valid = valid && verifyPropertiesFile(value, reporter);
                        break;
                    case OPTION_TITLE:
                        valid = valid && verifyTitle(value, reporter);
                        break;
                }
            }
        }
        if (!optionsFound.contains(OPTION_OUTPUT)) {
            reporter.printError("Missing required property -output");
            return false;
        }
        return valid;
    }

    private static boolean verifyOutputFile(String filename, DocErrorReporter reporter) {
        final File file = new File(filename);
        if (file.exists() && file.isDirectory()) {
            reporter.printError("Output file " + filename + " is a directory.");
            return false;
        }
        if (file.exists() && !file.canWrite()) {
            reporter.printError("Output file " + filename + " exists and cannot be overwritten.");
            return false;
        }
        outputFile = file;
        return true;
    }

    private static boolean verifyPropertiesFile(String propertiesFile, DocErrorReporter reporter) {
        try (FileReader fileReader = new FileReader(propertiesFile);
             BufferedReader reader = new BufferedReader(fileReader)) {
            properties = new Properties();
            properties.load(reader);
        } catch (IOException e) {
            reporter.printError("Couldn't load properties from " + propertiesFile);
            return false;
        }
        return true;
    }

    private static boolean verifyTitle(String value, DocErrorReporter reporter) {
        title = value;
        return true;
    }

    /*
     * For testing allow a custom writer to be set so that output goes there no matter what.
     */

    private static PrintWriter customOutputWriter = null;

    static void setCustomOutputWriter(PrintWriter writer) {
        customOutputWriter = writer;
    }

    static void resetOutputWriter() {
        customOutputWriter = null;
    }

    private static PrintWriter createOutputWriter() {
        if (customOutputWriter != null) {
            return customOutputWriter;
        }
        try {
            return new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open file for writing: " + e.getMessage(), e);
        }
    }
}
