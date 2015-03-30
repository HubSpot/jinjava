package com.hubspot.jinjava.lib.tag;

import java.util.Objects;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.TagNode;


@JinjavaDoc(value="Echos the result of the expression", params=@JinjavaParam(value="expr", type="expression"))
public class PrintTag implements Tag {

  @Override
  public String getName() {
    return "print";
  }

  @Override
  public String interpret(TagNode tagNode,
      JinjavaInterpreter interpreter) {
    return Objects.toString( interpreter.resolveELExpression(tagNode.getHelpers(), tagNode.getLineNumber()), "" );
  }

  @Override
  public String getEndTagName() {
    return null;
  }

}
