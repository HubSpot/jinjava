/**********************************************************************
 * Copyright (c) 2022 HubSpot Inc.
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

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.Objects;
import org.apache.commons.lang3.StringEscapeUtils;

@JinjavaDoc(
  value = "Converts HTML entities in string s to Unicode characters.",
  input = @JinjavaParam(value = "s", desc = "String to unescape", required = true),
  snippets = {
    @JinjavaSnippet(
      code = "{% set escaped_string = \"<div>This &amp; that</div>\" %}\n" +
      "{{ escaped_string|unescape_html }}"
    ),
  }
)
public class UnescapeHtmlFilter implements Filter {

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    return StringEscapeUtils.unescapeHtml4(Objects.toString(object, ""));
  }

  @Override
  public String getName() {
    return "unescape_html";
  }
}
