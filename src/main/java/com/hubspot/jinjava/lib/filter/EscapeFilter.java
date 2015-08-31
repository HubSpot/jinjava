/**********************************************************************
 * Copyright (c) 2014 HubSpot Inc.
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
    value = "Converts the characters &, <, >, ‘, and ” in string s to HTML-safe sequences. "
        + "Use this filter if you need to display text that might contain such characters in HTML. "
        + "Marks return value as markup string.",
    params = {
        @JinjavaParam(value = "s", desc = "String to escape")
    },
    snippets = {
        @JinjavaSnippet(
            code = "{% set escape_string = \"<div>This markup is printed as text</div>\" %}\n" +
                "{{ escape_string|escape }}")
    })
public class EscapeFilter implements Filter {

  private static final String SAMP = "&";
  private static final String BAMP = "&amp;";
  private static final String SGT = ">";
  private static final String BGT = "&gt;";
  private static final String SLT = "<";
  private static final String BLT = "&lt;";
  private static final String BSQ = "&#39;";
  private static final String BDQ = "&quot;";

  private static final String[] TO_REPLACE = new String[] {
      SAMP, SGT, SLT, "'", "\""
  };
  private static final String[] REPLACE_WITH = new String[] {
      BAMP, BGT, BLT, BSQ, BDQ
  };

  public static String escapeHtmlEntities(String input) {
    return StringUtils.replaceEach(input, TO_REPLACE, REPLACE_WITH);
  }

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    return escapeHtmlEntities(Objects.toString(object, ""));
  }

  @Override
  public String getName() {
    return "escape";
  }

}
