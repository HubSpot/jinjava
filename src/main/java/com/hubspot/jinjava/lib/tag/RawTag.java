package com.hubspot.jinjava.lib.tag;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;
import org.apache.commons.lang3.StringUtils;

@JinjavaDoc(
  value = "Process all inner expressions as plain text",
  snippets = {
    @JinjavaSnippet(
      code = "{% raw %}\n" +
      "    The personalization token for a contact's first name is {{ contact.firstname }}\n" +
      "{% endraw %}"
    )
  }
)
public class RawTag implements Tag {
  public static final String TAG_NAME = "raw";

  private static final long serialVersionUID = -6963360187396753883L;

  @Override
  public String getName() {
    return TAG_NAME;
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    LengthLimitingStringBuilder result = new LengthLimitingStringBuilder(
      interpreter.getConfig().getMaxOutputSize()
    );
    if (
      interpreter.getConfig().getExecutionMode().isPreserveRawTags() &&
      !interpreter.getContext().isUnwrapRawOverride()
    ) {
      result.append(
        String.format(
          "%s raw %s",
          tagNode.getSymbols().getExpressionStartWithTag(),
          tagNode.getSymbols().getExpressionEndWithTag()
        )
      );
    }

    for (Node n : tagNode.getChildren()) {
      result.append(renderNodeRaw(n));
    }

    if (
      interpreter.getConfig().getExecutionMode().isPreserveRawTags() &&
      !interpreter.getContext().isUnwrapRawOverride()
    ) {
      result.append(
        String.format(
          "%s endraw %s",
          tagNode.getSymbols().getExpressionStartWithTag(),
          tagNode.getSymbols().getExpressionEndWithTag()
        )
      );
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
        result.append(
          String.format(
            "%s %s %s",
            t.getSymbols().getExpressionStartWithTag(),
            t.getEndName(),
            t.getSymbols().getExpressionEndWithTag()
          )
        );
      }
    }

    return result.toString();
  }
}
