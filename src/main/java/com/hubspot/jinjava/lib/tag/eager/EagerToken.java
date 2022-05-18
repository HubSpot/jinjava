package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.CallStack;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
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

  private final CallStack currentPathStack;

  private final String importResourcePath;
  private final String currentMacroFunction;

  public EagerToken(Token token, Set<String> usedDeferredWords) {
    this.token = token;
    this.usedDeferredWords = usedDeferredWords;
    this.setDeferredWords = Collections.emptySet();
    importResourcePath = acquireImportResourcePath();
    currentMacroFunction = acquireCurrentMacroFunction();
    currentPathStack = acquireCurrentPathStack();
  }

  public EagerToken(
    Token token,
    Set<String> usedDeferredWords,
    Set<String> setDeferredWords
  ) {
    this.token = token;
    this.usedDeferredWords = usedDeferredWords;
    this.setDeferredWords = setDeferredWords;
    importResourcePath = acquireImportResourcePath();
    currentMacroFunction = acquireCurrentMacroFunction();
    currentPathStack = acquireCurrentPathStack();
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

  public String getImportResourcePath() {
    return importResourcePath;
  }

  public String getCurrentMacroFunction() {
    return currentMacroFunction;
  }

  public CallStack getCurrentPathStack() {
    return currentPathStack;
  }

  private static String acquireImportResourcePath() {
    return (String) JinjavaInterpreter
      .getCurrentMaybe()
      .map(interpreter -> interpreter.getContext().get(Context.IMPORT_RESOURCE_PATH_KEY))
      .filter(path -> path instanceof String)
      .orElse(null);
  }

  private static String acquireCurrentMacroFunction() {
    return JinjavaInterpreter
      .getCurrentMaybe()
      .flatMap(interpreter -> interpreter.getContext().getMacroStack().peek())
      .orElse(null);
  }

  private static CallStack acquireCurrentPathStack() {
    return JinjavaInterpreter
      .getCurrentMaybe()
      .map(interpreter -> interpreter.getContext().getCurrentPathStack())
      .orElse(null);
  }
}
