package com.hubspot.jinjava.lib.expression;

import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.EscapeFilter;
import com.hubspot.jinjava.lib.tag.RawTag;
import com.hubspot.jinjava.lib.tag.eager.EagerStringResult;
import com.hubspot.jinjava.lib.tag.eager.EagerTagDecorator;
import com.hubspot.jinjava.lib.tag.eager.EagerToken;
import com.hubspot.jinjava.tree.output.RenderedOutputNode;
import com.hubspot.jinjava.tree.parse.ExpressionToken;
import com.hubspot.jinjava.util.ChunkResolver;
import com.hubspot.jinjava.util.Logging;
import com.hubspot.jinjava.util.WhitespaceUtils;
import org.apache.commons.lang3.StringUtils;

public class EagerExpressionStrategy implements ExpressionStrategy {

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
    ChunkResolver chunkResolver = new ChunkResolver(
      master.getExpr(),
      master,
      interpreter
    );
    EagerStringResult eagerStringResult = EagerTagDecorator.executeInChildContext(
      eagerInterpreter -> chunkResolver.resolveChunks(),
      interpreter,
      true,
      interpreter.getConfig().isNestedInterpretationEnabled()
    );
    StringBuilder prefixToPreserveState = new StringBuilder(
      interpreter.getContext().isDeferredExecutionMode()
        ? eagerStringResult.getPrefixToPreserveState()
        : ""
    );
    if (chunkResolver.getDeferredWords().isEmpty()) {
      String result = WhitespaceUtils.unquoteAndUnescape(
        eagerStringResult.getResult().toString()
      );
      if (
        !StringUtils.equals(result, master.getImage()) &&
        (
          StringUtils.contains(result, master.getSymbols().getExpressionStart()) ||
          StringUtils.contains(result, master.getSymbols().getExpressionStartWithTag())
        )
      ) {
        if (interpreter.getConfig().isNestedInterpretationEnabled()) {
          try {
            result = interpreter.renderFlat(result);
          } catch (Exception e) {
            Logging.ENGINE_LOG.warn("Error rendering variable node result", e);
          }
        } else {
          // Possible macro/set tag in front of this one. Includes result
          result = wrapInRawOrExpressionIfNeeded(result, interpreter);
        }
      }

      if (interpreter.getContext().isAutoEscape()) {
        result = EscapeFilter.escapeHtmlEntities(result);
      }
      return prefixToPreserveState.toString() + result;
    }
    prefixToPreserveState.append(
      EagerTagDecorator.reconstructFromContextBeforeDeferring(
        chunkResolver.getDeferredWords(),
        interpreter
      )
    );
    String helpers = wrapInExpression(
      eagerStringResult.getResult().toString(),
      interpreter
    );
    interpreter
      .getContext()
      .handleEagerToken(
        new EagerToken(
          new ExpressionToken(
            helpers,
            master.getLineNumber(),
            master.getStartPosition(),
            master.getSymbols()
          ),
          chunkResolver.getDeferredWords()
        )
      );
    // There is no only a preserving prefix because it couldn't be entirely evaluated.
    return EagerTagDecorator.wrapInAutoEscapeIfNeeded(
      prefixToPreserveState.toString() + helpers,
      interpreter
    );
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
      return EagerTagDecorator.wrapInTag(output, RawTag.TAG_NAME, interpreter);
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
