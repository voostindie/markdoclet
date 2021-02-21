package nl.ulso.markdoclet;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import java.io.*;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/**
 * Implements the JavaDoc Doclet API from Java 9+.
 */
public class Markdoclet implements Doclet {

    private static final String OPTION_OUTPUT = "-output";
    private static final String OPTION_PROPERTIES = "-properties";
    private static final String OPTION_TITLE = "-title";

    private File outputFile = null;
    private Properties properties = null;
    private String title = null;

    private Reporter reporter;

    @Override
    public void init(Locale locale, Reporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public String getName() {
        return "Markdoclet";
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return Set.of(
                new StandardOption(OPTION_OUTPUT, "Write Doclet output to the specified file", 1) {
                    @Override
                    public boolean process(String option, List<String> arguments) {
                        outputFile = new File(arguments.get(0));
                        if (outputFile.exists() && outputFile.isDirectory()) {
                            reporter.print(Diagnostic.Kind.ERROR, "Output file "
                                    + outputFile.getAbsolutePath() + " is a directory.");
                            return false;
                        }
                        if (outputFile.exists() && !outputFile.canWrite()) {
                            reporter.print(Diagnostic.Kind.ERROR, "Output file "
                                    + outputFile.getAbsolutePath() + " exists and cannot be overwritten.");
                            return false;
                        }
                        return true;
                    }
                },
                new StandardOption(OPTION_PROPERTIES, "Properties file to use", 1) {
                    @Override
                    public boolean process(String option, List<String> arguments) {
                        var propertiesFile = arguments.get(0);
                        try (var fileReader = new FileReader(propertiesFile);
                             var reader = new BufferedReader(fileReader)) {
                            properties = new Properties();
                            properties.load(reader);
                        } catch (IOException e) {
                            reporter.print(Diagnostic.Kind.ERROR,
                                    "Couldn't load properties from " + propertiesFile);
                            return false;
                        }
                        return true;
                    }
                },
                new StandardOption(OPTION_TITLE, "Title to use in the output document", 1) {
                    @Override
                    public boolean process(String option, List<String> arguments) {
                        title = arguments.get(0);
                        return true;
                    }
                }
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_11;
    }

    @Override
    public boolean run(DocletEnvironment environment) {
        if (outputFile == null) {
            reporter.print(Kind.ERROR, "Output file is not set. Please use -output to do so!");
            return false;
        }
        reporter.print(Kind.NOTE, "Parsing Java code");
        var document = new JavadocToDocumentConverter(environment, reporter, title, properties)
                .createDocument();
        reporter.print(Kind.NOTE, "Generating Markdown output to " + outputFile);
        var error = new DocumentToMarkdownWriter(createOutputWriter()).writeDocument(document);
        var result = (error == null);
        if (error != null) {
            reporter.print(Kind.ERROR, error);
        }
        reporter.print(Kind.NOTE, "Done!");
        return result;
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

    private PrintWriter createOutputWriter() {
        if (customOutputWriter != null) {
            return customOutputWriter;
        }
        try {
            return new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open file for writing: " + e.getMessage(), e);
        }
    }

    private static abstract class StandardOption implements Option {

        private final String name;
        private final String description;
        private final int argumentCount;

        StandardOption(String name, String description, int argumentCount) {
            this.name = name;
            this.description = description;
            this.argumentCount = argumentCount;
        }

        @Override
        public int getArgumentCount() {
            return argumentCount;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public Kind getKind() {
            return Kind.STANDARD;
        }

        @Override
        public List<String> getNames() {
            return List.of(name);
        }

        public String getParameters() {
            return "";
        }
    }
}
