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
import java.util.Map;

@JinjavaDoc(
  value = "Return a truncated copy of the string. The length is specified with the first parameter which defaults to 255. " +
  "If the second parameter is true the filter will cut the text at length. Otherwise it will discard the last word. " +
  "If the text was in fact truncated it will append an ellipsis sign (\"...\"). If you want a different ellipsis sign " +
  "than \"...\" you can specify it using the third parameter.",
  input = @JinjavaParam(
    value = "string",
    desc = "The string to truncate",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = TruncateFilter.LENGTH_PARAM,
      type = "int",
      defaultValue = "255",
      desc = "Specifies the length at which to truncate the text (includes HTML characters)"
    ),
    @JinjavaParam(
      value = TruncateFilter.KILLWORDS_PARAM,
      type = "boolean",
      defaultValue = "False",
      desc = "If true, the string will cut text at length"
    ),
    @JinjavaParam(
      value = TruncateFilter.END_PARAM,
      defaultValue = "...",
      desc = "The characters that will be added to indicate where the text was truncated"
    )
  },
  snippets = {
    @JinjavaSnippet(
      code = "{{ \"I only want to show the first sentence. Not the second.\"|truncate(40) }} ",
      output = "I only want to show the first sentence."
    ),
    @JinjavaSnippet(
      code = "{{ \"I only want to show the first sentence. Not the second.\"|truncate(35, True, '..') }}",
      output = "I only want to show the first sente.."
    )
  }
)
public class TruncateFilter extends AbstractFilter implements Filter {
  public static final String LENGTH_PARAM = "length";
  public static final String KILLWORDS_PARAM = "killwords";
  public static final String END_PARAM = "end";

  @Override
  public Object filter(
    Object object,
    JinjavaInterpreter interpreter,
    Map<String, Object> parsedArgs
  ) {
    int length = (int) parsedArgs.get(LENGTH_PARAM);
    boolean killwords = (boolean) parsedArgs.get(KILLWORDS_PARAM);
    String end = (String) parsedArgs.get(END_PARAM);
    return Functions.truncate(object, length, killwords, end);
  }

  @Override
  public String getName() {
    return "truncate";
  }
}
