package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.lib.tag.eager.importing.AliasedEagerImportingStrategy;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import com.hubspot.jinjava.util.PrefixToPreserveState;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Triple;

@Beta
public abstract class EagerSetTagStrategy {
  protected final SetTag setTag;

  protected EagerSetTagStrategy(SetTag setTag) {
    this.setTag = setTag;
  }

  public String run(TagNode tagNode, JinjavaInterpreter interpreter) {
    int eqPos = tagNode.getHelpers().indexOf('=');
    String[] variables;
    String expression;
    if (eqPos > 0) {
      variables = tagNode.getHelpers().substring(0, eqPos).trim().split(",");
      expression = tagNode.getHelpers().substring(eqPos + 1).trim();
    } else {
      int filterPos = tagNode.getHelpers().indexOf('|');
      String var = tagNode.getHelpers().trim();
      if (filterPos >= 0) {
        var = tagNode.getHelpers().substring(0, filterPos).trim();
      }
      variables = new String[] { var };
      expression = tagNode.getHelpers();
    }

    EagerReconstructionUtils.removeMetaContextVariables(
      Arrays.stream(variables).map(String::trim),
      interpreter.getContext()
    );

    EagerExecutionResult eagerExecutionResult = getEagerExecutionResult(
      tagNode,
      variables,
      expression,
      interpreter
    );
    if (
      eagerExecutionResult.getResult().isFullyResolved() &&
      !interpreter.getContext().isDeferredExecutionMode()
    ) {
      EagerReconstructionUtils.commitSpeculativeBindings(
        interpreter,
        eagerExecutionResult
      );
      Optional<String> maybeResolved = resolveSet(
        tagNode,
        variables,
        eagerExecutionResult,
        interpreter
      );
      if (maybeResolved.isPresent()) {
        return maybeResolved.get();
      }
    }
    Triple<String, String, String> triple = getPrefixTokenAndSuffix(
      tagNode,
      variables,
      eagerExecutionResult,
      interpreter
    );
    if (
      eagerExecutionResult.getResult().isFullyResolved() &&
      interpreter.getContext().isDeferredExecutionMode()
    ) {
      attemptResolve(tagNode, variables, eagerExecutionResult, interpreter);
    }
    return buildImage(tagNode, variables, eagerExecutionResult, triple, interpreter);
  }

  protected abstract EagerExecutionResult getEagerExecutionResult(
    TagNode tagNode,
    String[] variables,
    String expression,
    JinjavaInterpreter interpreter
  );

  protected abstract Optional<String> resolveSet(
    TagNode tagNode,
    String[] variables,
    EagerExecutionResult resolvedValues,
    JinjavaInterpreter interpreter
  );

  protected abstract Triple<String, String, String> getPrefixTokenAndSuffix(
    TagNode tagNode,
    String[] variables,
    EagerExecutionResult eagerExecutionResult,
    JinjavaInterpreter interpreter
  );

  protected abstract void attemptResolve(
    TagNode tagNode,
    String[] variables,
    EagerExecutionResult resolvedValues,
    JinjavaInterpreter interpreter
  );

  protected abstract String buildImage(
    TagNode tagNode,
    String[] variables,
    EagerExecutionResult eagerExecutionResult,
    Triple<String, String, String> triple,
    JinjavaInterpreter interpreter
  );

  protected PrefixToPreserveState getPrefixToPreserveState(
    EagerExecutionResult eagerExecutionResult,
    String[] variables,
    JinjavaInterpreter interpreter
  ) {
    PrefixToPreserveState prefixToPreserveState = new PrefixToPreserveState();
    if (
      !eagerExecutionResult.getResult().isFullyResolved() ||
      interpreter.getContext().isDeferredExecutionMode()
    ) {
      prefixToPreserveState.putAll(eagerExecutionResult.getPrefixToPreserveState());
    } else {
      EagerReconstructionUtils.commitSpeculativeBindings(
        interpreter,
        eagerExecutionResult
      );
    }
    EagerReconstructionUtils.hydrateReconstructionFromContextBeforeDeferring(
      prefixToPreserveState,
      Stream
        .concat(
          eagerExecutionResult.getResult().getDeferredWords().stream(),
          Arrays.stream(variables).filter(var -> var.contains("."))
        )
        .collect(Collectors.toSet()),
      interpreter
    );
    return prefixToPreserveState;
  }

  public static String getSuffixToPreserveState(
    String variables,
    JinjavaInterpreter interpreter
  ) {
    if (variables.isEmpty()) {
      return "";
    }
    StringBuilder suffixToPreserveState = new StringBuilder();
    Optional<String> maybeTemporaryImportAlias = AliasedEagerImportingStrategy.getTemporaryImportAlias(
      interpreter.getContext()
    );
    if (
      maybeTemporaryImportAlias.isPresent() &&
      !AliasedEagerImportingStrategy.isTemporaryImportAlias(variables) &&
      !interpreter.getContext().getMetaContextVariables().contains(variables)
    ) {
      if (!interpreter.getContext().containsKey(maybeTemporaryImportAlias.get())) {
        throw new DeferredValueException(
          "Cannot modify temporary import alias outside of import tag"
        );
      }
      String updateString = getUpdateString(variables);

      // Don't need to render because the temporary import alias's value is always deferred, and rendering will do nothing
      suffixToPreserveState.append(
        EagerReconstructionUtils.buildDoUpdateTag(
          maybeTemporaryImportAlias.get(),
          updateString,
          interpreter
        )
      );
    }

    return suffixToPreserveState.toString();
  }

  private static String getUpdateString(String variables) {
    List<String> varList = Arrays
      .stream(variables.split(","))
      .map(String::trim)
      .collect(Collectors.toList());
    StringJoiner updateString = new StringJoiner(",");
    // Update the alias map to the value of the set variable.
    varList.forEach(var -> updateString.add(String.format("'%s': %s", var, var)));
    return "{" + updateString + "}";
  }
}
