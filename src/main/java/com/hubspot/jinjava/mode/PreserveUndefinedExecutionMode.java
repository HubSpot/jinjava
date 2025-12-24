package com.hubspot.jinjava.mode;

import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.lib.expression.PreserveUndefinedExpressionStrategy;

/**
 * An execution mode that preserves unknown/undefined variables as their original template syntax
 * instead of rendering them as empty strings. This enables multi-pass rendering scenarios where
 * templates are processed in stages with different variable contexts available at each stage.
 *
 * <p>Behavior:
 * <ul>
 *   <li>Expressions with undefined variables are preserved: {@code {{ unknown }}} → {@code {{ unknown }}}</li>
 *   <li>Expressions with defined variables are evaluated: {@code {{ name }}} with {name: "World"} → "World"</li>
 *   <li>Control structures (if/for) with undefined conditions/iterables are preserved</li>
 *   <li>Set tags are preserved with evaluated RHS: {@code {% set x = name %}}
 *       with {name: "World"} → {@code {% set x = 'World' %}}</li>
 *   <li>Macros are executed; undefined variables within macro output are preserved</li>
 *   <li>Variables explicitly set to null are also preserved</li>
 * </ul>
 *
 * <p>This mode extends {@link EagerExecutionMode} to preserve control structures and tags,
 * but uses a custom expression strategy to preserve the original expression syntax
 * instead of internal representations. It enables partial macro evaluation so that
 * macros can execute and produce output with undefined parts preserved.
 */
public class PreserveUndefinedExecutionMode extends EagerExecutionMode {

  private static final ExecutionMode INSTANCE = new PreserveUndefinedExecutionMode();

  protected PreserveUndefinedExecutionMode() {}

  public static ExecutionMode instance() {
    return INSTANCE;
  }

  @Override
  public void prepareContext(Context context) {
    super.prepareContext(context);
    context.setExpressionStrategy(new PreserveUndefinedExpressionStrategy());
    context.setDynamicVariableResolver(varName -> DeferredValue.instance());
    context.setPartialMacroEvaluation(true);
    context.setPreserveResolvedSetTags(true);
    context.setPreserveComments(true);
  }
}
