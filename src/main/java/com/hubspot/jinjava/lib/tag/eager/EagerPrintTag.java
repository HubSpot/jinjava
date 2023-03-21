package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.DeferredMacroValueImpl;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.tag.PrintTag;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerContextWatcher;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import com.hubspot.jinjava.util.LengthLimitingStringJoiner;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

@Beta
public class EagerPrintTag extends EagerStateChangingTag<PrintTag> {

  public EagerPrintTag() {
    super(new PrintTag());
  }

  public EagerPrintTag(PrintTag printTag) {
    super(printTag);
  }

  @Override
  public String getEagerTagImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    String expr = tagToken.getHelpers();
    if (StringUtils.isBlank(expr)) {
      throw new TemplateSyntaxException(
        interpreter,
        tagToken.getImage(),
        "Tag 'print' expects expression"
      );
    }
    return interpretExpression(expr, tagToken, interpreter, true);
  }

  /**
   * Interprets the expression, which may depend on deferred values.
   * If the expression can be entirely evaluated, return the result only if
   * {@code includeExpressionResult} is true.
   * When the expression depends on deferred values, then reconstruct the tag.
   * @param expr Expression to interpret.
   * @param tagToken TagToken which is calling the expression.
   * @param interpreter The Jinjava interpreter.
   * @param includeExpressionResult Whether to include the result of the expression in
   *                                the output.
   * @return The result of the expression, if requested. OR a reconstruction of the calling tag.
   */
  public static String interpretExpression(
    String expr,
    TagToken tagToken,
    JinjavaInterpreter interpreter,
    boolean includeExpressionResult
  ) {
    EagerExecutionResult eagerExecutionResult = EagerContextWatcher.executeInChildContext(
      eagerInterpreter -> EagerExpressionResolver.resolveExpression(expr, interpreter),
      interpreter,
      EagerContextWatcher
        .EagerChildContextConfig.newBuilder()
        .withTakeNewValue(true)
        .build()
    );
    StringBuilder prefixToPreserveState = new StringBuilder();
    if (interpreter.getContext().isDeferredExecutionMode()) {
      prefixToPreserveState.append(eagerExecutionResult.getPrefixToPreserveState());
    } else {
      interpreter.getContext().putAll(eagerExecutionResult.getSpeculativeBindings());
    }
    if (eagerExecutionResult.getResult().isFullyResolved()) {
      // Possible macro/set tag in front of this one.
      return (
        prefixToPreserveState.toString() +
        (
          includeExpressionResult
            ? EagerReconstructionUtils.wrapInRawIfNeeded(
              eagerExecutionResult.getResult().toString(true),
              interpreter
            )
            : ""
        )
      );
    }
    prefixToPreserveState.append(
      EagerReconstructionUtils.reconstructFromContextBeforeDeferring(
        eagerExecutionResult.getResult().getDeferredWords(),
        interpreter
      )
    );

    LengthLimitingStringJoiner joiner = new LengthLimitingStringJoiner(
      interpreter.getConfig().getMaxOutputSize(),
      " "
    );
    joiner
      .add(tagToken.getSymbols().getExpressionStartWithTag())
      .add(tagToken.getTagName())
      .add(eagerExecutionResult.getResult().toString().trim())
      .add(tagToken.getSymbols().getExpressionEndWithTag());
    prefixToPreserveState.append(
      EagerReconstructionUtils.handleDeferredTokenAndReconstructReferences(
        interpreter,
        new DeferredToken(
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
              word ->
                !(interpreter.getContext().get(word) instanceof DeferredMacroValueImpl)
            )
            .collect(Collectors.toSet())
        )
      )
    );
    // Possible set tag in front of this one.
    return EagerReconstructionUtils.wrapInAutoEscapeIfNeeded(
      prefixToPreserveState.toString() + joiner.toString(),
      interpreter
    );
  }
}
