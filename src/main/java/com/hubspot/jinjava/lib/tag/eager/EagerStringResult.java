package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.util.ChunkResolver.ResolvedExpression;

/**
 * This represents the result of executing an expression, where if something got
 * deferred, then the <code>prefixToPreserveState</code> can be added to the output
 * that would preserve the state for a second pass.
 */
public class EagerStringResult {
  private final ResolvedExpression result;
  private final String prefixToPreserveState;

  public EagerStringResult(ResolvedExpression result) {
    this.result = result;
    this.prefixToPreserveState = "";
  }

  public EagerStringResult(ResolvedExpression result, String prefixToPreserveState) {
    this.result = result;
    this.prefixToPreserveState = prefixToPreserveState;
  }

  public ResolvedExpression getResult() {
    return result;
  }

  public String getPrefixToPreserveState() {
    return prefixToPreserveState;
  }

  @Override
  public String toString() {
    return prefixToPreserveState + result;
  }
}
