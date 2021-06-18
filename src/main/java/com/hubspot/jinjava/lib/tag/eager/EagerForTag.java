package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.tag.ForTag;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import com.hubspot.jinjava.util.HelperStringTokenizer;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;
import com.hubspot.jinjava.util.LengthLimitingStringJoiner;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class EagerForTag extends EagerTagDecorator<ForTag> {

  public EagerForTag() {
    super(new ForTag());
  }

  public EagerForTag(ForTag forTag) {
    super(forTag);
  }

  @Override
  public String eagerInterpret(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    InterpretException e
  ) {
    LengthLimitingStringBuilder result = new LengthLimitingStringBuilder(
      interpreter.getConfig().getMaxOutputSize()
    );

    // separate getEagerImage from renderChildren because the token gets evaluated once
    // while the children are evaluated 0...n times.
    result.append(
      executeInChildContext(
          eagerInterpreter ->
            EagerExpressionResult.fromString(
              getEagerImage(
                buildToken(tagNode, e, interpreter.getLineNumber()),
                eagerInterpreter
              )
            ),
          interpreter,
          true,
          false,
          false
        )
        .asTemplateString()
    );

    EagerExecutionResult eagerExecutionResult = executeInChildContext(
      eagerInterpreter -> {
        eagerInterpreter.getContext().put("loop", DeferredValue.instance());
        return EagerExpressionResult.fromString(
          renderChildren(tagNode, eagerInterpreter)
        );
      },
      interpreter,
      false,
      false,
      true
    );
    if (!eagerExecutionResult.getSpeculativeBindings().isEmpty()) {
      // Values cannot be modified within a for loop because we don't know many times, if any it will run
      throw new DeferredValueException(
        "Modified values in deferred for loop: " +
        String.join(", ", eagerExecutionResult.getSpeculativeBindings().keySet())
      );
    }
    result.append(eagerExecutionResult.asTemplateString());
    result.append(reconstructEnd(tagNode));
    return result.toString();
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

    EagerExpressionResult eagerExpressionResult = EagerExpressionResolver.resolveExpression(
      loopExpression,
      interpreter
    );

    LengthLimitingStringJoiner joiner = new LengthLimitingStringJoiner(
      interpreter.getConfig().getMaxOutputSize(),
      " "
    );

    joiner
      .add(tagToken.getSymbols().getExpressionStartWithTag())
      .add(tagToken.getTagName())
      .add(String.join(", ", loopVars))
      .add("in")
      .add(eagerExpressionResult.toString())
      .add(tagToken.getSymbols().getExpressionEndWithTag());
    String newlyDeferredFunctionImages = reconstructFromContextBeforeDeferring(
      eagerExpressionResult.getDeferredWords(),
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
          eagerExpressionResult
            .getDeferredWords()
            .stream()
            .filter(
              word -> !(interpreter.getContext().get(word) instanceof DeferredValue)
            )
            .collect(Collectors.toSet()),
          new HashSet<>(loopVars)
        )
      );
    return (newlyDeferredFunctionImages + joiner.toString());
  }
}
