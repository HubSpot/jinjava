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
package com.hubspot.jinjava.lib.tag;

import java.util.List;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.HelperStringTokenizer;

/**
 * {% cycle a,b,c %} {% cycle a,'b',c as d %} {% cycle d %}
 *
 * @author anysome
 *
 */

@JinjavaDoc(
    value = "The cycle tag can be used within a for loop to cycle through a series of string values and print them with each iteration",
    params = {
        @JinjavaParam(value = "string_to_print", desc = "A comma separated list of strings to print with each interation. The list will repeat if there are more iterations than string parameter values.")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "In the example below, a class of \"odd\" and \"even\" and even are applied to posts in a listing",
            code = "{% for content in contents %}\n" +
                "    <div class=\"post-item {% cycle \'odd\',\'even\' %}\">Blog post content</div>\n" +
                "{% endfor %}"),
    })
public class CycleTag implements Tag {

  private static final long serialVersionUID = 9145890505287556784L;
  private static final String LOOP_INDEX = "loop.index0";
  private static final String TAGNAME = "cycle";

  @SuppressWarnings("unchecked")
  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    List<String> values;
    String var = null;
    HelperStringTokenizer tk = new HelperStringTokenizer(tagNode.getHelpers());

    List<String> helper = tk.allTokens();
    if (helper.size() == 1) {
      HelperStringTokenizer items = new HelperStringTokenizer(helper.get(0));
      items.splitComma(true);
      values = items.allTokens();
      Integer forindex = (Integer) interpreter.retraceVariable(LOOP_INDEX, tagNode.getLineNumber(), tagNode.getStartPosition());
      if (forindex == null) {
        forindex = 0;
      }
      if (values.size() == 1) {
        var = values.get(0);
        values = (List<String>) interpreter.retraceVariable(var, tagNode.getLineNumber(), tagNode.getStartPosition());
        if (values == null) {
          return interpreter.resolveString(var, tagNode.getLineNumber(), tagNode.getStartPosition());
        }
      } else {
        for (int i = 0; i < values.size(); i++) {
          values.set(i, interpreter.resolveString(values.get(i), tagNode.getLineNumber(), tagNode.getStartPosition()));
        }
      }
      return values.get(forindex % values.size());
    } else if (helper.size() == 3) {
      HelperStringTokenizer items = new HelperStringTokenizer(helper.get(0));
      items.splitComma(true);
      values = items.allTokens();
      for (int i = 0; i < values.size(); i++) {
        values.set(i, interpreter.resolveString(values.get(i), tagNode.getLineNumber(), tagNode.getStartPosition()));
      }
      var = helper.get(2);
      interpreter.getContext().put(var, values);
      return "";
    } else {
      throw new TemplateSyntaxException(tagNode.getMaster().getImage(), "Tag 'cycle' expects 1 or 3 helper(s), was: " + helper.size(), tagNode.getLineNumber(), tagNode.getStartPosition());
    }
  }

  @Override
  public String getEndTagName() {
    return null;
  }

  @Override
  public String getName() {
    return TAGNAME;
  }

}
