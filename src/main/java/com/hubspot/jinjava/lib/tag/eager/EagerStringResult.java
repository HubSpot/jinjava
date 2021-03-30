package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.util.ChunkResolver.ResolvedChunks;

/**
 * This represents the result of executing an expression, where if something got
 * deferred, then the <code>prefixToPreserveState</code> can be added to the output
 * that would preserve the state for a second pass.
 */
public class EagerStringResult {
  private final ResolvedChunks result;
  private final String prefixToPreserveState;

  public EagerStringResult(ResolvedChunks result) {
    this.result = result;
    this.prefixToPreserveState = "";
  }

  public EagerStringResult(ResolvedChunks result, String prefixToPreserveState) {
    this.result = result;
    this.prefixToPreserveState = prefixToPreserveState;
  }

  public ResolvedChunks getResult() {
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
