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

import com.google.common.collect.Lists;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.objects.DummyObject;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.ForLoop;
import com.hubspot.jinjava.util.HelperStringTokenizer;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;
import com.hubspot.jinjava.util.ObjectIterator;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;

/**
 * {% for a in b|f1:d,c %}
 * <p>
 * {% for key, value in my_dict.items() %}
 *
 * @author anysome
 */
@JinjavaDoc(
  value = "Outputs the inner content for each item in the given iterable",
  params = {
    @JinjavaParam(
      value = "items_to_iterate",
      desc = "Specifies the name of a single item in the sequence or dict."
    )
  },
  snippets = {
    @JinjavaSnippet(
      code = "{% for item in items %}\n" + "    {{ item }}\n" + "{% endfor %}"
    ),
    @JinjavaSnippet(
      desc = "Iterating over dictionary values",
      code = "{% for value in dictionary %}\n" + "    {{ value }}\n" + "{% endfor %}"
    ),
    @JinjavaSnippet(
      desc = "Iterating over dictionary entries",
      code = "{% for key, value in dictionary.items() %}\n" +
      "    {{ key }}: {{ value }}\n" +
      "{% endfor %}"
    ),
    @JinjavaSnippet(
      desc = "Standard blog listing loop",
      code = "{% for content in contents %}\n" +
      "    Post content variables\n" +
      "{% endfor %}"
    )
  }
)
public class ForTag implements Tag {
  public static final String TAG_NAME = "for";

  private static final long serialVersionUID = 6175143875754966497L;
  private static final String LOOP = "loop";

  @Override
  public boolean isRenderedInValidationMode() {
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    /* apdlv72@gmail.com
     * Fix for issues with for-loops that contain whitespace in their range, e.g.
     * "{% for i in range(1 * 1, 2 * 2) %}"
     * This is because HelperStringTokenizer will split the range expressions also
     * at white spaces and end up with [i, in, range(1, *, 1, 2, *, 2)].
     * To avoid this, the below fix will remove white space from the expression
     * on the right side of the keyword "in". It will do so however only if there
     * are no characters in this expression that indicate strings - namely ' and ".
     * This avoids messing up expressions like {% for i in ['a ','b'] %} that
     * contain spaces in the arguments.
     * TODO A somewhat more sophisticated tokenizing/parsing of the for-loop expression.
     */
    String helpers = tagNode.getHelpers();
    String[] parts = helpers.split("\\s+in\\s+");
    if (parts.length == 2 && !parts[1].contains("'") && !parts[1].contains("\"")) {
      helpers = parts[0] + " in " + parts[1].replace(" ", "");
    }
    List<String> helper = new HelperStringTokenizer(helpers).splitComma(true).allTokens();

    List<String> loopVars = getLoopVars(helper);

    if (loopVars.size() >= helper.size()) {
      throw new TemplateSyntaxException(
        tagNode.getHelpers().trim(),
        "Tag 'for' expects valid 'in' clause, got: " + tagNode.getHelpers(),
        tagNode.getLineNumber(),
        tagNode.getStartPosition()
      );
    }

    String loopExpr = getLoopExpression(helper, loopVars);
    Object collection = interpreter.resolveELExpression(
      loopExpr,
      tagNode.getLineNumber()
    );
    ForLoop loop = ObjectIterator.getLoop(collection);

    try (InterpreterScopeClosable c = interpreter.enterScope()) {
      if (interpreter.isValidationMode() && !loop.hasNext()) {
        loop = ObjectIterator.getLoop(new DummyObject());
        interpreter.getContext().setValidationMode(true);
      }

      interpreter.getContext().put(LOOP, loop);

      LengthLimitingStringBuilder buff = new LengthLimitingStringBuilder(
        interpreter.getConfig().getMaxOutputSize()
      );
      while (loop.hasNext()) {
        Object val = interpreter.wrap(loop.next());

        // set item variables
        if (loopVars.size() == 1) {
          interpreter.getContext().put(loopVars.get(0), val);
        } else {
          for (int loopVarIndex = 0; loopVarIndex < loopVars.size(); loopVarIndex++) {
            String loopVar = loopVars.get(loopVarIndex);
            if (Map.Entry.class.isAssignableFrom(val.getClass())) {
              Map.Entry<String, Object> entry = (Entry<String, Object>) val;
              Object entryVal = null;

              if (loopVars.indexOf(loopVar) == 0) {
                entryVal = entry.getKey();
              } else if (loopVars.indexOf(loopVar) == 1) {
                entryVal = entry.getValue();
              }

              interpreter.getContext().put(loopVar, entryVal);
            } else if (List.class.isAssignableFrom(val.getClass())) {
              List<Object> entries = ((PyList) val).toList();
              Object entryVal = null;
              // safety check for size
              if (entries.size() >= loopVarIndex) {
                entryVal = entries.get(loopVarIndex);
              }
              interpreter.getContext().put(loopVar, entryVal);
            } else {
              try {
                PropertyDescriptor[] valProps = Introspector
                  .getBeanInfo(val.getClass())
                  .getPropertyDescriptors();
                for (PropertyDescriptor valProp : valProps) {
                  if (loopVar.equals(valProp.getName())) {
                    interpreter
                      .getContext()
                      .put(loopVar, valProp.getReadMethod().invoke(val));
                    break;
                  }
                }
              } catch (Exception e) {
                throw new InterpretException(
                  e.getMessage(),
                  e,
                  tagNode.getLineNumber(),
                  tagNode.getStartPosition()
                );
              }
            }
          }
        }

        int numDeferredNodesBefore = interpreter.getContext().getDeferredNodes().size();
        for (Node node : tagNode.getChildren()) {
          if (interpreter.getContext().isValidationMode()) {
            node.render(interpreter);
          } else {
            buff.append(node.render(interpreter));
            if (
              interpreter.getContext().getDeferredNodes().size() > numDeferredNodesBefore
            ) {
              throw new DeferredValueException(
                "for loop",
                interpreter.getLineNumber(),
                interpreter.getPosition()
              );
            }
          }
        }
      }

      return buff.toString();
    }
  }

  public String getLoopExpression(List<String> helper, List<String> loopVars) {
    String loopExpr = StringUtils.join(
      helper.subList(loopVars.size() + 1, helper.size()),
      ","
    );
    return loopExpr;
  }

  public List<String> getLoopVars(List<String> helper) {
    List<String> loopVars = Lists.newArrayList();
    while (loopVars.size() < helper.size()) {
      String val = helper.get(loopVars.size());

      if ("in".equals(val)) {
        break;
      } else {
        loopVars.add(val);
      }
    }
    return loopVars;
  }

  @Override
  public String getName() {
    return TAG_NAME;
  }
}
