package com.hubspot.jinjava.lib.tag;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.doc.annotations.JinjavaTextMateSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import org.apache.commons.lang3.StringUtils;

@JinjavaDoc(
  value = "Evaluates expression without printing out result.",
  snippets = {
    @JinjavaSnippet(code = "{% do list.append('value 2') %}"),
    @JinjavaSnippet(
      desc = "Execute a block of code in the same scope while ignoring the output",
      code = "{% do %}\n" +
      "{% set foo = [] %}\n" +
      "{{ foo.append('a') }}\n" +
      "{% enddo %}"
    )
  }
)
@JinjavaTextMateSnippet(code = "{% do ${1:expr} %}")
public class DoTag implements Tag, FlexibleTag {
  public static final String TAG_NAME = "do";

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    if (hasEndTag((TagToken) tagNode.getMaster())) {
      tagNode.getChildren().forEach(child -> child.render(interpreter));
    } else {
      interpreter.resolveELExpression(tagNode.getHelpers(), tagNode.getLineNumber());
    }
    return "";
  }

  @Override
  public String getName() {
    return TAG_NAME;
  }

  @Override
  public boolean hasEndTag(TagToken tagToken) {
    return StringUtils.isBlank(tagToken.getHelpers());
  }
}
