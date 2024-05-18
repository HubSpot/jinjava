package com.hubspot.jinjava.mode;

import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.lib.expression.EagerExpressionStrategy;
import com.hubspot.jinjava.lib.tag.eager.EagerTagDecorator;
import com.hubspot.jinjava.lib.tag.eager.EagerTagFactory;
import com.hubspot.jinjava.loader.RelativePathResolver;
import java.util.Optional;

public class EagerExecutionMode implements ExecutionMode {

  private static final ExecutionMode INSTANCE = new EagerExecutionMode();

  // These meta context variables should never be removed from the set of meta context variables
  public static final ImmutableSet<String> STATIC_META_CONTEXT_VARIABLES =
    ImmutableSet.of(
      Context.GLOBAL_MACROS_SCOPE_KEY,
      Context.IMPORT_RESOURCE_PATH_KEY,
      Context.DEFERRED_IMPORT_RESOURCE_PATH_KEY,
      Context.IMPORT_RESOURCE_ALIAS_KEY,
      RelativePathResolver.CURRENT_PATH_CONTEXT_KEY
    );

  protected EagerExecutionMode() {}

  public static ExecutionMode instance() {
    return INSTANCE;
  }

  @Override
  public boolean isPreserveRawTags() {
    return true;
  }

  @Override
  public boolean useEagerParser() {
    return true;
  }

  @Override
  public boolean useEagerContextReverting() {
    return true;
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
    context.setExpressionStrategy(new EagerExpressionStrategy());
    context.addMetaContextVariables(STATIC_META_CONTEXT_VARIABLES);
  }
}
