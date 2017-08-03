package com.hubspot.jinjava.lib.tag;

import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;

@JinjavaDoc(
    value = "Process all inner HubL as plain text",
    snippets = {
        @JinjavaSnippet(
            code = "{% raw %}\n" +
                "    The personalization token for a contact's first name is {{ contact.firstname }}\n" +
                "{% endraw %}"
        ),
    })
public class RawTag implements Tag {
  private static final long serialVersionUID = -6963360187396753883L;

  @Override
  public String getName() {
    return "raw";
  }

  @Override
  public String getEndTagName() {
    return "endraw";
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    LengthLimitingStringBuilder result = new LengthLimitingStringBuilder(interpreter.getConfig().getMaxOutputSize());

    for (Node n : tagNode.getChildren()) {
      result.append(renderNodeRaw(n));
    }

    return result.toString();
  }

  public String renderNodeRaw(Node n) {
    StringBuilder result = new StringBuilder(n.getMaster().getImage());

    for (Node child : n.getChildren()) {
      result.append(renderNodeRaw(child));
    }

    if (TagNode.class.isAssignableFrom(n.getClass())) {
      TagNode t = (TagNode) n;
      if (StringUtils.isNotBlank(t.getEndName())) {
        result.append("{% ").append(t.getEndName()).append(" %}");
      }
    }

    return result.toString();
  }

}
