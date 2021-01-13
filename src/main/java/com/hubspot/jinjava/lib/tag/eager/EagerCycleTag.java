package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.lib.tag.CycleTag;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.ChunkResolver;
import com.hubspot.jinjava.util.HelperStringTokenizer;
import com.hubspot.jinjava.util.WhitespaceUtils;
import java.util.ArrayList;
import java.util.List;

public class EagerCycleTag extends EagerStateChangingTag<CycleTag> {

  public EagerCycleTag() {
    super(new CycleTag());
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
    ChunkResolver chunkResolver = new ChunkResolver(helper.get(0), tagToken, interpreter);
    EagerStringResult resolvedExpression = executeInChildContext(
      eagerInterpreter -> chunkResolver.resolveChunks(),
      interpreter,
      true,
      false
    );
    String expression = resolvedExpression.getResult();
    if (WhitespaceUtils.isWrappedWith(expression, "[", "]")) {
      expression = expression.substring(1, expression.length() - 1).replaceAll(", ", ",");
    }
    StringBuilder prefixToPreserveState = new StringBuilder(
      interpreter.getContext().isDeferredExecutionMode()
        ? resolvedExpression.getPrefixToPreserveState()
        : ""
    );
    HelperStringTokenizer items = new HelperStringTokenizer(expression).splitComma(true);
    List<String> values = items.allTokens();
    if (!chunkResolver.getDeferredWords().isEmpty()) {
      prefixToPreserveState.append(
        reconstructFromContextBeforeDeferring(
          chunkResolver.getDeferredWords(),
          interpreter
        )
      );
    } else {
      for (int i = 0; i < values.size(); i++) {
        values.set(
          i,
          interpreter.resolveString(
            values.get(i),
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
        interpretPrintingCycle(tagToken, interpreter, values, chunkResolver, expression)
      );
    } else if (helper.size() == 3) {
      // The helpers get set to a new variable
      return (
        prefixToPreserveState.toString() +
        interpretSettingCycle(interpreter, values, helper, chunkResolver, expression)
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
    ChunkResolver chunkResolver,
    String resolvedExpression
  ) {
    String var = helper.get(2);
    if (!chunkResolver.getDeferredWords().isEmpty()) {
      return EagerTagDecorator.buildSetTagForDeferredInChildContext(
        ImmutableMap.of(var, String.format("[%s]", resolvedExpression)),
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
    ChunkResolver chunkResolver,
    String resolvedExpression
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
      if (!chunkResolver.getDeferredWords().isEmpty()) {
        return getIsIterable(var, forindex, tagToken);
      } else {
        return values.get(forindex % values.size());
      }
    }
    String item = values.get(forindex % values.size());
    if (
      !chunkResolver.getDeferredWords().isEmpty() &&
      ChunkResolver.shouldBeEvaluated(item, tagToken, interpreter)
    ) {
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
      String.format("%s if %s is iterable %s", tokenStart, var, tokenEnd) +
      // modulo indexing
      String.format("{{ %s[%d %% %s|length] }}", var, forIndex, var) +
      String.format("%s else %s", tokenStart, tokenEnd) +
      String.format("{{ %s }}", var) +
      String.format("%s endif %s", tokenStart, tokenEnd)
    );
  }
}
