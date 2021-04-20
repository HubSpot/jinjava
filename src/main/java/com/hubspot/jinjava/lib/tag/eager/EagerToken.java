package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.tree.parse.Token;
import java.util.Collections;
import java.util.Set;

public class EagerToken {
  private Token token;
  private Set<String> usedDeferredWords;
  private Set<String> setDeferredWords;

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
