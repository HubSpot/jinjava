package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.DoTag;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.ChunkResolver;
import java.util.StringJoiner;

public class EagerDoTag extends EagerStateChangingTag<DoTag> {

  public EagerDoTag() {
    super(new DoTag());
  }

  public EagerDoTag(DoTag doTag) {
    super(doTag);
  }

  @Override
  public String getEagerTagImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    String expr = tagToken.getHelpers();
    ChunkResolver chunkResolver = new ChunkResolver(expr, tagToken, interpreter);
    EagerStringResult resolvedExpression = executeInChildContext(
      eagerInterpreter -> chunkResolver.resolveChunks(),
      interpreter,
      true
    );
    StringJoiner joiner = new StringJoiner(" ");
    joiner
      .add(tagToken.getSymbols().getExpressionStartWithTag())
      .add(tagToken.getTagName())
      .add(resolvedExpression.getResult())
      .add(tagToken.getSymbols().getExpressionEndWithTag());
    StringBuilder prefixToPreserveState = new StringBuilder(
      interpreter.getContext().isProtectedMode()
        ? resolvedExpression.getPrefixToPreserveState()
        : ""
    );
    if (chunkResolver.getDeferredWords().isEmpty()) {
      // Possible macro/set tag in front of this one. Omits result
      return prefixToPreserveState.toString();
    }
    prefixToPreserveState.append(
      reconstructFromContextBeforeDeferring(chunkResolver.getDeferredWords(), interpreter)
    );
    interpreter
      .getContext()
      .handleEagerToken(
        new EagerToken(
          new TagToken(
            joiner.toString(),
            tagToken.getLineNumber(),
            tagToken.getStartPosition(),
            tagToken.getSymbols()
          ),
          chunkResolver.getDeferredWords()
        )
      );
    // Possible set tag in front of this one.
    return wrapInAutoEscapeIfNeeded(
      prefixToPreserveState.toString() + joiner.toString(),
      interpreter
    );
  }
}
