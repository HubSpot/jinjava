package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.EagerValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.tag.ElseIfTag;
import com.hubspot.jinjava.lib.tag.ElseTag;
import com.hubspot.jinjava.lib.tag.IfTag;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;
import java.util.Iterator;
import org.apache.commons.lang3.StringUtils;

public class EagerIfTag extends EagerTagDecorator<IfTag> {

  public EagerIfTag() {
    super(new IfTag());
  }

  @Override
  public String eagerInterpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    if (StringUtils.isBlank(tagNode.getHelpers())) {
      throw new TemplateSyntaxException(
        interpreter,
        tagNode.getMaster().getImage(),
        "Tag 'if' expects expression"
      );
    }

    LengthLimitingStringBuilder eagerImage = new LengthLimitingStringBuilder(
      interpreter.getConfig().getMaxOutputSize()
    );

    eagerImage.append(tagNode.getMaster().getImage());

    Iterator<Node> nodeIterator = tagNode.getChildren().iterator();

    boolean parentValidationMode = interpreter.getContext().isValidationMode();

    try {
      while (nodeIterator.hasNext()) {
        if (interpreter.isValidationMode() && !parentValidationMode) {
          interpreter.getContext().setValidationMode(false);
        }

        Node node = nodeIterator.next();
        if (TagNode.class.isAssignableFrom(node.getClass())) {
          TagNode tag = (TagNode) node;
          if (
            tag.getName().equals(ElseIfTag.TAG_NAME) ||
            tag.getName().equals(ElseTag.TAG_NAME)
          ) {
            eagerImage.append(tag.getMaster().getImage());
            continue;
          }
        }
        eagerImage.append(node.render(interpreter));
      }
    } finally {
      interpreter.getContext().setValidationMode(parentValidationMode);
    }

    eagerImage.append(String.format("{%% %s %%}", tagNode.getEndName()));

    return eagerImage.toString();
  }

  @Override
  public void handleEagerValueException(
    EagerValueException e,
    TagNode tagNode,
    JinjavaInterpreter interpreter
  ) {}
}
