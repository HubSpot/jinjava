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

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.doc.annotations.JinjavaTextMateSnippet;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.objects.Namespace;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.DeferredValueUtils;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * {% set primary_line_height = primary_font_size_num*1.5 %}
 *
 * {% set lw_font_size = "font-size: " ~ lw_font_size_base ~ "px; " %} {% set lw_secondary_font_size = "font-size: " ~ (lw_font_size_base - 2) ~ "px; " %} {% set lw_line_height = "line-height: " ~ lw_font_size_base*1.5 ~ "px; " %}
 *
 * @author anysome
 *
 */
@JinjavaDoc(
  value = "Assigns the value or result of a statement to a variable",
  params = {
    @JinjavaParam(
      value = "var",
      type = "variable identifier",
      desc = "The name of the variable"
    ),
    @JinjavaParam(
      value = "expr",
      type = "expression",
      desc = "The value stored in the variable (string, number, boolean, or sequence"
    ),
  },
  snippets = {
    @JinjavaSnippet(
      desc = "Set a variable in with a set statement and print the variable in a expression",
      code = "{% set primaryColor = \"#F7761F\" %}\n" + "{{ primaryColor }}\n"
    ),
    @JinjavaSnippet(
      desc = "You can combine multiple values or variables into a sequence variable",
      code = "{% set var_one = \"String 1\" %}\n" +
      "{% set var_two = \"String 2\" %}\n" +
      "{% set sequence = [var_one,  var_two] %}"
    ),
    @JinjavaSnippet(
      desc = "You can set a value to the string value within a block",
      code = "{% set name = 'Jack' %}\n" +
      "{% set message %}\n" +
      "My name is {{ name }}\n" +
      "{% endset %}"
    ),
  }
)
@JinjavaTextMateSnippet(code = "{% set ${1:var} = ${2:expr} %}")
public class SetTag implements Tag, FlexibleTag {

  public static final String TAG_NAME = "set";
  public static final String IGNORED_VARIABLE_NAME = "__ignored__";

  private static final long serialVersionUID = -8558479410226781539L;

  @Override
  public String getName() {
    return TAG_NAME;
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    if (!tagNode.getHelpers().contains("=")) {
      return interpretBlockSet(tagNode, interpreter);
    }

    int eqPos = tagNode.getHelpers().indexOf('=');
    String var = tagNode.getHelpers().substring(0, eqPos).trim();
    String expr = tagNode.getHelpers().substring(eqPos + 1);

    if (var.length() == 0) {
      throw new TemplateSyntaxException(
        tagNode.getMaster().getImage(),
        "Tag 'set' requires a var name to assign to",
        tagNode.getLineNumber(),
        tagNode.getStartPosition()
      );
    }
    if (StringUtils.isBlank(expr)) {
      throw new TemplateSyntaxException(
        tagNode.getMaster().getImage(),
        "Tag 'set' requires an expression to assign to a var",
        tagNode.getLineNumber(),
        tagNode.getStartPosition()
      );
    }

    String[] varTokens = var.split(",");

    try {
      @SuppressWarnings("unchecked")
      List<?> exprVals = (List<Object>) interpreter.resolveELExpression(
        "[" + expr + "]",
        tagNode.getMaster().getLineNumber()
      );
      executeSet((TagToken) tagNode.getMaster(), interpreter, varTokens, exprVals, false);
    } catch (DeferredValueException e) {
      DeferredValueUtils.deferVariables(varTokens, interpreter.getContext());
      throw e;
    }

    return "";
  }

  private String interpretBlockSet(TagNode tagNode, JinjavaInterpreter interpreter) {
    int filterPos = tagNode.getHelpers().indexOf('|');
    String var = tagNode.getHelpers().trim();
    if (filterPos >= 0) {
      var = tagNode.getHelpers().substring(0, filterPos).trim();
    }
    String result;
    result = renderChildren(tagNode, interpreter, var);
    try {
      executeSetBlock(tagNode, var, result, filterPos >= 0, interpreter);
    } catch (DeferredValueException e) {
      DeferredValueUtils.deferVariables(new String[] { var }, interpreter.getContext());
      throw e;
    }
    return "";
  }

