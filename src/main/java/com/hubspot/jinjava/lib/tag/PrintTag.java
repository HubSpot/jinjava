package com.hubspot.jinjava.lib.tag;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.TagNode;
import java.util.Objects;

@JinjavaDoc(
  value = "Echos the result of the expression",
  params = @JinjavaParam(
    value = "expr",
    type = "expression",
    desc = "Expression to print"
  ),
  snippets = {
    @JinjavaSnippet(
      code = "{% set string_to_echo = \"Print me\" %}\n" + "{% print string_to_echo %}"
    )
  }
)
public class PrintTag implements Tag {
  public static final String TAG_NAME = "print";

  private static final long serialVersionUID = -8613906103187594569L;

  @Override
  public String getName() {
    return TAG_NAME;
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    return Objects.toString(
      interpreter.resolveELExpression(tagNode.getHelpers(), tagNode.getLineNumber()),
      ""
    );
  }

  @Override
  public String getEndTagName() {
    return null;
  }
}
