package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.tag.DoTag;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.ChunkResolver;
import com.hubspot.jinjava.util.LengthLimitingStringJoiner;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
          .executeSet(tagToken, interpreter, varTokens, resolvedExpression.getResult());
        return "";
      } catch (DeferredValueException ignored) {}
    }
    prefixToPreserveState.append(
      reconstructFromContextBeforeDeferring(chunkResolver.getDeferredWords(), interpreter)
    );
    if (interpreter.getContext().containsKey(Context.IMPORT_RESOURCE_ALIAS_KEY)) {
      String fullImportAlias = interpreter
        .getContext()
        .getImportResourceAlias()
        .orElseThrow(RuntimeException::new);
      String currentAlias = fullImportAlias.substring(
        fullImportAlias.lastIndexOf(".") + 1
      );
      //      if ((interpreter.getContext().get(currentAlias) instanceof Map)) {
      //        prefixToPreserveState.append(
      //          buildSetTagForDeferredInChildContext(
      //            ImmutableMap.of(
      //              currentAlias,
      //              String.format(
      //                "{%s}",
      //                (
      //                  (Map<String, Object>) interpreter.getContext().get(currentAlias)
      //                ).keySet()
      //                  .stream()
      //                  .map(key -> String.format("'%s': %s", key, key))
      //                  .collect(Collectors.joining(","))
      //              )
      //            ),
      //            interpreter,
      //            true
      //          )
      //        );
      //      }
      return wrapInAutoEscapeIfNeeded(
        prefixToPreserveState.toString() +
        interpreter.render(
          convertSetToUpdate(
            variables,
            resolvedExpression.getResult(),
            tagToken,
            interpreter
          )
        ),
        interpreter
      );
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

  private static String convertSetToUpdate(
    String variables,
    String expression,
    TagToken tagToken,
    JinjavaInterpreter interpreter
  ) {
    String fullImportAlias = interpreter
      .getContext()
      .getImportResourceAlias()
      .orElseThrow(RuntimeException::new);
    String currentAlias = fullImportAlias.substring(fullImportAlias.lastIndexOf(".") + 1);

    LengthLimitingStringJoiner joiner = new LengthLimitingStringJoiner(
      interpreter.getConfig().getMaxOutputSize(),
      " "
    )
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionStartWithTag())
      .add(DoTag.TAG_NAME);
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

    joiner.add(
      String.format(
        "%s.update({%s})",
        fullImportAlias.substring(fullImportAlias.lastIndexOf(".") + 1),
        updateString.toString()
      )
    );
    joiner.add(
      interpreter.getConfig().getTokenScannerSymbols().getExpressionEndWithTag()
    );
    return joiner.toString();
  }
}
