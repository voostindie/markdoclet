package nl.ulso.markdoclet;

import jdk.javadoc.doclet.Doclet;

import javax.tools.ToolProvider;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base class for tests on Doclets.
 * <p>
 * This class assumes that:
 * </p>
 * <ul>
 * <li>The test class has the name of the Doclet under test, with "Test" appended to it.</li>
 * <li>The Doclet has a method called {@code setCustomOutputWriter(PrintWriter)} that can be called to set a custom
 * output writer to send all Doclet output to.</li>
 * <li>The Doclet has a method called {@code resetOutputWriter()} that reverts the Doclet to its default behavior.</li>
 * <li>All classes to run the Doclet against are in the <strong>src/test/java</strong> directory.</li>
 * </ul>
 * <p>
 * Given that these assumptions are valid, a subclass can just define as many test methods as needed, calling
 * {@link #runDoclet(List, Class[])} to run the Doclet on some classes. Afterwards use one or more of the several
 * {@code assert}-methods on the {@link DocletResult} to validate the outcome.
 * </p>
 */
public abstract class AbstractDocletTestCase {

    private static final String SOURCES_PATH =
            "src" + File.separator + "test" + File.separator + "java" + File.separator;

    protected final DocletResult runDoclet(List<String> options, Class<?>... types) throws Exception {
        final List<String> files = Stream.of(types)
                .map(Class::getCanonicalName)
                .map(name -> name.replace('.', File.separatorChar))
                .map(path -> path + ".java")
                .map(file -> SOURCES_PATH + file)
                .collect(Collectors.toList());
        return runJavaDoc(files, options);
    }

    private DocletResult runJavaDoc(List<String> files, List<String> options) throws Exception {
        final Class<Doclet> docletClass = resolveDocletClass();
        try (StringWriter outputBuffer = new StringWriter();
             PrintWriter outputWriter = new PrintWriter(outputBuffer)) {
            prepareOutputWriter(docletClass, outputWriter);

            var diagnosticListener = new CollectingDiagnosticListener();
            var fileManager = ToolProvider.getSystemJavaCompiler()
                    .getStandardFileManager(diagnosticListener, null, null);
            var documentationTool = ToolProvider.getSystemDocumentationTool();
            var task = documentationTool.getTask(
                    null,
                    fileManager,
                    diagnosticListener,
                    docletClass,
                    options,
                    fileManager.getJavaFileObjectsFromStrings(files));
            final Boolean result = task.call();
            outputWriter.flush();
            return new DocletResult(result, outputBuffer.toString(), diagnosticListener.getDiagnostics());
        } finally {
            resetOutputWriter(docletClass);
        }
    }

    private Class<Doclet> resolveDocletClass()  {
        final String testClassName = this.getClass().getCanonicalName();
        final String docletClassName = testClassName.substring(0, testClassName.length() - 4);
        try {
            return (Class<Doclet>) Class.forName(docletClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find Doclet class. Expecting a class called "
                    + docletClassName + ". Did you make a typo somewhere?");
        }
    }

    private void prepareOutputWriter(Class<?> docletClass, PrintWriter outputWriter) throws Exception {
        final Method method;
        try {
            method = docletClass.getDeclaredMethod("setCustomOutputWriter", PrintWriter.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot find the static setCustomOutputWriter(PrintWriter) method "
                    + "in the Doclet class " + docletClass + ". Please add and implement it by writing your "
                    + " Doclet output to it when set.");
        }
        method.invoke(docletClass, outputWriter);
    }

    private void resetOutputWriter(Class<?> docletClass) throws Exception {
        final Method method;
        try {
            method = docletClass.getDeclaredMethod("resetOutputWriter");
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot find the static resetOutputWriter() method in the Doclet "
                    + "class " + docletClass + ". Please add and implement it by resetting the Doclet to its "
                    + " default output writing behavior.");
        }
        method.invoke(docletClass);
    }
}
