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

import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.TagNode;

/**
 * {% set primary_line_height = primary_font_size_num*1.5 %}
 * 
 * {% set lw_font_size = "font-size: " ~ lw_font_size_base ~ "px; " %}
 * {% set lw_secondary_font_size = "font-size: " ~ (lw_font_size_base - 2) ~ "px; " %}
 * {% set lw_line_height = "line-height: " ~ lw_font_size_base*1.5 ~ "px; " %}
 * 
 * @author anysome
 * 
 */
public class SetTag implements Tag {

  private static final String TAGNAME = "set";

  @Override
  public String getName() {
    return TAGNAME;
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    if(!tagNode.getHelpers().contains("=")) {
      throw new InterpretException("Tag 'set' expects an assignment expression with '=', but was: " + tagNode.getHelpers(), tagNode.getLineNumber());
    }

    int eqPos = tagNode.getHelpers().indexOf('=');
    String var = tagNode.getHelpers().substring(0, eqPos).trim();
    String expr = tagNode.getHelpers().substring(eqPos + 1, tagNode.getHelpers().length());

    if(var == null || var.length() == 0) {
      throw new InterpretException("Tag 'set' requires a var name to assign to", tagNode.getLineNumber());
    }
    if(StringUtils.isBlank(expr)) {
      throw new InterpretException("Tag 'set' requires an expression to assign to a var", tagNode.getLineNumber());
    }
    
    Object val = interpreter.resolveELExpression(expr, tagNode.getLineNumber());
    interpreter.getContext().put(var, val);

    return "";
  }

  @Override
  public String getEndTagName() {
    return null;
  }

}
