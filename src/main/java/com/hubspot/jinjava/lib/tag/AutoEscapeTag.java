package com.hubspot.jinjava.lib.tag;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;

@JinjavaDoc(
    value = "Autoescape the tag's contents",
    hidden = true,
    snippets = {
        @JinjavaSnippet(
            code = "{% autoescape %}\n" +
                "<div>Code to escape</div>\n" +
                "{% endautoescape %}")
    })
public class AutoEscapeTag implements Tag {
  private static final long serialVersionUID = 786006577642541285L;

  @Override
  public String getName() {
    return "autoescape";
  }

  @Override
  public String getEndTagName() {
    return "endautoescape";
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    try (InterpreterScopeClosable c = interpreter.enterScope()) {
      String boolFlagStr = StringUtils.trim(tagNode.getHelpers());
      boolean escapeFlag = BooleanUtils.toBoolean(StringUtils.isNotBlank(boolFlagStr) ? boolFlagStr : "true");
      interpreter.getContext().setAutoEscape(escapeFlag);

      LengthLimitingStringBuilder result = new LengthLimitingStringBuilder(interpreter.getConfig().getMaxOutputSize());

      for (Node child : tagNode.getChildren()) {
        result.append(child.render(interpreter));
      }

      return result.toString();
    }
  }

}
