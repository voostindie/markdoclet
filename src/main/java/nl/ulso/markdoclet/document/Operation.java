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

package nl.ulso.markdoclet.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Operation extends Section {

    private final String returnType;
    private final List<Parameter> parameters;

    private Operation(Builder builder) {
        super(builder);
        returnType = builder.returnType;
        parameters = Collections.unmodifiableList(builder.parameters);
    }

    public String getReturnType() {
        return returnType;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    static Builder newBuilder(String name, String returnType) {
        return new Builder(name, returnType);
    }

    public static final class Builder extends Section.Builder<Builder> {

        public String returnType;
        public List<Parameter> parameters;

        private Builder(String name, String returnType) {
            super(name);
            this.parameters = new ArrayList<>();
            this.returnType = returnType;
        }

        public Builder withParameter(String name, String type) {
            parameters.add(new Parameter(name, type));
            return this;
        }

        public Operation build() {
            return new Operation(this);
        }
    }
}
