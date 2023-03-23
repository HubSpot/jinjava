package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.tag.CycleTag;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerContextWatcher;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import com.hubspot.jinjava.util.HelperStringTokenizer;
import com.hubspot.jinjava.util.WhitespaceUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Beta
public class EagerCycleTag extends EagerStateChangingTag<CycleTag> {

  public EagerCycleTag() {
    super(new CycleTag());
  }

  public EagerCycleTag(CycleTag cycleTag) {
    super(cycleTag);
  }

  @SuppressWarnings("unchecked")
  @Override
  public String getEagerTagImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    HelperStringTokenizer tk = new HelperStringTokenizer(tagToken.getHelpers());

    List<String> helper = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    for (String token : tk.allTokens()) {
      sb.append(token);
      if (!token.endsWith(",")) {
        helper.add(sb.toString());
        sb = new StringBuilder();
      }
    }
    if (sb.length() > 0) {
      helper.add(sb.toString());
    }
    String expression = '[' + helper.get(0) + ']';
    EagerExecutionResult eagerExecutionResult = EagerContextWatcher.executeInChildContext(
      eagerInterpreter ->
        EagerExpressionResolver.resolveExpression(expression, interpreter),
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
    String resolvedExpression;
    List<String> resolvedValues; // can only be retrieved if the EagerExpressionResult are fully resolved.
    if (
      eagerExecutionResult
        .getResult()
        .toString()
        .equals(EagerExpressionResolver.JINJAVA_EMPTY_STRING)
    ) {
      resolvedExpression = normalizeResolvedExpression(expression); // Cycle tag defaults to input on null
      resolvedValues =
        new HelperStringTokenizer(resolvedExpression).splitComma(true).allTokens();
    } else {
      resolvedExpression =
        normalizeResolvedExpression(eagerExecutionResult.getResult().toString());
      if (!eagerExecutionResult.getResult().isFullyResolved()) {
        resolvedValues =
          new HelperStringTokenizer(resolvedExpression).splitComma(true).allTokens();
        prefixToPreserveState.append(
          EagerReconstructionUtils.reconstructFromContextBeforeDeferring(
            eagerExecutionResult.getResult().getDeferredWords(),
            interpreter
          )
        );
      } else {
        List<?> objects = eagerExecutionResult.getResult().toList();
        if (objects.size() == 1 && objects.get(0) instanceof List) {
          // because we may have wrapped in an extra set of brackets
          objects = (List<?>) objects.get(0);
        }
        resolvedValues =
          objects.stream().map(interpreter::getAsString).collect(Collectors.toList());
        for (int i = 0; i < resolvedValues.size(); i++) {
          resolvedValues.set(
            i,
            interpreter.resolveString(
              resolvedValues.get(i),
              tagToken.getLineNumber(),
              tagToken.getStartPosition()
            )
          );
        }
      }
    }
    if (helper.size() == 1) {
      // The helpers get printed out
      return (
        prefixToPreserveState.toString() +
        interpretPrintingCycle(
          tagToken,
          interpreter,
          resolvedValues,
          resolvedExpression,
          eagerExecutionResult.getResult().isFullyResolved(),
          eagerExecutionResult.getResult().getDeferredWords()
        )
      );
    } else if (helper.size() == 3) {
      // The helpers get set to a new variable
      return (
        prefixToPreserveState.toString() +
        interpretSettingCycle(
          interpreter,
          resolvedValues,
          helper,
          resolvedExpression,
          eagerExecutionResult.getResult().isFullyResolved()
        )
      );
    } else {
      throw new TemplateSyntaxException(
        tagToken.getImage(),
        "Tag 'cycle' expects 1 or 3 helper(s), was: " + helper.size(),
        tagToken.getLineNumber(),
        tagToken.getStartPosition()
      );
    }
  }

  private String normalizeResolvedExpression(String resolvedExpression) {
    resolvedExpression = resolvedExpression.replace(", ", ",");
    resolvedExpression = resolvedExpression.substring(1, resolvedExpression.length() - 1);
    if (WhitespaceUtils.isWrappedWith(resolvedExpression, "[", "]")) {
      resolvedExpression =
        resolvedExpression.substring(1, resolvedExpression.length() - 1);
    }
    return resolvedExpression;
  }

  private String interpretSettingCycle(
    JinjavaInterpreter interpreter,
    List<String> values,
    List<String> helper,
    String resolvedExpression,
    boolean fullyResolved
  ) {
    String var = helper.get(2);
    if (!fullyResolved) {
      return EagerReconstructionUtils.buildSetTag(
        ImmutableMap.of(
          var,
          String.format("[%s]", resolvedExpression.replace(",", ", "))
        ),
        interpreter,
        true
      );
    }
    interpreter.getContext().put(var, values);
    return "";
  }

  private String interpretPrintingCycle(
    TagToken tagToken,
    JinjavaInterpreter interpreter,
    List<String> values,
    String resolvedExpression,
    boolean fullyResolved,
    Set<String> deferredWords
  ) {
    if (interpreter.getContext().isDeferredExecutionMode()) {
      String reconstructedTag = reconstructCycleTag(resolvedExpression, tagToken);
      return (
        reconstructedTag +
        EagerReconstructionUtils.handleDeferredTokenAndReconstructReferences(
          interpreter,
          new DeferredToken(
            new TagToken(
              reconstructedTag,
              tagToken.getLineNumber(),
              tagToken.getStartPosition(),
              tagToken.getSymbols()
            ),
            deferredWords
          )
        )
      );
    }
    Integer forindex = (Integer) interpreter.retraceVariable(
      CycleTag.LOOP_INDEX,
      tagToken.getLineNumber(),
      tagToken.getStartPosition()
    );
    if (forindex == null) {
      forindex = 0;
    }
    if (values.size() == 1) {
      String var = values.get(0);
      if (!fullyResolved) {
        return getIsIterable(var, forindex, tagToken);
      } else {
        return var;
      }
    }
    String item = values.get(forindex % values.size());
    if (!fullyResolved && EagerExpressionResolver.shouldBeEvaluated(item, interpreter)) {
      return String.format("{{ %s }}", values.get(forindex % values.size()));
    }
    return item;
  }

  private String reconstructCycleTag(String expression, TagToken tagToken) {
    return String.format(
      "%s cycle %s %s",
      tagToken.getSymbols().getExpressionStartWithTag(),
      expression,
      tagToken.getSymbols().getExpressionEndWithTag()
    );
  }

  private static String getIsIterable(String var, int forIndex, TagToken tagToken) {
    String tokenStart = tagToken.getSymbols().getExpressionStartWithTag();
    String tokenEnd = tagToken.getSymbols().getExpressionEndWithTag();
    return (
      String.format(
        "%s if exptest:iterable.evaluate(%s, %s) %s",
        tokenStart,
        var,
        ExtendedParser.INTERPRETER,
        tokenEnd
      ) +
      // modulo indexing
      String.format(
        "{{ %s[%d %% filter:length.filter(%s, %s)] }}",
        var,
        forIndex,
        var,
        ExtendedParser.INTERPRETER
      ) +
      String.format("%s else %s", tokenStart, tokenEnd) +
      String.format("{{ %s }}", var) +
      String.format("%s endif %s", tokenStart, tokenEnd)
    );
  }
}
