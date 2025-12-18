package com.hubspot.jinjava.mode;

import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DynamicVariableResolver;

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
 *   <li>Set tags with undefined RHS are preserved; with defined RHS, tag is preserved with evaluated value</li>
 *   <li>Variables explicitly set to null are also preserved</li>
 * </ul>
 *
 * <p>This mode extends {@link EagerExecutionMode} and leverages its infrastructure by setting up
 * a {@link DynamicVariableResolver} that returns {@link DeferredValue}
 * for any undefined or null variable, triggering the eager machinery to preserve the original syntax.
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
    context.setDynamicVariableResolver(varName -> DeferredValue.instance());
  }
}
