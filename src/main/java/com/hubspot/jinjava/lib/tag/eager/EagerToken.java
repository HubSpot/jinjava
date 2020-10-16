package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.tree.parse.Token;
import java.util.Set;

public class EagerToken {
  private Token token;
  private Set<String> deferredHelpers;

  public EagerToken(Token token, Set<String> deferredHelpers) {
    this.token = token;
    this.deferredHelpers = deferredHelpers;
  }

  public Token getToken() {
    return token;
  }

  public Set<String> getDeferredHelpers() {
    return deferredHelpers;
  }
}
