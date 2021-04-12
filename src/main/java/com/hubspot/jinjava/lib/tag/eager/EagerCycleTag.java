package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.tag.CycleTag;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.ChunkResolver;
import com.hubspot.jinjava.util.HelperStringTokenizer;
import com.hubspot.jinjava.util.WhitespaceUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    ChunkResolver chunkResolver = new ChunkResolver(expression, tagToken, interpreter);
    EagerStringResult eagerStringResult = executeInChildContext(
      eagerInterpreter -> chunkResolver.resolveChunks(),
      interpreter,
      true,
      false
    );

    StringBuilder prefixToPreserveState = new StringBuilder();
    if (interpreter.getContext().isDeferredExecutionMode()) {
      prefixToPreserveState.append(eagerStringResult.getPrefixToPreserveState());
    } else {
      interpreter.getContext().putAll(eagerStringResult.getSessionBindings());
    }
    String resolvedExpression = eagerStringResult
      .getResult()
      .toString()
      .replace(", ", ",");
    resolvedExpression = resolvedExpression.substring(1, resolvedExpression.length() - 1);
    if (WhitespaceUtils.isWrappedWith(resolvedExpression, "[", "]")) {
      resolvedExpression =
        resolvedExpression.substring(1, resolvedExpression.length() - 1);
    }
    List<String> resolvedValues; // can only be retrieved if the ResolvedChunks are fully resolved.
    if (!eagerStringResult.getResult().isFullyResolved()) {
      resolvedValues =
        new HelperStringTokenizer(resolvedExpression).splitComma(true).allTokens();
      prefixToPreserveState.append(
        reconstructFromContextBeforeDeferring(
          chunkResolver.getDeferredWords(),
          interpreter
        )
      );
    } else {
      List<?> objects = eagerStringResult.getResult().toList();
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
    if (helper.size() == 1) {
      // The helpers get printed out
      return (
        prefixToPreserveState.toString() +
        interpretPrintingCycle(
          tagToken,
          interpreter,
          resolvedValues,
          resolvedExpression,
          eagerStringResult.getResult().isFullyResolved()
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
          eagerStringResult.getResult().isFullyResolved()
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

  private String interpretSettingCycle(
    JinjavaInterpreter interpreter,
    List<String> values,
    List<String> helper,
    String resolvedExpression,
    boolean fullyResolved
  ) {
    String var = helper.get(2);
    if (!fullyResolved) {
      return EagerTagDecorator.buildSetTagForDeferredInChildContext(
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
    boolean fullyResolved
  ) {
    if (interpreter.getContext().isDeferredExecutionMode()) {
      return reconstructCycleTag(resolvedExpression, tagToken);
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
        return values.get(forindex % values.size());
      }
    }
    String item = values.get(forindex % values.size());
    if (!fullyResolved && ChunkResolver.shouldBeEvaluated(item, tagToken, interpreter)) {
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
