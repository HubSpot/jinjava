package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.tag.ElseIfTag;
import com.hubspot.jinjava.lib.tag.ElseTag;
import com.hubspot.jinjava.lib.tag.IfTag;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.NoteToken;
import com.hubspot.jinjava.util.EagerContextWatcher;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

@Beta
public class EagerIfTag extends EagerTagDecorator<IfTag> {

  public EagerIfTag() {
    super(new IfTag());
  }

  public EagerIfTag(IfTag ifTag) {
    super(ifTag);
  }

  @Override
  public String innerInterpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    return getTag().interpret(tagNode, interpreter);
  }

  @Override
  public String eagerInterpret(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    InterpretException e
  ) {
    if (StringUtils.isBlank(tagNode.getHelpers())) {
      throw new TemplateSyntaxException(
        interpreter,
        tagNode.getMaster().getImage(),
        "Tag 'if' expects expression"
      );
    }

    LengthLimitingStringBuilder result = new LengthLimitingStringBuilder(
      interpreter.getConfig().getMaxOutputSize()
    );

    result.append(
      EagerContextWatcher
        .executeInChildContext(
          eagerInterpreter ->
            EagerExpressionResult.fromString(
              eagerRenderBranches(tagNode, eagerInterpreter, e)
            ),
          interpreter,
          EagerContextWatcher
            .EagerChildContextConfig.newBuilder()
            .withForceDeferredExecutionMode(true)
            .build()
        )
        .asTemplateString()
    );
    tagNode.getMaster().setRightTrimAfterEnd(false);
    result.append(EagerReconstructionUtils.reconstructEnd(tagNode));

    return result.toString();
  }

  public String eagerRenderBranches(
    TagNode tagNode,
    JinjavaInterpreter interpreter,
    InterpretException e
  ) {
    // line number of the last attempted resolveELExpression
    final int deferredLineNumber = interpreter.getLineNumber();
    final int deferredPosition = interpreter.getPosition();
    // If the branch is impossible, it should be removed.
    boolean definitelyDrop = shouldDropBranch(
      tagNode,
      interpreter,
      deferredLineNumber,
      deferredPosition
    );
    // If an ("elseif") branch would definitely get executed,
    // change it to an "else" tag and drop all the subsequent branches.
    // We know this has to start as false otherwise IfTag would have chosen
    // the first branch.
    boolean definitelyExecuted = false;
    StringBuilder prefixToPreserveState = new StringBuilder();
    StringBuilder sb = new StringBuilder();
    sb.append(
      getEagerImage(
        buildToken(tagNode, e, deferredLineNumber, deferredPosition),
        interpreter
      )
    );
    int branchStart = 0;
    int childrenSize = tagNode.getChildren().size();
    Set<String> bindingsToDefer = new HashSet<>();
    while (branchStart < childrenSize) {
      int branchEnd = findNextElseToken(tagNode, branchStart);
      if (!definitelyDrop) {
        int finalBranchStart = branchStart;
        EagerExecutionResult result = EagerContextWatcher.executeInChildContext(
          eagerInterpreter ->
            EagerExpressionResult.fromString(
              evaluateBranch(tagNode, finalBranchStart, branchEnd, interpreter)
            ),
          interpreter,
          EagerContextWatcher
            .EagerChildContextConfig.newBuilder()
            .withForceDeferredExecutionMode(true)
            .build()
        );
        sb.append(result.getResult());
        bindingsToDefer.addAll(
          EagerReconstructionUtils.resetSpeculativeBindings(interpreter, result)
        );
      }
      if (branchEnd >= childrenSize || definitelyExecuted) {
        break;
      }
      TagNode caseNode = (TagNode) tagNode.getChildren().get(branchEnd);
      definitelyDrop =
        caseNode.getName().equals(ElseIfTag.TAG_NAME) &&
        shouldDropBranch(caseNode, interpreter, deferredLineNumber, deferredPosition);
      if (!definitelyDrop) {
        definitelyExecuted =
          caseNode.getName().equals(ElseTag.TAG_NAME) ||
          isDefinitelyExecuted(caseNode, interpreter, deferredLineNumber);
        if (definitelyExecuted) {
          sb.append(
            String.format(
              "%s else %s",
              caseNode.getSymbols().getExpressionStartWithTag(),
              caseNode.getSymbols().getExpressionEndWithTag()
            )
          );
        } else {
          sb.append(
            getEagerImage(
              buildToken(caseNode, e, deferredLineNumber, deferredPosition),
              interpreter
            )
          );
        }
      }
      branchStart = branchEnd + 1;
    }
    if (!bindingsToDefer.isEmpty()) {
      bindingsToDefer =
        bindingsToDefer
          .stream()
          .filter(key -> !(interpreter.getContext().get(key) instanceof DeferredValue))
          .collect(Collectors.toSet());
      if (!bindingsToDefer.isEmpty()) {
        prefixToPreserveState.append(
          EagerReconstructionUtils.handleDeferredTokenAndReconstructReferences(
            interpreter,
            new DeferredToken(
              new NoteToken(
                "",
                interpreter.getLineNumber(),
                interpreter.getPosition(),
                interpreter.getConfig().getTokenScannerSymbols()
              ),
              bindingsToDefer
            )
          )
        );
      }
      return sb.toString();
    }
    return sb.toString();
  }

  private String evaluateBranch(
    TagNode tagNode,
    int startIdx,
    int endIdx,
    JinjavaInterpreter interpreter
  ) {
    StringBuilder sb = new StringBuilder();
    for (int i = startIdx; i < endIdx; i++) {
      Node child = tagNode.getChildren().get(i);
      sb.append(child.render(interpreter).getValue());
    }
    return sb.toString();
  }

  private int findNextElseToken(TagNode tagNode, int startIdx) {
    int i;
    for (i = startIdx; i < tagNode.getChildren().size(); i++) {
      Node childNode = tagNode.getChildren().get(i);
      if (
        (TagNode.class.isAssignableFrom(childNode.getClass())) &&
        childNode.getName().equals(ElseIfTag.TAG_NAME) ||
        childNode.getName().equals(ElseTag.TAG_NAME)
      ) {
        return i;
      }
    }
    return i;
  }

  private boolean shouldDropBranch(
    TagNode tagNode,
    JinjavaInterpreter eagerInterpreter,
    int deferredLineNumber,
    int deferredPosition
  ) {
    if (deferredLineNumber > tagNode.getLineNumber()) {
      return true; // Deferred value thrown on a later branch so we can drop this one.
    } else if (
      deferredLineNumber == tagNode.getLineNumber() &&
      deferredPosition >= tagNode.getStartPosition()
    ) {
      return deferredPosition > tagNode.getStartPosition(); // false if they are equal
    }
    // the tag node is after the deferred exception location
    try {
      return !getTag().isPositiveIfElseNode(tagNode, eagerInterpreter);
    } catch (DeferredValueException e) {
      return false;
    }
  }

  private boolean isDefinitelyExecuted(
    TagNode tagNode,
    JinjavaInterpreter eagerInterpreter,
    int deferredLineNumber
  ) {
    if (deferredLineNumber == tagNode.getLineNumber()) {
      return false; // Deferred value thrown when checking if this branch would be executed.
    }
    try {
      return getTag().isPositiveIfElseNode(tagNode, eagerInterpreter);
    } catch (DeferredValueException e) {
      return false;
    }
  }
}
