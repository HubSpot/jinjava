/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import org.apache.commons.lang3.BooleanUtils;

/**
 * bool(value) Convert value to boolean.
 */
@JinjavaDoc(
        value = "Convert value into a boolean.",
        params = {
                @JinjavaParam(value = "value", desc = "The value to convert to a boolean"),
        },
        snippets = {
                @JinjavaSnippet(
                        desc = "This example converts a text string value to a boolean",
                        code = "{% if \"true\"|bool == true %}hello world{% endif %}")
        })
public class BoolFilter implements Filter {
    @Override
    public String getName() {
        return "bool";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter,
                         String... args) {
        if (var == null) {
            return false;
        }

        final String str = var.toString();

        return str.equals("1") ? Boolean.TRUE : BooleanUtils.toBoolean(str);
    }

}

