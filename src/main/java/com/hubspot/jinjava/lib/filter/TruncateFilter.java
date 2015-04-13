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

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;

@JinjavaDoc(
    value="Return a truncated copy of the string. The length is specified with the first parameter which defaults to 255. " +
          "If the second parameter is true the filter will cut the text at length. Otherwise it will discard the last word. " +
          "If the text was in fact truncated it will append an ellipsis sign (\"...\"). If you want a different ellipsis sign " +
          "than \"...\" you can specify it using the third parameter.",
    params={
        @JinjavaParam("s"),
        @JinjavaParam(value="length", type="number", defaultValue="255"),
        @JinjavaParam(value="killwords", type="boolean", defaultValue="False", desc="if true, will cut text at length"),
        @JinjavaParam(value="end", defaultValue="...")
    },
    snippets={
        @JinjavaSnippet(
            code="{{ \"foo bar baz\"|truncate(9) }}",
            output="\"foo ...\""),
        @JinjavaSnippet(
            code="{{ \"foo bar baz\"|truncate(9, True) }}",
            output="\"foo ba...\""),
    })
public class TruncateFilter implements Filter {

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    return Functions.truncate(object, (Object[]) arg);
  }

  @Override
  public String getName() {
    return "truncate";
  }

}
