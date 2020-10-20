package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.ChunkResolver;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class EagerSetTag extends EagerTagDecorator<SetTag> {

  public EagerSetTag() {
    super(new SetTag());
  }

  public EagerSetTag(SetTag setTag) {
    super(setTag);
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    if (interpreter.getContext().isEagerMode()) {
      // Preserve set tags when eagerly executing nodes.
      return eagerInterpret(tagNode, interpreter);
    } else {
      return super.interpret(tagNode, interpreter);
    }
  }

  @Override
  public String getEagerTagImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    if (!tagToken.getHelpers().contains("=")) {
      throw new TemplateSyntaxException(
        tagToken.getImage(),
        "Tag 'set' expects an assignment expression with '=', but was: " +
        tagToken.getHelpers(),
        tagToken.getLineNumber(),
        tagToken.getStartPosition()
      );
    }
    StringJoiner joiner = new StringJoiner(" ");
    joiner
      .add(tagToken.getSymbols().getExpressionStartWithTag())
      .add(tagToken.getTagName());

    int eqPos = tagToken.getHelpers().indexOf('=');
    String var = tagToken.getHelpers().substring(0, eqPos).trim();
    joiner.add(var);
    Set<String> deferredVariables = Arrays
      .stream(var.split(","))
      .map(String::trim)
      .collect(Collectors.toCollection(HashSet::new));

    joiner.add("=");

    String expr = tagToken.getHelpers().substring(eqPos + 1);
    ChunkResolver chunkResolver = new ChunkResolver(expr, tagToken, interpreter)
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
