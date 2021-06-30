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
import com.hubspot.jinjava.doc.annotations.JinjavaHasCodeBody;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.doc.annotations.JinjavaTextMateSnippet;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.objects.DummyObject;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.tree.ExpressionNode;
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
import java.util.Optional;

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
@JinjavaHasCodeBody
@JinjavaTextMateSnippet(
  code = "{% for ${1:items} in ${2:list} %}\n" + "{{ ${1} }}$0\n" + "{% endfor %}"
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
    long numDeferredNodesBefore = interpreter
      .getContext()
      .getDeferredNodes()
      .stream()
      .filter(n -> !(n instanceof ExpressionNode))
      .count();
    String result = interpretUnchecked(tagNode, interpreter);
    if (
      interpreter
        .getContext()
        .getDeferredNodes()
        .stream()
        .filter(n -> !(n instanceof ExpressionNode))
        .count() >
      numDeferredNodesBefore
    ) {
      throw new DeferredValueException(
        "for loop",
        interpreter.getLineNumber(),
        interpreter.getPosition()
      );
    }
    return result;
  }

  public String interpretUnchecked(TagNode tagNode, JinjavaInterpreter interpreter) {
    List<String> helperTokens = new HelperStringTokenizer(tagNode.getHelpers())
      .splitComma(true)
      .allTokens();
    List<String> loopVars = getLoopVars(helperTokens);
    Optional<String> maybeLoopExpr = getLoopExpression(tagNode.getHelpers());

    if (loopVars.size() >= helperTokens.size() || !maybeLoopExpr.isPresent()) {
      throw new TemplateSyntaxException(
        tagNode.getHelpers().trim(),
        "Tag 'for' expects valid 'in' clause, got: " + tagNode.getHelpers(),
        tagNode.getLineNumber(),
        tagNode.getStartPosition()
      );
    }
    String loopExpression = maybeLoopExpr.get();

    Object collection;
    try {
      collection =
        interpreter.resolveELExpression(loopExpression, tagNode.getLineNumber());
    } catch (DeferredParsingException e) {
      throw new DeferredParsingException(
        String.format("%s in %s", String.join(", ", loopVars), e.getDeferredEvalResult())
      );
    }
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

        for (Node node : tagNode.getChildren()) {
          if (interpreter.getContext().isValidationMode()) {
            node.render(interpreter);
          } else {
            try {
              buff.append(node.render(interpreter));
            } catch (OutputTooBigException e) {
              interpreter.addError(TemplateError.fromOutputTooBigException(e));
              return buff.toString();
            }
          }
        }
      }

      return buff.toString();
    }
  }

  public Optional<String> getLoopExpression(String helpers) {
    int inIndex = helpers.indexOf(" in ");
    if (inIndex > 0) {
      return Optional.of(helpers.substring(inIndex + 4).trim());
    }
    return Optional.empty();
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
