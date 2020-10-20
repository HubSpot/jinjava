package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.tag.ForTag;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.ChunkResolver;
import com.hubspot.jinjava.util.HelperStringTokenizer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public class EagerForTag extends EagerTagDecorator<ForTag> {

  public EagerForTag() {
    super(new ForTag());
  }

  public EagerForTag(ForTag forTag) {
    super(forTag);
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    try {
      return getTag().interpret(tagNode, interpreter);
    } catch (DeferredValueException e) {
      return eagerInterpret(tagNode, interpreter);
    }
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
    StringJoiner joiner = new StringJoiner(" ");
    joiner
      .add(tagToken.getSymbols().getExpressionStartWithTag())
      .add(tagToken.getTagName());
    joiner.add(String.join(", ", loopVars));
    Set<String> deferredVariables = new HashSet<>(loopVars);
    joiner.add("in");

    String loopExpression = getTag().getLoopExpression(helperTokens, loopVars);
    ChunkResolver chunkResolver = new ChunkResolver(loopExpression, tagToken, interpreter)
    .useMiniChunks(true);
    joiner.add(chunkResolver.resolveChunks());
    deferredVariables.addAll(chunkResolver.getDeferredVariables());

    interpreter
      .getContext()
      .handleEagerToken(new EagerToken(tagToken, deferredVariables));
    joiner.add(tagToken.getSymbols().getExpressionEndWithTag());

    return joiner.toString();
  }
}
