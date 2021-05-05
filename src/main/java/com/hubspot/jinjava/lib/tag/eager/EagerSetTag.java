package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import com.hubspot.jinjava.util.LengthLimitingStringJoiner;
import com.hubspot.jinjava.util.WhitespaceUtils;
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
          eagerExecutionResult.getResult().getDeferredWords(),
          Arrays.stream(varTokens).map(String::trim).collect(Collectors.toSet())
        )
      );

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
    return wrapInAutoEscapeIfNeeded(
      prefixToPreserveState + joiner.toString() + suffixToPreserveState.toString(),
      interpreter
    );
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
}
