package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.google.common.collect.Sets;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerContextWatcher;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult.ResolutionState;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import com.hubspot.jinjava.util.LengthLimitingStringJoiner;
import java.util.Collections;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Triple;

@Beta
public class EagerBlockSetTagStrategy extends EagerSetTagStrategy {
  public static final EagerBlockSetTagStrategy INSTANCE = new EagerBlockSetTagStrategy(
    new SetTag()
  );

  protected EagerBlockSetTagStrategy(SetTag setTag) {
    super(setTag);
  }

  @Override
  protected EagerExecutionResult getEagerExecutionResult(
    TagNode tagNode,
    String expression,
    JinjavaInterpreter interpreter
  ) {
    EagerExecutionResult result = EagerContextWatcher.executeInChildContext(
      eagerInterpreter ->
        EagerExpressionResult.fromSupplier(
          () -> {
            StringBuilder sb = new StringBuilder();
            for (Node child : tagNode.getChildren()) {
              sb.append(child.render(eagerInterpreter).getValue());
            }
            return sb.toString();
          },
          eagerInterpreter
        ),
      interpreter,
      EagerContextWatcher
        .EagerChildContextConfig.newBuilder()
        .withTakeNewValue(true)
        .build()
    );
    if (result.getResult().getResolutionState() == ResolutionState.NONE) {
      throw new DeferredValueException(result.getResult().toString());
    }
    return result;
  }

  @Override
  protected Optional<String> resolveSet(
    TagNode tagNode,
    String[] variables,
    EagerExecutionResult eagerExecutionResult,
    JinjavaInterpreter interpreter
  ) {
    int filterPos = tagNode.getHelpers().indexOf('|');
    try {
      setTag.executeSet(
        (TagToken) tagNode.getMaster(),
        interpreter,
        variables,
        eagerExecutionResult.getResult().toList(),
        true
      );
      if (filterPos >= 0) {
        EagerExecutionResult filterResult = EagerInlineSetTagStrategy.INSTANCE.getEagerExecutionResult(
          tagNode,
          tagNode.getHelpers().trim(),
          interpreter
        );
        if (filterResult.getResult().isFullyResolved()) {
          setTag.executeSet(
            (TagToken) tagNode.getMaster(),
            interpreter,
            variables,
            filterResult.getResult().toList(),
            true
          );
        } else {
          // We could evaluate the block part, and just need to defer the filtering.
          return Optional.of(
            runInlineStrategy(tagNode, variables, filterResult, interpreter)
          );
        }
      }
      return Optional.of("");
    } catch (DeferredValueException ignored) {}
    return Optional.empty();
  }

  @Override
  protected Triple<String, String, String> getPrefixTokenAndSuffix(
    TagNode tagNode,
    String[] variables,
    EagerExecutionResult eagerExecutionResult,
    JinjavaInterpreter interpreter
  ) {
    LengthLimitingStringJoiner joiner = new LengthLimitingStringJoiner(
      interpreter.getConfig().getMaxOutputSize(),
      " "
    )
      .add(tagNode.getSymbols().getExpressionStartWithTag())
      .add(tagNode.getTag().getName())
      .add(variables[0])
      .add(tagNode.getSymbols().getExpressionEndWithTag());

    String prefixToPreserveState =
      getPrefixToPreserveState(eagerExecutionResult, variables, interpreter) +
      EagerReconstructionUtils.handleDeferredTokenAndReconstructReferences(
        interpreter,
        new DeferredToken(
          new TagToken(
            joiner.toString(),
            tagNode.getLineNumber(),
            tagNode.getStartPosition(),
            tagNode.getSymbols()
          ),
          Collections.emptySet(),
          Sets.newHashSet(variables)
        )
      );
    String suffixToPreserveState = getSuffixToPreserveState(variables[0], interpreter);
    return Triple.of(prefixToPreserveState, joiner.toString(), suffixToPreserveState);
  }

  @Override
  protected void attemptResolve(
    TagNode tagNode,
    String[] variables,
    EagerExecutionResult eagerExecutionResult,
    JinjavaInterpreter interpreter
  ) {
    try {
      // try to override the value for just this context
      setTag.executeSet(
        (TagToken) tagNode.getMaster(),
        interpreter,
        variables,
        eagerExecutionResult.getResult().toList(),
        true
      );
    } catch (DeferredValueException ignored) {}
  }

  @Override
  protected String buildImage(
    TagNode tagNode,
    String[] variables,
    EagerExecutionResult eagerExecutionResult,
    Triple<String, String, String> triple,
    JinjavaInterpreter interpreter
  ) {
    int filterPos = tagNode.getHelpers().indexOf('|');
    String filterSetPostfix = "";
    if (filterPos >= 0) {
      EagerExecutionResult filterResult = EagerInlineSetTagStrategy.INSTANCE.getEagerExecutionResult(
        tagNode,
        tagNode.getHelpers().trim(),
        interpreter
      );
      if (filterResult.getResult().isFullyResolved()) {
        setTag.executeSet(
          (TagToken) tagNode.getMaster(),
          interpreter,
          variables,
          filterResult.getResult().toList(),
          true
        );
      }
      filterSetPostfix = runInlineStrategy(tagNode, variables, filterResult, interpreter);
    }

    return EagerReconstructionUtils.wrapInAutoEscapeIfNeeded(
      triple.getLeft() +
      triple.getMiddle() +
      eagerExecutionResult.getResult().toString(true) +
      EagerReconstructionUtils.reconstructEnd(tagNode) +
      filterSetPostfix +
      triple.getRight(),
      interpreter
    );
  }

  private String runInlineStrategy(
    TagNode tagNode,
    String[] variables,
    EagerExecutionResult eagerExecutionResult,
    JinjavaInterpreter interpreter
  ) {
    Triple<String, String, String> triple = EagerInlineSetTagStrategy.INSTANCE.getPrefixTokenAndSuffix(
      tagNode,
      variables,
      eagerExecutionResult,
      interpreter
    );
    if (
      eagerExecutionResult.getResult().isFullyResolved() &&
      interpreter.getContext().isDeferredExecutionMode()
    ) {
      EagerInlineSetTagStrategy.INSTANCE.attemptResolve(
        tagNode,
        variables,
        eagerExecutionResult,
        interpreter
      );
    }
    return EagerInlineSetTagStrategy.INSTANCE.buildImage(
      tagNode,
      variables,
      eagerExecutionResult,
      triple,
      interpreter
    );
  }
}
