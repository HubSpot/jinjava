package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.ForTag;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.HelperStringTokenizer;
import java.util.HashSet;
import java.util.Set;

public class EagerForTag extends EagerTagDecorator<ForTag> {

  public EagerForTag() {
    super(new ForTag());
  }

  public EagerForTag(ForTag forTag) {
    super(forTag);
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    try {
      // We don't want ForTag to throw DeferredValueException
      // when rendering its children increases deferredNodes count.
      return getTag().interpretUnchecked(tagNode, interpreter);
    } catch (DeferredValueException e) {
      return eagerInterpret(tagNode, interpreter);
    }
  }

  @Override
  public String getEagerImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    HelperStringTokenizer tokenizer = new HelperStringTokenizer(tagToken.getHelpers())
    .splitComma(true);
    Set<String> deferredHelpers = new HashSet<>(
      getTag().getLoopVars(tokenizer.allTokens())
    );
    interpreter
      .getContext()
      .handleEagerTagToken(new EagerTagToken(tagToken, deferredHelpers));

    return tagToken.getImage();
  }
}
