package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.FlexibleTag;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;

@Beta
public class EagerSetTag extends EagerStateChangingTag<SetTag> implements FlexibleTag {

  public EagerSetTag() {
    super(new SetTag());
  }

  public EagerSetTag(SetTag setTag) {
    super(setTag);
  }

  @Override
  public String eagerInterpret(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    InterpretException e
  ) {
    if (tagNode.getHelpers().contains("=")) {
      return EagerInlineSetTagStrategy.INSTANCE.run(
        new TagNode(
          getTag(),
          buildToken(tagNode, e, interpreter.getLineNumber(), interpreter.getPosition()),
          tagNode.getSymbols()
        ),
        interpreter
      );
    }
    return EagerBlockSetTagStrategy.INSTANCE.run(tagNode, interpreter);
  }

  @Override
  public boolean hasEndTag(TagToken tagToken) {
    return getTag().hasEndTag(tagToken);
  }
}
