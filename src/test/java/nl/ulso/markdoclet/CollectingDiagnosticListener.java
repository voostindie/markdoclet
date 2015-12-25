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
