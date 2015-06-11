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

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.HelperStringTokenizer;
import com.hubspot.jinjava.util.ObjectIterator;

/**
 * {% for a in b|f1:d,c %}
 *
 * {% for key, value in my_dict %}
 *
 * @author anysome
 *
 */
@JinjavaDoc(
    value = "Outputs the inner content for each item in the given iterable",
    params = {
        @JinjavaParam(value = "items_to_iterate", desc = "Specifies the name of a single item in the sequence or dict."),
    },
    snippets = {
        @JinjavaSnippet(
            code = "{% for item in items %}\n" +
                "    {{ item }}\n" +
                "{% endfor %}"),
        @JinjavaSnippet(
            desc = "Standard blog listing loop",
            code = "{% for content in contents %}\n" +
                "    Post content variables\n" +
                "{% endfor %}")
    })
public class ForTag implements Tag {

  private static final long serialVersionUID = 6175143875754966497L;
  private static final String LOOP = "loop";
  private static final String TAGNAME = "for";
  private static final String ENDTAGNAME = "endfor";

  @SuppressWarnings("unchecked")
  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    List<String> helper = new HelperStringTokenizer(tagNode.getHelpers()).splitComma(true).allTokens();

    List<String> loopVars = Lists.newArrayList();
    int inPos = 0;
    while (inPos < helper.size()) {
      String val = helper.get(inPos);

      if ("in".equals(val)) {
        break;
      }
      else {
        loopVars.add(val);
        inPos++;
      }
    }

    if (inPos >= helper.size()) {
      throw new InterpretException("Tag 'for' expects valid 'in' clause, got: " + tagNode.getHelpers(), tagNode.getLineNumber());
    }

    String loopExpr = StringUtils.join(helper.subList(inPos + 1, helper.size()), ",");
    Object collection = interpreter.resolveELExpression(loopExpr, tagNode.getLineNumber());
    ForLoop loop = ObjectIterator.getLoop(collection);

    interpreter.enterScope();
    try {
      interpreter.getContext().put(LOOP, loop);

      StringBuilder buff = new StringBuilder();
      while (loop.hasNext()) {
        Object val = loop.next();

        // set item variables
        if (loopVars.size() == 1) {
          interpreter.getContext().put(loopVars.get(0), val);
        }
        else {
          for (String loopVar : loopVars) {
            if (Map.Entry.class.isAssignableFrom(val.getClass())) {
              Map.Entry<String, Object> entry = (Entry<String, Object>) val;
              Object entryVal = null;

              if ("key".equals(loopVar)) {
                entryVal = entry.getKey();
              }
              else if ("value".equals(loopVar)) {
                entryVal = entry.getValue();
              }

              interpreter.getContext().put(loopVar, entryVal);
            }
            else {
              try {
                PropertyDescriptor[] valProps = Introspector.getBeanInfo(val.getClass()).getPropertyDescriptors();
                for (PropertyDescriptor valProp : valProps) {
                  if (loopVar.equals(valProp.getName())) {
                    interpreter.getContext().put(loopVar, valProp.getReadMethod().invoke(val));
                    break;
                  }
                }
              } catch (Exception e) {
                throw new InterpretException(e.getMessage(), e, tagNode.getLineNumber());
              }
            }
          }
        }

        for (Node node : tagNode.getChildren()) {
          buff.append(node.render(interpreter));
        }
      }

      return buff.toString();
    } finally {
      interpreter.leaveScope();
    }

  }

  @Override
  public String getEndTagName() {
    return ENDTAGNAME;
  }

  @Override
  public String getName() {
    return TAGNAME;
  }

}
