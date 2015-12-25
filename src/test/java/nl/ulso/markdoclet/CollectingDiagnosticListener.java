package nl.ulso.markdoclet;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class CollectingDiagnosticListener implements DiagnosticListener<JavaFileObject> {

    private final List<String> diagnostics = new ArrayList<>();

    @Override
    public void report(Diagnostic diagnostic) {
        diagnostics.add(diagnostic.toString());
    }

    public List<String> getDiagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }
}
