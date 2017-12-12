
/**********************************************************************
 * Copyright (c) 2017 HubSpot Inc.
 *
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
 **********************************************************************/
package com.hubspot.jinjava.lib.filter;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Converts the characters { and } in string s to Jinjava-safe sequences. "
        + "Use this filter if you need to display text that might contain such characters in Jinjava. "
        + "Marks return value as markup string.",
    params = {
        @JinjavaParam(value = "s", desc = "String to escape")
    },
    snippets = {
        @JinjavaSnippet(
            code = "{% set escape_string = \"{{This markup is printed as text}}\" %}\n" +
                "{{ escape_string|escapeJinjava }}")
    })

public class EscapeJinjavaFilter implements Filter {

  private static final String SLBRACE = "{";
  private static final String BLBRACE = "&lbrace;";
  private static final String SRBRACE = "}";
  private static final String BRBRACE = "&rbrace;";

  private static final String[] TO_REPLACE = new String[] {
      SLBRACE, SRBRACE
  };
  private static final String[] REPLACE_WITH = new String[] {
      BLBRACE, BRBRACE
  };

  public static String escapeJinjavaEntities(String input) {
    return StringUtils.replaceEach(input, TO_REPLACE, REPLACE_WITH);
  }

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    return escapeJinjavaEntities(Objects.toString(object, ""));
  }

  @Override
  public String getName() {
    return "escape_jinjava";
  }

}
