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
    interpreter
      .getContext()
      .addNonMetaContextVariables(
        Arrays.stream(variables).map(String::trim).collect(Collectors.toList())
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
      eagerExecutionResult.getResult().getDeferredWords(),
      interpreter
    );
    EagerReconstructionUtils.hydrateReconstructionFromContextBeforeDeferring(
      prefixToPreserveState,
      Arrays
        .stream(variables)
        .filter(var -> var.contains("."))
        .collect(Collectors.toSet()),
      interpreter
    );
    return prefixToPreserveState;
  }

  public static String getSuffixToPreserveState(
    List<String> varList,
    JinjavaInterpreter interpreter
  ) {
    if (varList.isEmpty()) {
      return "";
    }
    return getSuffixToPreserveState(varList.stream(), interpreter);
  }

  public static String getSuffixToPreserveState(
    String[] varList,
    JinjavaInterpreter interpreter
  ) {
    if (varList.length == 0) {
      return "";
    }
    return getSuffixToPreserveState(Arrays.stream(varList), interpreter);
  }

  private static String getSuffixToPreserveState(
    Stream<String> varStream,
    JinjavaInterpreter interpreter
  ) {
    StringBuilder suffixToPreserveState = new StringBuilder();
    Optional<String> maybeTemporaryImportAlias =
      AliasedEagerImportingStrategy.getTemporaryImportAlias(interpreter.getContext());
    if (maybeTemporaryImportAlias.isPresent()) {
      boolean stillInsideImportTag = interpreter
        .getContext()
        .containsKey(maybeTemporaryImportAlias.get());
      List<String> filteredVars = varStream
        .filter(var -> !AliasedEagerImportingStrategy.isTemporaryImportAlias(var))
        .filter(var ->
          !interpreter.getContext().getComputedMetaContextVariables().contains(var)
        )
        .peek(var -> {
          if (!stillInsideImportTag) {
            if (
              interpreter.retraceVariable(
                String.format(
                  "%s.%s",
                  interpreter.getContext().getImportResourceAlias().get(),
                  var
                ),
                -1
              ) !=
              null
            ) {
              throw new DeferredValueException(
                "Cannot modify temporary import alias outside of import tag"
              );
            }
          }
        })
        .collect(Collectors.toList());
      if (filteredVars.isEmpty()) {
        return "";
      }
      String updateString = getUpdateString(filteredVars);
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

  private static String getUpdateString(List<String> varList) {
    StringJoiner updateString = new StringJoiner(",");
    // Update the alias map to the value of the set variable.
    varList.forEach(var -> updateString.add(String.format("'%s': %s", var, var)));
    return "{" + updateString + "}";
  }
}
