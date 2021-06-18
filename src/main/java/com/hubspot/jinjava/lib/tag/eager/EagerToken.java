package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.tree.parse.Token;
import java.util.Collections;
import java.util.Set;

public class EagerToken {
  private final Token token;
  // These words aren't yet DeferredValues, but are unresolved
  // so they should be replaced with DeferredValueImpls if they exist in the context
  private final Set<String> usedDeferredWords;
  // These words are those which will be set to a value which has been deferred.
  private final Set<String> setDeferredWords;

  public EagerToken(Token token, Set<String> usedDeferredWords) {
    this.token = token;
    this.usedDeferredWords = usedDeferredWords;
    this.setDeferredWords = Collections.emptySet();
  }

  public EagerToken(
    Token token,
    Set<String> usedDeferredWords,
    Set<String> setDeferredWords
  ) {
    this.token = token;
    this.usedDeferredWords = usedDeferredWords;
    this.setDeferredWords = setDeferredWords;
  }

  public Token getToken() {
    return token;
  }

  public Set<String> getUsedDeferredWords() {
    return usedDeferredWords;
  }

  public Set<String> getSetDeferredWords() {
    return setDeferredWords;
  }
}
