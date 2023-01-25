package com.hubspot.jinjava.lib.expression;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.DeferredMacroValueImpl;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.lib.filter.EscapeFilter;
import com.hubspot.jinjava.lib.tag.RawTag;
import com.hubspot.jinjava.lib.tag.eager.DeferredToken;
import com.hubspot.jinjava.lib.tag.eager.EagerExecutionResult;
import com.hubspot.jinjava.tree.output.RenderedOutputNode;
import com.hubspot.jinjava.tree.parse.ExpressionToken;
import com.hubspot.jinjava.util.EagerContextWatcher;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import com.hubspot.jinjava.util.Logging;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

@Beta
public class EagerExpressionStrategy implements ExpressionStrategy {
  private static final long serialVersionUID = -6792345439237764193L;

  @Override
  public RenderedOutputNode interpretOutput(
    ExpressionToken master,
    JinjavaInterpreter interpreter
  ) {
    return new RenderedOutputNode(eagerResolveExpression(master, interpreter));
  }

  private String eagerResolveExpression(
    ExpressionToken master,
    JinjavaInterpreter interpreter
  ) {
    interpreter.getContext().checkNumberOfDeferredTokens();
    EagerExecutionResult eagerExecutionResult = EagerContextWatcher.executeInChildContext(
      eagerInterpreter ->
        EagerExpressionResolver.resolveExpression(master.getExpr(), interpreter),
      interpreter,
      EagerContextWatcher
        .EagerChildContextConfig.newBuilder()
        .withTakeNewValue(true)
        .withPartialMacroEvaluation(
          interpreter.getConfig().isNestedInterpretationEnabled()
        )
        .build()
    );

    StringBuilder prefixToPreserveState = new StringBuilder();
    if (interpreter.getContext().isDeferredExecutionMode()) {
      prefixToPreserveState.append(eagerExecutionResult.getPrefixToPreserveState());
    } else {
      interpreter.getContext().putAll(eagerExecutionResult.getSpeculativeBindings());
    }
    if (eagerExecutionResult.getResult().isFullyResolved()) {
      String result = eagerExecutionResult.getResult().toString(true);
      return (
        prefixToPreserveState.toString() + postProcessResult(master, result, interpreter)
      );
    }
    prefixToPreserveState.append(
      EagerReconstructionUtils.reconstructFromContextBeforeDeferring(
        eagerExecutionResult.getResult().getDeferredWords(),
        interpreter
      )
    );
    String helpers = wrapInExpression(
      eagerExecutionResult.getResult().toString(),
      interpreter
    );
    prefixToPreserveState.append(
      EagerReconstructionUtils.handleDeferredTokenAndReconstructReferences(
        interpreter,
        new DeferredToken(
          new ExpressionToken(
            helpers,
            master.getLineNumber(),
            master.getStartPosition(),
            master.getSymbols()
          ),
          eagerExecutionResult
            .getResult()
            .getDeferredWords()
            .stream()
            .filter(
              word ->
                !(interpreter.getContext().get(word) instanceof DeferredMacroValueImpl)
            )
            .collect(Collectors.toSet())
        )
      )
    );
    // There is only a preserving prefix because it couldn't be entirely evaluated.
    return EagerReconstructionUtils.wrapInAutoEscapeIfNeeded(
      prefixToPreserveState.toString() + helpers,
      interpreter
    );
  }

  public static String postProcessResult(
    ExpressionToken master,
    String result,
    JinjavaInterpreter interpreter
  ) {
    if (
      !StringUtils.equals(result, master.getImage()) &&
      (
        StringUtils.contains(result, master.getSymbols().getExpressionStart()) ||
        StringUtils.contains(result, master.getSymbols().getExpressionStartWithTag())
      )
    ) {
      if (interpreter.getConfig().isNestedInterpretationEnabled()) {
        long errorSizeStart = getParsingErrorsCount(interpreter);

        interpreter.parse(result);

        if (getParsingErrorsCount(interpreter) == errorSizeStart) {
          try {
            result = interpreter.renderFlat(result);
          } catch (Exception e) {
            Logging.ENGINE_LOG.warn("Error rendering variable node result", e);
          }
        }
      } else {
        // Possible macro/set tag in front of this one. Includes result
        result = wrapInRawOrExpressionIfNeeded(result, interpreter);
      }
    }

    if (interpreter.getContext().isAutoEscape()) {
      result = EscapeFilter.escapeHtmlEntities(result);
    }
    return result;
  }

  private static long getParsingErrorsCount(JinjavaInterpreter interpreter) {
    return interpreter
      .getErrors()
      .stream()
      .filter(Objects::nonNull)
      .filter(
        error ->
          "Unclosed comment".equals(error.getMessage()) ||
          error.getReason() == ErrorReason.DISABLED
      )
      .count();
  }

  private static String wrapInRawOrExpressionIfNeeded(
    String output,
    JinjavaInterpreter interpreter
  ) {
    JinjavaConfig config = interpreter.getConfig();
    if (
      config.getExecutionMode().isPreserveRawTags() &&
      !interpreter.getContext().isUnwrapRawOverride() &&
      (
        output.contains(config.getTokenScannerSymbols().getExpressionStart()) ||
        output.contains(config.getTokenScannerSymbols().getExpressionStartWithTag())
      )
    ) {
      return EagerReconstructionUtils.wrapInTag(output, RawTag.TAG_NAME, interpreter);
    }
    return output;
  }

  private static String wrapInExpression(String output, JinjavaInterpreter interpreter) {
    JinjavaConfig config = interpreter.getConfig();
    return String.format(
      "%s %s %s",
      config.getTokenScannerSymbols().getExpressionStart(),
      output,
      config.getTokenScannerSymbols().getExpressionEnd()
    );
  }
}
