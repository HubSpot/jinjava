package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.tag.ForTag;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.ChunkResolver;
import com.hubspot.jinjava.util.HelperStringTokenizer;
import java.util.HashSet;
import java.util.List;
import java.util.StringJoiner;

public class EagerForTag extends EagerTagDecorator<ForTag> {

  public EagerForTag() {
    super(new ForTag());
  }

  public EagerForTag(ForTag forTag) {
    super(forTag);
  }

  @Override
  public String getEagerTagImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    List<String> helperTokens = new HelperStringTokenizer(
      ForTag.getWhitespaceAdjustedHelpers(tagToken.getHelpers())
    )
      .splitComma(true)
      .allTokens();
    List<String> loopVars = getTag().getLoopVars(helperTokens);
    if (loopVars.size() >= helperTokens.size()) {
      throw new TemplateSyntaxException(
        tagToken.getHelpers().trim(),
        "Tag 'for' expects valid 'in' clause, got: " + tagToken.getHelpers(),
        tagToken.getLineNumber(),
        tagToken.getStartPosition()
      );
    }

    String loopExpression = getTag().getLoopExpression(helperTokens, loopVars);
    ChunkResolver chunkResolver = new ChunkResolver(
      loopExpression,
      tagToken,
      interpreter
    );

    StringJoiner joiner = new StringJoiner(" ");
    joiner
      .add(tagToken.getSymbols().getExpressionStartWithTag())
      .add(tagToken.getTagName())
      .add(String.join(", ", loopVars))
      .add("in")
      .add(chunkResolver.resolveChunks())
      .add(tagToken.getSymbols().getExpressionEndWithTag());
    String newlyDeferredFunctionImages = reconstructFromContextBeforeDeferring(
      chunkResolver.getDeferredWords(),
      interpreter
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
          chunkResolver.getDeferredWords(),
          new HashSet<>(loopVars)
        )
      );
    return (newlyDeferredFunctionImages + joiner.toString());
  }
}
