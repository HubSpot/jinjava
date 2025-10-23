package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.ContinueTag;
import com.hubspot.jinjava.lib.tag.ForTag;
import com.hubspot.jinjava.tree.parse.TagToken;

/**
 * Eager decorator for the continue tag that handles reconstruction when the continue
 * is inside a deferred context (e.g., when in deferred execution mode such as
 * inside a deferred if condition within a for loop).
 */
@Beta
public class EagerContinueTag extends EagerTagDecorator<ContinueTag> {

  public EagerContinueTag() {
    super(new ContinueTag());
  }

  public EagerContinueTag(ContinueTag continueTag) {
    super(continueTag);
  }

  @Override
  public String getEagerTagImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    if (!(interpreter.getContext().get(ForTag.LOOP) instanceof DeferredValue)) {
      interpreter.getContext().replace(ForTag.LOOP, DeferredValue.instance());
    }
    return super.getEagerTagImage(tagToken, interpreter);
  }
}
