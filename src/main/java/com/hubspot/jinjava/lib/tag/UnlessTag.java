package com.hubspot.jinjava.lib.tag;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaHasCodeBody;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.ObjectTruthValue;

/**
 * Unless is a conditional just like 'if' but works on the inverse logic.
 *
 * {% unless x &lt; 0 %} x is greater than zero {% endunless %}
 *
 *
 * @author jstehler
 */

@JinjavaDoc(
  value = "Unless is a conditional just like 'if' but works on the inverse logic.",
  params = @JinjavaParam(
    value = "expr",
    type = "expression",
    desc = "Condition to evaluate"
  ),
  snippets = @JinjavaSnippet(
    code = "{% unless x < 0 %} x is greater than zero {% endunless %}"
  )
)
@JinjavaHasCodeBody
public class UnlessTag extends IfTag {
  public static final String TAG_NAME = "unless";

  private static final long serialVersionUID = 1562284758153763419L;

  @Override
  public String getName() {
    return TAG_NAME;
  }

  @Override
  protected boolean isPositiveIfElseNode(
    TagNode tagNode,
    JinjavaInterpreter interpreter
  ) {
    if (tagNode.getName().equals("unless")) {
      return !ObjectTruthValue.evaluate(
        interpreter.resolveELExpression(tagNode.getHelpers(), tagNode.getLineNumber())
      );
    }

    return super.isPositiveIfElseNode(tagNode, interpreter);
  }
}
