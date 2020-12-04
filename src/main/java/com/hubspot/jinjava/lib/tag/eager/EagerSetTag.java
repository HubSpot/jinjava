package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.ChunkResolver;
import com.hubspot.jinjava.util.LengthLimitingStringJoiner;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class EagerSetTag extends EagerStateChangingTag<SetTag> {

  public EagerSetTag() {
    super(new SetTag());
  }

  public EagerSetTag(SetTag setTag) {
    super(setTag);
  }

  @Override
  public String getEagerTagImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    if (!tagToken.getHelpers().contains("=")) {
      throw new TemplateSyntaxException(
        interpreter,
        tagToken.getImage(),
        "Tag 'set' expects an assignment expression with '=', but was: " +
        tagToken.getHelpers()
      );
    }

    int eqPos = tagToken.getHelpers().indexOf('=');
    String variables = tagToken.getHelpers().substring(0, eqPos).trim();

    String expression = tagToken.getHelpers().substring(eqPos + 1);

    ChunkResolver chunkResolver = new ChunkResolver(expression, tagToken, interpreter);
    EagerStringResult resolvedExpression = executeInChildContext(
      eagerInterpreter -> chunkResolver.resolveChunks(),
      interpreter,
      true
    );
    LengthLimitingStringJoiner joiner = new LengthLimitingStringJoiner(
      interpreter.getConfig().getMaxOutputSize(),
      " "
    );
    joiner
      .add(tagToken.getSymbols().getExpressionStartWithTag())
      .add(tagToken.getTagName())
      .add(variables)
      .add("=")
      .add(resolvedExpression.getResult())
      .add(tagToken.getSymbols().getExpressionEndWithTag());
    StringBuilder prefixToPreserveState = new StringBuilder(
      interpreter.getContext().isProtectedMode()
        ? resolvedExpression.getPrefixToPreserveState()
        : ""
    );
    String[] varTokens = variables.split(",");

    if (
      chunkResolver.getDeferredWords().isEmpty() &&
      !interpreter.getContext().isProtectedMode()
    ) {
      try {
        getTag()
          .executeSet(
            tagToken,
            interpreter,
            varTokens,
            resolvedExpression.getResult(),
            true
          );
        return "";
      } catch (DeferredValueException ignored) {}
    }
    prefixToPreserveState.append(
      reconstructFromContextBeforeDeferring(chunkResolver.getDeferredWords(), interpreter)
    );
    Optional<String> maybeFullImportAlias = interpreter
      .getContext()
      .getImportResourceAlias();
    if (maybeFullImportAlias.isPresent()) {
      String currentImportAlias = maybeFullImportAlias
        .get()
        .substring(maybeFullImportAlias.get().lastIndexOf(".") + 1);
      String updateString = getUpdateString(
        variables,
        resolvedExpression.getResult(),
        tagToken,
        interpreter
      );
      prefixToPreserveState.append(
        interpreter.render(
          buildDoUpdateTag(currentImportAlias, updateString, interpreter)
        )
      );
      //      return wrapInAutoEscapeIfNeeded(
      //        prefixToPreserveState.toString() +
      //        interpreter.render(
      //          buildDoUpdateTag(currentImportAlias, updateString, interpreter)
      //        ),
      //        interpreter
      //      );
    }

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
          Arrays.stream(varTokens).map(String::trim).collect(Collectors.toSet())
        )
      );
    // Possible macro/set tag in front of this one.
    return wrapInAutoEscapeIfNeeded(
      prefixToPreserveState.toString() + joiner.toString(),
      interpreter
    );
  }

  private static String getUpdateString(
    String variables,
    String expression,
    TagToken tagToken,
    JinjavaInterpreter interpreter
  ) {
    List<String> varList = Arrays
      .stream(variables.split(","))
      .map(String::trim)
      .collect(Collectors.toList());
    ChunkResolver chunkResolver = new ChunkResolver(expression, tagToken, interpreter);
    List<String> expressionList = chunkResolver.splitChunks();
    StringJoiner updateString = new StringJoiner(",");
    for (int i = 0; i < varList.size() && i < expressionList.size(); i++) {
      updateString.add(String.format("'%s': %s", varList.get(i), expressionList.get(i)));
    }
    return updateString.toString();
  }
}
