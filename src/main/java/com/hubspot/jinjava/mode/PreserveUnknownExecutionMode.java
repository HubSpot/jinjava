package com.hubspot.jinjava.mode;

import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.lib.tag.eager.EagerTagDecorator;
import com.hubspot.jinjava.lib.tag.eager.EagerTagFactory;
import java.util.Optional;

public class PreserveUnknownExecutionMode implements ExecutionMode {

  private static final ExecutionMode INSTANCE = new PreserveUnknownExecutionMode();

  private PreserveUnknownExecutionMode() {}

  public static ExecutionMode instance() {
    return INSTANCE;
  }

  @Override
  public boolean isPreserveRawTags() {
    return true;
  }

  @Override
  public boolean useEagerParser() {
    return false;
  }

  @Override
  public boolean useEagerContextReverting() {
    return false;
  }

  @Override
  public void prepareContext(Context context) {
    context
      .getAllTags()
      .stream()
      .filter(tag -> !(tag instanceof EagerTagDecorator))
      .map(EagerTagFactory::getEagerTagDecorator)
      .filter(Optional::isPresent)
      .forEach(maybeEagerTag -> context.registerTag(maybeEagerTag.get()));

    context.setDynamicVariableResolver(varName -> DeferredValue.instance());
  }
}
