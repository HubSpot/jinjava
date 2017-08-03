/**********************************************************************
 Copyright (c) 2014 HubSpot Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **********************************************************************/
package com.hubspot.jinjava.lib.filter;

import java.util.Locale;
import java.util.Objects;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;

@JinjavaDoc(
    value = "Escapes strings so that they can be safely inserted into a JavaScript variable declaration",
    params = {
        @JinjavaParam(value = "s", desc = "String to escape")
    },
    snippets = {
        @JinjavaSnippet(
            code = "{% set escape_string = \"This string can safely be inserted into JavaScript\" %}\n" +
                "{{ escape_string|escapejs }}")
    })
public class EscapeJsFilter implements Filter {

  @Override
  public Object filter(Object objectToFilter, JinjavaInterpreter jinjavaInterpreter, String... strings) {
    String input = Objects.toString(objectToFilter, "");
    LengthLimitingStringBuilder builder = new LengthLimitingStringBuilder(jinjavaInterpreter.getConfig().getMaxOutputSize());

    for (int i = 0; i < input.length(); i++) {
      char ch = input.charAt(i);

      if (ch > 0xfff) {
        builder.append("\\u");
        builder.append(toHex(ch));
      } else if (ch > 0xff) {
        builder.append("\\u0");
        builder.append(toHex(ch));
      } else if (ch > 0x7f) {
        builder.append("\\u00");
        builder.append(toHex(ch));
      } else if (ch < 32) {
        switch (ch) {
          case '\b' :
            builder.append("\\b");
            break;
          case '\f' :
            builder.append("\\f");
            break;
          case '\n' :
            builder.append("\\n");
            break;
          case '\t' :
            builder.append("\\t");
            break;
          case '\r' :
            builder.append("\\r");
            break;
          default :
            if (ch > 0xf) {
              builder.append("\\u00");
              builder.append(toHex(ch));
            } else {
              builder.append("\\u000");
              builder.append(toHex(ch));
            }
            break;
        }
      } else {
        switch (ch) {
          case '"' :
            builder.append("\\\"");
            break;
          case '\\' :
            builder.append("\\\\");
            break;
          default :
            builder.append(ch);
            break;
        }
      }
    }

    return builder.toString();
  }

  @Override
  public String getName() {
    return "escapejs";
  }

  private String toHex(char ch) {
    return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
  }
}
