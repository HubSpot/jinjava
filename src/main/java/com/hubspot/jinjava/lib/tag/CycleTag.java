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
package com.hubspot.jinjava.lib.tag;

import java.util.List;

import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.HelperStringTokenizer;

/**
 * {% cycle a,b,c %} {% cycle a,'b',c as d %} {% cycle d %}
 *
 * @author anysome
 *
 */
public class CycleTag implements Tag {

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
      Integer forindex = (Integer) interpreter.retraceVariable(LOOP_INDEX, tagNode.getLineNumber());
      if (forindex == null) {
        forindex = 0;
      }
      if (values.size() == 1) {
        var = values.get(0);
        values = (List<String>) interpreter.retraceVariable(var, tagNode.getLineNumber());
        if (values == null) {
          return interpreter.resolveString(var, tagNode.getLineNumber());
        }
      } else {
        for (int i = 0; i < values.size(); i++) {
          values.set(i, interpreter.resolveString(values.get(i), tagNode.getLineNumber()));
        }
      }
      return values.get(forindex % values.size());
    } else if (helper.size() == 3) {
      HelperStringTokenizer items = new HelperStringTokenizer(helper.get(0));
      items.splitComma(true);
      values = items.allTokens();
      for (int i = 0; i < values.size(); i++) {
        values.set(i, interpreter.resolveString(values.get(i), tagNode.getLineNumber()));
      }
      var = helper.get(2);
      interpreter.getContext().put(var, values);
      return "";
    } else {
      throw new InterpretException("Tag 'cycle' expects 1 or 3 helper(s) >>> " + helper.size(), tagNode.getLineNumber());
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
