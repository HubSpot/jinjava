package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.tag.FlexibleTag;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import com.hubspot.jinjava.util.LengthLimitingStringJoiner;
import com.hubspot.jinjava.util.WhitespaceUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class EagerSetTag extends EagerStateChangingTag<SetTag> implements FlexibleTag {

  public EagerSetTag() {
    super(new SetTag());
  }

  public EagerSetTag(SetTag setTag) {
    super(setTag);
  }

  @Override
  public String eagerInterpret(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    InterpretException e
  ) {
    if (tagNode.getHelpers().contains("=")) {
      return getEagerImage(
        buildToken(tagNode, e, interpreter.getLineNumber()),
        interpreter
      );
    }
    return interpretBlockSet(tagNode, interpreter);
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

    EagerExecutionResult eagerExecutionResult = executeInChildContext(
      eagerInterpreter ->
        EagerExpressionResolver.resolveExpression('[' + expression + ']', interpreter),
      interpreter,
      true,
      false,
      interpreter.getContext().isDeferredExecutionMode()
    );

    String[] varTokens = variables.split(",");

    if (
      eagerExecutionResult.getResult().isFullyResolved() &&
      !interpreter.getContext().isDeferredExecutionMode()
    ) {
      try {
        getTag()
          .executeSet(
            tagToken,
            interpreter,
            varTokens,
            eagerExecutionResult.getResult().toList(),
            true
          );
        return "";
      } catch (DeferredValueException ignored) {}
    }
    return deferSetToken(tagToken, variables, eagerExecutionResult, interpreter);
  }

  private String deferSetToken(
    TagToken tagToken,
    String variables,
    EagerExecutionResult eagerExecutionResult,
    JinjavaInterpreter interpreter
  ) {
    String[] varTokens = variables.split(",");

    String deferredResult = eagerExecutionResult.getResult().toString();
    if (WhitespaceUtils.isWrappedWith(deferredResult, "[", "]")) {
      deferredResult = deferredResult.substring(1, deferredResult.length() - 1);
    }
    LengthLimitingStringJoiner joiner = new LengthLimitingStringJoiner(
      interpreter.getConfig().getMaxOutputSize(),
      " "
    )
      .add(tagToken.getSymbols().getExpressionStartWithTag())
      .add(tagToken.getTagName())
      .add(variables)
      .add("=")
      .add(deferredResult)
      .add(tagToken.getSymbols().getExpressionEndWithTag());
    String prefixToPreserveState = getPrefixToPreserveState(
      eagerExecutionResult,
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
          eagerExecutionResult
            .getResult()
            .getDeferredWords()
            .stream()
            .filter(
              word -> !(interpreter.getContext().get(word) instanceof DeferredValue)
            )
            .collect(Collectors.toSet()),
          Arrays.stream(varTokens).map(String::trim).collect(Collectors.toSet())
        )
      );
    String suffixToPreserveState = getSuffixToPreserveState(variables, interpreter);

    if (
      eagerExecutionResult.getResult().isFullyResolved() &&
      interpreter.getContext().isDeferredExecutionMode()
    ) {
      try {
        getTag()
          .executeSet(
            tagToken,
            interpreter,
            varTokens,
            eagerExecutionResult.getResult().toList(),
            true
          );
      } catch (DeferredValueException ignored) {}
    }
    return wrapInAutoEscapeIfNeeded(
      prefixToPreserveState + joiner + suffixToPreserveState,
      interpreter
    );
  }

  private String interpretBlockSet(TagNode tagNode, JinjavaInterpreter interpreter) {
    int filterPos = tagNode.getHelpers().indexOf('|');
    String var = tagNode.getHelpers().trim();

    if (filterPos >= 0) {
      var = tagNode.getHelpers().substring(0, filterPos).trim();
    }
    int numEagerTokens = interpreter.getContext().getEagerTokens().size();
    EagerExecutionResult blockResult = executeInChildContext(
      eagerInterpreter ->
        EagerExpressionResult.fromString(renderChildren(tagNode, eagerInterpreter)),
      interpreter,
      false,
      false,
      true
    );
    String[] varAsArray = new String[] { var };
    boolean fullyResolved =
      numEagerTokens == interpreter.getContext().getEagerTokens().size();
    if (fullyResolved && !interpreter.getContext().isDeferredExecutionMode()) {
      try {
        return eagerExecuteBlockSet(tagNode, interpreter, var, blockResult, varAsArray);
      } catch (DeferredValueException ignored) {}
    }
    LengthLimitingStringJoiner joiner = new LengthLimitingStringJoiner(
      interpreter.getConfig().getMaxOutputSize(),
      " "
    )
      .add(tagNode.getSymbols().getExpressionStartWithTag())
      .add(tagNode.getTag().getName())
      .add(var)
      .add(tagNode.getSymbols().getExpressionEndWithTag());
    String prefixToPreserveState = getPrefixToPreserveState(blockResult, interpreter);

    interpreter
      .getContext()
      .handleEagerToken(
        new EagerToken(
          new TagToken(
            joiner.toString(),
            tagNode.getLineNumber(),
            tagNode.getStartPosition(),
            tagNode.getSymbols()
          ),
          Collections.emptySet(),
          Collections.singleton(var)
        )
      );
    String suffixToPreserveState = getSuffixToPreserveState(var, interpreter);

    if (fullyResolved && interpreter.getContext().isDeferredExecutionMode()) {
      try {
        // try to override the value for just this context
        eagerExecuteBlockSet(tagNode, interpreter, var, blockResult, varAsArray);
      } catch (DeferredValueException ignored) {}
    }
    EagerExecutionResult filterResult = executeInChildContext(
      eagerInterpreter ->
        EagerExpressionResolver.resolveExpression(
          '[' + tagNode.getHelpers().trim() + ']',
          interpreter
        ),
      interpreter,
      true,
      false,
      interpreter.getContext().isDeferredExecutionMode()
    );
    String filterSetPostfix = filterPos >= 0
      ? deferSetToken((TagToken) tagNode.getMaster(), var, filterResult, interpreter)
      : "";

    return (
      prefixToPreserveState +
      joiner +
      blockResult.asTemplateString() +
      reconstructEnd(tagNode) +
      filterSetPostfix +
      suffixToPreserveState
    );
  }

  private String eagerExecuteBlockSet(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    String var,
    EagerExecutionResult blockResult,
    String[] varAsArray
  ) {
    getTag()
      .executeSet(
        (TagToken) tagNode.getMaster(),
        interpreter,
        varAsArray,
        Collections.singletonList(blockResult.getResult().toString()),
        true
      );
    EagerExecutionResult filterResult = executeInChildContext(
      eagerInterpreter ->
        EagerExpressionResolver.resolveExpression(
          '[' + tagNode.getHelpers().trim() + ']',
          interpreter
        ),
      interpreter,
      true,
      false,
      interpreter.getContext().isDeferredExecutionMode()
    );
    if (filterResult.getResult().isFullyResolved()) {
      getTag()
        .executeSet(
          (TagToken) tagNode.getMaster(),
          interpreter,
          varAsArray,
          filterResult.getResult().toList(),
          true
        );
    } else {
      // We could evaluate the block part, and just need to defer the filtering.
      return deferSetToken(
        (TagToken) tagNode.getMaster(),
        var,
        filterResult,
        interpreter
      );
    }
    return "";
  }

  private String getPrefixToPreserveState(
    EagerExecutionResult eagerExecutionResult,
    JinjavaInterpreter interpreter
  ) {
    StringBuilder prefixToPreserveState = new StringBuilder();
    if (interpreter.getContext().isDeferredExecutionMode()) {
      prefixToPreserveState.append(eagerExecutionResult.getPrefixToPreserveState());
    } else {
      interpreter.getContext().putAll(eagerExecutionResult.getSpeculativeBindings());
    }
    prefixToPreserveState.append(
      reconstructFromContextBeforeDeferring(
        eagerExecutionResult.getResult().getDeferredWords(),
        interpreter
      )
    );
    return prefixToPreserveState.toString();
  }

  private String getSuffixToPreserveState(
    String variables,
    JinjavaInterpreter interpreter
  ) {
    StringBuilder suffixToPreserveState = new StringBuilder();
    Optional<String> maybeFullImportAlias = interpreter
      .getContext()
      .getImportResourceAlias();
    if (maybeFullImportAlias.isPresent()) {
      String currentImportAlias = maybeFullImportAlias
        .get()
        .substring(maybeFullImportAlias.get().lastIndexOf(".") + 1);
      String updateString = getUpdateString(variables);
      suffixToPreserveState.append(
        interpreter.render(
          buildDoUpdateTag(currentImportAlias, updateString, interpreter)
        )
      );
    }
    return suffixToPreserveState.toString();
  }

  private static String getUpdateString(String variables) {
    List<String> varList = Arrays
      .stream(variables.split(","))
      .map(String::trim)
      .collect(Collectors.toList());
    StringJoiner updateString = new StringJoiner(",");
    // Update the alias map to the value of the set variable.
    varList.forEach(var -> updateString.add(String.format("'%s': %s", var, var)));
    return "{" + updateString.toString() + "}";
  }

  @Override
  public boolean hasEndTag(TagToken tagToken) {
    return false; // not yet implemented
  }
}
