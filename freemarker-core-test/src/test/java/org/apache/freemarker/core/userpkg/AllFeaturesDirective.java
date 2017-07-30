/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.freemarker.core.userpkg;

import static org.apache.freemarker.core.TemplateCallableModelUtils.*;

import java.io.IOException;
import java.io.Writer;

import org.apache.freemarker.core.Environment;
import org.apache.freemarker.core.TemplateException;
import org.apache.freemarker.core.model.ArgumentArrayLayout;
import org.apache.freemarker.core.model.CallPlace;
import org.apache.freemarker.core.model.TemplateHashModelEx2;
import org.apache.freemarker.core.model.TemplateModel;
import org.apache.freemarker.core.model.TemplateNumberModel;
import org.apache.freemarker.core.model.TemplateSequenceModel;
import org.apache.freemarker.core.model.impl.SimpleNumber;
import org.apache.freemarker.core.util.StringToIndexMap;

public class AllFeaturesDirective extends TestTemplateDirectiveModel {

    private static final int P1_ARG_IDX = 0;
    private static final int P2_ARG_IDX = 1;
    private static final int N1_ARG_IDX = 2;
    private static final int N2_ARG_IDX = 3;

    private static final String N1_ARG_NAME = "n1";
    private static final String N2_ARG_NAME = "n2";

    private static final ArgumentArrayLayout ARGS_LAYOUT = ArgumentArrayLayout.create(
            2,
            true,
            StringToIndexMap.of(
                    N1_ARG_NAME, N1_ARG_IDX,
                    N2_ARG_NAME, N2_ARG_IDX),
            true
    );

    private static final int P_VARARGS_ARG_IDX = ARGS_LAYOUT.getPositionalVarargsArgumentIndex();
    private static final int N_VARARGS_ARG_IDX = ARGS_LAYOUT.getNamedVarargsArgumentIndex();

    private final boolean p1AllowNull;
    private final boolean p2AllowNull;
    private final boolean n1AllowNull;
    private final boolean n2AllowNull;

    public AllFeaturesDirective() {
        this(true, true, true, true);
    }

    public AllFeaturesDirective(boolean p1AllowNull, boolean p2AllowNull, boolean n1AllowNull, boolean n2AllowNull) {
        this.p1AllowNull = p1AllowNull;
        this.p2AllowNull = p2AllowNull;
        this.n1AllowNull = n1AllowNull;
        this.n2AllowNull = n2AllowNull;
    }

    @Override
    public void execute(TemplateModel[] args, CallPlace callPlace, Writer out, Environment env)
            throws TemplateException, IOException {
        execute(castArgumentToNumber(args, P1_ARG_IDX, p1AllowNull, env),
                castArgumentToNumber(args, P2_ARG_IDX, p2AllowNull, env),
                (TemplateSequenceModel) args[P_VARARGS_ARG_IDX],
                castArgumentToNumber(args[N1_ARG_IDX], N1_ARG_NAME, n1AllowNull, env),
                castArgumentToNumber(args[N2_ARG_IDX], N2_ARG_NAME, n2AllowNull, env),
                (TemplateHashModelEx2) args[N_VARARGS_ARG_IDX],
                out, env, callPlace);
    }

    private void execute(TemplateNumberModel p1, TemplateNumberModel p2, TemplateSequenceModel pOthers,
            TemplateNumberModel n1, TemplateNumberModel n2, TemplateHashModelEx2 nOthers,
            Writer out, Environment env, CallPlace callPlace) throws IOException, TemplateException {
        out.write("#a(");
        printParam("p1", p1, out, true);
        printParam("p2", p2, out);
        printParam("pVarargs", pOthers, out);
        printParam(N1_ARG_NAME, n1, out);
        printParam(N2_ARG_NAME, n2, out);
        printParam("nVarargs", nOthers, out);
        int nestedContParamCnt = callPlace.getNestedContentParameterCount();
        if (nestedContParamCnt != 0) {
            out.write("; " + nestedContParamCnt);
        }
        out.write(")");

        if (callPlace.hasNestedContent()) {
            out.write(" {");
            if (p1 != null) {
                int intP1 = p1.getAsNumber().intValue();
                for (int i = 0; i < intP1; i++) {
                    // We dynamically set as many nested content parameters as many the caller has declared; this is
                    // unusual, and is for testing purposes only.
                    TemplateModel[] nestedContParamValues = new TemplateModel[nestedContParamCnt];
                    for (int paramIdx = 0; paramIdx < nestedContParamValues.length; paramIdx++) {
                        nestedContParamValues[paramIdx] = new SimpleNumber((i + 1) * (paramIdx + 1));
                    }
                    callPlace.executeNestedContent(nestedContParamValues, out, env);
                }
            }
            out.write("}");
        }
    }

    @Override
    public ArgumentArrayLayout getArgumentArrayLayout() {
        return ARGS_LAYOUT;
    }
}
