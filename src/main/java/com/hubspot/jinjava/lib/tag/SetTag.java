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
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.tree.TagNode;
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
    )
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
    )
  }
)
public class SetTag implements Tag {
  public static final String TAG_NAME = "set";

  private static final long serialVersionUID = -8558479410226781539L;

  @Override
  public String getName() {
    return TAG_NAME;
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    if (!tagNode.getHelpers().contains("=")) {
      throw new TemplateSyntaxException(
        tagNode.getMaster().getImage(),
        "Tag 'set' expects an assignment expression with '=', but was: " +
        tagNode.getHelpers(),
        tagNode.getLineNumber(),
        tagNode.getStartPosition()
      );
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
      if (varTokens.length > 1) {
        // handle multi-variable assignment
        @SuppressWarnings("unchecked")
        List<Object> exprVals = (List<Object>) interpreter.resolveELExpression(
          "[" + expr + "]",
          tagNode.getLineNumber()
        );

        if (varTokens.length != exprVals.size()) {
          throw new TemplateSyntaxException(
            tagNode.getMaster().getImage(),
            "Tag 'set' declares an uneven number of variables and assigned values",
            tagNode.getLineNumber(),
            tagNode.getStartPosition()
          );
        }

        for (int i = 0; i < varTokens.length; i++) {
          String varItem = varTokens[i].trim();
          interpreter.getContext().put(varItem, exprVals.get(i));
        }
      } else {
        // handle single variable assignment
        interpreter
          .getContext()
          .put(var, interpreter.resolveELExpression(expr, tagNode.getLineNumber()));
      }
    } catch (DeferredValueException e) {
      for (String varToken : varTokens) {
        String key = varToken.trim();
        Object originalValue = interpreter.getContext().get(key);
        if (originalValue != null) {
          if (originalValue instanceof DeferredValue) {
            interpreter.getContext().put(key, originalValue);
          } else {
            interpreter.getContext().put(key, DeferredValue.instance(originalValue));
          }
        } else {
          interpreter.getContext().put(key, DeferredValue.instance());
        }
      }
      throw e;
    }

    return "";
  }

  @Override
  public String getEndTagName() {
    return null;
  }
}