  public static String renderChildren(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    String var
  ) {
    String result;
    if (IGNORED_VARIABLE_NAME.equals(var)) {
      result = renderChildren(tagNode, interpreter);
    } else {
      try (InterpreterScopeClosable c = interpreter.enterScope()) {
        result = renderChildren(tagNode, interpreter);
      }
    }
    return result;
  }

  private static String renderChildren(TagNode tagNode, JinjavaInterpreter interpreter) {
    String result;
    StringBuilder sb = new StringBuilder();
    for (Node child : tagNode.getChildren()) {
      sb.append(child.render(interpreter));
    }
    result = sb.toString();
    return result;
  }

  private void executeSetBlock(
    TagNode tagNode,
    String var,
    String resolvedBlock,
    boolean hasFilterOp,
    JinjavaInterpreter interpreter
  ) {
    String[] varAsArray = new String[] { var };
    executeSet(
      (TagToken) tagNode.getMaster(),
      interpreter,
      varAsArray,
      Collections.singletonList(resolvedBlock),
      false
    );
    if (hasFilterOp) {
      // Evaluate the whole expression to get the filtered result
      Object finalVal = interpreter.resolveELExpression(
        tagNode.getHelpers().trim(),
        tagNode.getMaster().getLineNumber()
      );
      executeSet(
        (TagToken) tagNode.getMaster(),
        interpreter,
        varAsArray,
        Collections.singletonList(finalVal),
        false
      );
    }
  }

  public void executeSet(
    TagToken tagToken,
    JinjavaInterpreter interpreter,
    String[] varTokens,
    List<?> resolvedList,
    boolean allowDeferredValueOverride
  ) {
    if (varTokens.length > 1) {
      // handle multi-variable assignment

      if (resolvedList == null || varTokens.length != resolvedList.size()) {
        throw new TemplateSyntaxException(
          tagToken.getImage(),
          "Tag 'set' declares an uneven number of variables and assigned values",
          tagToken.getLineNumber(),
          tagToken.getStartPosition()
        );
      }

      for (int i = 0; i < varTokens.length; i++) {
        String varItem = varTokens[i].trim();
        if (interpreter.getContext().containsKey(varItem)) {
          if (
            !allowDeferredValueOverride &&
            interpreter.getContext().get(varItem) instanceof DeferredValue
          ) {
            throw new DeferredValueException(varItem);
          }
        }
        interpreter.getContext().put(varItem, resolvedList.get(i));
      }
    } else {
      // handle single variable assignment
      if (interpreter.getContext().containsKey(varTokens[0])) {
        if (
          !allowDeferredValueOverride &&
          interpreter.getContext().get(varTokens[0]) instanceof DeferredValue
        ) {
          throw new DeferredValueException(varTokens[0]);
        }
      }
      setVariable(
        interpreter,
        varTokens[0],
        resolvedList != null && resolvedList.size() > 0 ? resolvedList.get(0) : null
      );
    }
  }

  private void setVariable(JinjavaInterpreter interpreter, String var, Object value) {
    if (var.contains(".")) {
      String[] varArray = var.split("\\.", 2);
      Object namespace = interpreter.getContext().get(varArray[0]);

      if (namespace instanceof Namespace) {
        ((Namespace) namespace).put(varArray[1], value);
        return;
      }
      if (namespace instanceof DeferredValue) {
        throw new DeferredValueException("Deferred Namespace");
      }
    }
    if (!IGNORED_VARIABLE_NAME.equals(var)) {
      interpreter.getContext().put(var, value);
    }
  }

  @Override
  public String getEndTagName() {
    return "endset";
  }

  @Override
  public boolean hasEndTag(TagToken tagToken) {
    return !tagToken.getHelpers().contains("=");
  }
}
