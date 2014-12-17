package com.hubspot.jinjava.lib.tag;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.ObjectTruthValue;

/**
 * Unless is a conditional just like 'if' but works on the inverse logic.
 * 
 *    {% unless x < 0 %} x is greater than zero {% endunless %}
 *
 *
 * @author jstehler
 */
public class UnlessTag extends IfTag {

  @Override
  public String getName() {
    return "unless";
  }
  
  @Override
  public String getEndTagName() {
    return "endunless";
  }
  
  @Override
  protected boolean evaluateIfElseTagNode(TagNode tagNode, JinjavaInterpreter interpreter) {
    if(tagNode.getName().equals("unless")) {
      return !ObjectTruthValue.evaluate(interpreter.resolveELExpression(tagNode.getHelpers(), tagNode.getLineNumber()));
    }

    return super.evaluateIfElseTagNode(tagNode, interpreter);
  }
  
}
