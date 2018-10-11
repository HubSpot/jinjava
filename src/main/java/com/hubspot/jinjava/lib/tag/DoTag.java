package com.hubspot.jinjava.lib.tag;

import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.tree.TagNode;

@JinjavaDoc(
    value = "Evaluates expression without printing out result.",
    snippets = {
        @JinjavaSnippet(
            code = "{% do list.append('value 2') %}")
    })
public class DoTag implements Tag {

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {

    if (StringUtils.isBlank(tagNode.getHelpers())) {
      throw new TemplateSyntaxException(tagNode.getMaster().getImage(), "Tag 'do' expects expression", tagNode.getLineNumber(), tagNode.getStartPosition());
    }

    interpreter.resolveELExpression(tagNode.getHelpers(), tagNode.getLineNumber());
    return "";
  }

  @Override
  public String getEndTagName() {
    return null;
  }

  @Override
  public String getName() {
    return "do";
  }
}
