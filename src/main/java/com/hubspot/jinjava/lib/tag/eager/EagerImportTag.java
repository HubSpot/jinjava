package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.tag.ImportTag;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.loader.RelativePathResolver;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

@Beta
public class EagerImportTag extends EagerStateChangingTag<ImportTag> {

  public EagerImportTag() {
    super(new ImportTag());
  }

  public EagerImportTag(ImportTag importTag) {
    super(importTag);
  }

  @Override
  public String getEagerTagImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    List<String> helper = ImportTag.getHelpers(tagToken);

    String currentImportAlias = ImportTag.getContextVar(helper);

    final String initialPathSetter = getSetTagForCurrentPath(interpreter);
    final String newPathSetter;

    Optional<String> maybeTemplateFile;
    try {
      maybeTemplateFile = ImportTag.getTemplateFile(helper, tagToken, interpreter);
    } catch (DeferredValueException e) {
      if (currentImportAlias.isEmpty()) {
        throw e;
      }
      return (
        initialPathSetter +
        EagerReconstructionUtils.handleDeferredTokenAndReconstructReferences(
          interpreter,
          new DeferredToken(
            tagToken,
            Collections.singleton(helper.get(0)),
            Collections.singleton(currentImportAlias)
          )
        ) +
        tagToken.getImage()
      );
    }
    if (!maybeTemplateFile.isPresent()) {
      return "";
    }
    String templateFile = maybeTemplateFile.get();
    try {
      Node node = ImportTag.parseTemplateAsNode(interpreter, templateFile);
      newPathSetter = getSetTagForCurrentPath(interpreter);

      JinjavaInterpreter child = interpreter
        .getConfig()
        .getInterpreterFactory()
        .newInstance(interpreter);
      child.getContext().put(Context.IMPORT_RESOURCE_PATH_KEY, templateFile);
      JinjavaInterpreter.pushCurrent(child);
      String output;
      try {
        setupImportAlias(currentImportAlias, child, interpreter);
        output = child.render(node);
      } finally {
        JinjavaInterpreter.popCurrent();
      }
      interpreter.addAllChildErrors(templateFile, child.getErrorsCopy());
      Map<String, Object> childBindings = child.getContext().getSessionBindings();

      // If the template depends on deferred values it should not be rendered,
      // and all defined variables and macros should be deferred too.
      if (
        !child.getContext().getDeferredNodes().isEmpty() ||
        (
          interpreter.getContext().isDeferredExecutionMode() &&
          !child.getContext().getGlobalMacros().isEmpty()
        )
      ) {
        ImportTag.handleDeferredNodesDuringImport(
          node,
          currentImportAlias,
          childBindings,
          child,
          interpreter
        );
        throw new DeferredValueException(
          templateFile,
          tagToken.getLineNumber(),
          tagToken.getStartPosition()
        );
      }
      integrateChild(currentImportAlias, childBindings, child, interpreter);
      String finalOutput;
      if (child.getContext().getDeferredTokens().isEmpty() || output == null) {
        return "";
      } else if (!Strings.isNullOrEmpty(currentImportAlias)) {
        // Since some values got deferred, output a DoTag that will load the currentImportAlias on the context.
        finalOutput =
          getFinalOutputWithAlias(
            interpreter,
            currentImportAlias,
            initialPathSetter,
            newPathSetter,
            output,
            childBindings
          );
      } else {
        finalOutput =
          getFinalOutputWithoutAlias(
            interpreter,
            currentImportAlias,
            initialPathSetter,
            newPathSetter,
            output,
            childBindings
          );
      }
      return EagerReconstructionUtils.buildBlockSetTag(
        SetTag.IGNORED_VARIABLE_NAME,
        finalOutput,
        interpreter,
        true
      );
    } catch (IOException e) {
      throw new InterpretException(
        e.getMessage(),
        e,
        tagToken.getLineNumber(),
        tagToken.getStartPosition()
      );
    } finally {
      interpreter.getContext().getCurrentPathStack().pop();
      interpreter.getContext().getImportPathStack().pop();
    }
  }

  private String getFinalOutputWithoutAlias(
    JinjavaInterpreter interpreter,
    String currentImportAlias,
    String initialPathSetter,
    String newPathSetter,
    String output,
    Map<String, Object> childBindings
  ) {
    return (
      newPathSetter +
      getSetTagForDeferredChildBindings(interpreter, currentImportAlias, childBindings) +
      output +
      initialPathSetter
    );
  }

  private String getFinalOutputWithAlias(
    JinjavaInterpreter interpreter,
    String currentImportAlias,
    String initialPathSetter,
    String newPathSetter,
    String output,
    Map<String, Object> childBindings
  ) {
    return (
      newPathSetter +
      getSetTagForDeferredChildBindings(interpreter, currentImportAlias, childBindings) +
      EagerReconstructionUtils.buildSetTag(
        ImmutableMap.of(currentImportAlias, "{}"),
        interpreter,
        true
      ) +
      wrapInChildScopeIfNecessary(interpreter, output, currentImportAlias) +
      initialPathSetter
    );
  }

  private static String wrapInChildScopeIfNecessary(
    JinjavaInterpreter interpreter,
    String output,
    String currentImportAlias
  ) {
    String combined = output + getDoTagToPreserve(interpreter, currentImportAlias);
    // So that any set variables other than the alias won't exist outside the child's scope
    if (interpreter.getContext().isDeferredExecutionMode()) {
      return EagerReconstructionUtils.wrapInChildScope(combined, interpreter);
    }
    return combined;
  }

  private String getSetTagForDeferredChildBindings(
    JinjavaInterpreter interpreter,
    String currentImportAlias,
    Map<String, Object> childBindings
  ) {
    if (
      Strings.isNullOrEmpty(currentImportAlias) &&
      interpreter.getContext().isDeferredExecutionMode()
    ) {
      Set<String> metaContextVariables = interpreter
        .getContext()
        .getMetaContextVariables();
      // defer imported variables
      EagerReconstructionUtils.buildSetTag(
        childBindings
          .entrySet()
          .stream()
          .filter(
            entry ->
              !(entry.getValue() instanceof DeferredValue) && entry.getValue() != null
          )
          .filter(entry -> !metaContextVariables.contains(entry.getKey()))
          .collect(Collectors.toMap(Entry::getKey, entry -> "")),
        interpreter,
        true
      );
    }
    return EagerReconstructionUtils.buildSetTag(
      childBindings
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue() instanceof DeferredValue)
        .filter(entry -> !interpreter.getContext().containsKey(entry.getKey()))
        .filter(entry -> !entry.getKey().equals(currentImportAlias))
        .collect(
          Collectors.toMap(
            Entry::getKey,
            entry ->
              PyishObjectMapper.getAsPyishString(
                ((DeferredValue) entry.getValue()).getOriginalValue()
              )
          )
        ),
      interpreter,
      false // false so that we don't defer them on higher context scopes; they only exist in the child scope
    );
  }

  public static String getSetTagForCurrentPath(JinjavaInterpreter interpreter) {
    return EagerReconstructionUtils.buildSetTag(
      ImmutableMap.of(
        RelativePathResolver.CURRENT_PATH_CONTEXT_KEY,
        PyishObjectMapper.getAsPyishString(
          interpreter
            .getContext()
            .getCurrentPathStack()
            .peek()
            .orElseGet(
              () ->
                (String) interpreter
                  .getContext()
                  .getOrDefault(RelativePathResolver.CURRENT_PATH_CONTEXT_KEY, "")
            )
        )
      ),
      interpreter,
      false
    );
  }

  @SuppressWarnings("unchecked")
  private static String getDoTagToPreserve(
    JinjavaInterpreter interpreter,
    String currentImportAlias
  ) {
    StringJoiner keyValueJoiner = new StringJoiner(",");
    Object currentAliasMap = interpreter
      .getContext()
      .getSessionBindings()
      .get(currentImportAlias);
    for (Map.Entry<String, Object> entry : (
      (Map<String, Object>) ((DeferredValue) currentAliasMap).getOriginalValue()
    ).entrySet()) {
      if (entry.getKey().equals(currentImportAlias)) {
        continue;
      }
      if (entry.getValue() instanceof DeferredValue) {
        keyValueJoiner.add(String.format("'%s': %s", entry.getKey(), entry.getKey()));
      } else if (!(entry.getValue() instanceof MacroFunction)) {
        keyValueJoiner.add(
          String.format(
            "'%s': %s",
            entry.getKey(),
            PyishObjectMapper.getAsPyishString(entry.getValue())
          )
        );
      }
    }
    if (keyValueJoiner.length() > 0) {
      return EagerReconstructionUtils.buildDoUpdateTag(
        currentImportAlias,
        "{" + keyValueJoiner.toString() + "}",
        interpreter
      );
    }
    return "";
  }

  @VisibleForTesting
  public static void setupImportAlias(
    String currentImportAlias,
    JinjavaInterpreter child,
    JinjavaInterpreter parent
  ) {
    if (!Strings.isNullOrEmpty(currentImportAlias)) {
      Optional<String> maybeParentImportAlias = parent
        .getContext()
        .getImportResourceAlias();
      if (maybeParentImportAlias.isPresent()) {
        child
          .getContext()
          .getScope()
          .put(
            Context.IMPORT_RESOURCE_ALIAS_KEY,
            String.format("%s.%s", maybeParentImportAlias.get(), currentImportAlias)
          );
      } else {
        child
          .getContext()
          .getScope()
          .put(Context.IMPORT_RESOURCE_ALIAS_KEY, currentImportAlias);
      }
      constructFullAliasPathMap(currentImportAlias, child);
      getMapForCurrentContextAlias(currentImportAlias, child);
    }
  }

  @SuppressWarnings("unchecked")
  private static void constructFullAliasPathMap(
    String currentImportAlias,
    JinjavaInterpreter child
  ) {
    String fullImportAlias = child
      .getContext()
      .getImportResourceAlias()
      .orElse(currentImportAlias);
    String[] allAliases = fullImportAlias.split("\\.");
    Map<String, Object> currentMap = child.getContext().getParent();
    for (int i = 0; i < allAliases.length - 1; i++) {
      Object maybeNextMap = currentMap.get(allAliases[i]);
      if (maybeNextMap instanceof Map) {
        currentMap = (Map<String, Object>) maybeNextMap;
      } else if (
        maybeNextMap instanceof DeferredValue &&
        ((DeferredValue) maybeNextMap).getOriginalValue() instanceof Map
      ) {
        currentMap =
          (Map<String, Object>) ((DeferredValue) maybeNextMap).getOriginalValue();
      } else {
        throw new InterpretException("Encountered a problem with import alias maps");
      }
    }
    currentMap.put(
      allAliases[allAliases.length - 1],
      child.getContext().isDeferredExecutionMode()
        ? DeferredValue.instance(new PyMap(new HashMap<>()))
        : new PyMap(new HashMap<>())
    );
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getMapForCurrentContextAlias(
    String currentImportAlias,
    JinjavaInterpreter child
  ) {
    Object parentValueForChild = child
      .getContext()
      .getParent()
      .getSessionBindings()
      .get(currentImportAlias);
    if (parentValueForChild instanceof Map) {
      return (Map<String, Object>) parentValueForChild;
    } else if (parentValueForChild instanceof DeferredValue) {
      if (((DeferredValue) parentValueForChild).getOriginalValue() instanceof Map) {
        return (Map<String, Object>) (
          (DeferredValue) parentValueForChild
        ).getOriginalValue();
      }
      Map<String, Object> newMap = new PyMap(new HashMap<>());
      child
        .getContext()
        .getParent()
        .put(currentImportAlias, DeferredValue.instance(newMap));
      return newMap;
    } else {
      Map<String, Object> newMap = new PyMap(new HashMap<>());
      child
        .getContext()
        .getParent()
        .put(
          currentImportAlias,
          child.getContext().isDeferredExecutionMode()
            ? DeferredValue.instance(newMap)
            : newMap
        );
      return newMap;
    }
  }

  @VisibleForTesting
  public static void integrateChild(
    String currentImportAlias,
    Map<String, Object> childBindings,
    JinjavaInterpreter child,
    JinjavaInterpreter parent
  ) {
    childBindings.remove(SetTag.IGNORED_VARIABLE_NAME);
    for (MacroFunction macro : child.getContext().getGlobalMacros().values()) {
      if (parent.getContext().isDeferredExecutionMode()) {
        macro.setDeferred(true);
      }
    }
    if (StringUtils.isBlank(currentImportAlias)) {
      for (MacroFunction macro : child.getContext().getGlobalMacros().values()) {
        parent.getContext().addGlobalMacro(macro);
      }
      childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);
      childBindings.remove(Context.IMPORT_RESOURCE_ALIAS_KEY);
      Map<String, Object> childBindingsWithoutImportResourcePath = ImportTag.getChildBindingsWithoutImportResourcePath(
        childBindings
      );
      if (parent.getContext().isDeferredExecutionMode()) {
        childBindingsWithoutImportResourcePath
          .keySet()
          .forEach(
            key ->
              parent
                .getContext()
                .put(key, DeferredValue.instance(parent.getContext().get(key)))
          );
      } else {
        parent.getContext().putAll(childBindingsWithoutImportResourcePath);
      }
    } else {
      if (
        child.getContext().isDeferredExecutionMode() &&
        child
          .getContext()
          .getDeferredTokens()
          .stream()
          .flatMap(deferredToken -> deferredToken.getSetDeferredWords().stream())
          .collect(Collectors.toSet())
          .contains(currentImportAlias)
      ) {
        // since a child scope will be used, the import alias would not be properly reconstructed
        throw new DeferredValueException(
          "Same-named variable as import alias: " + currentImportAlias
        );
      }
      childBindings.putAll(child.getContext().getGlobalMacros());
      Map<String, Object> mapForCurrentContextAlias = getMapForCurrentContextAlias(
        currentImportAlias,
        child
      );
      // Remove layers from self down to original import alias to prevent reference loops
      Arrays
        .stream(
          child
            .getContext()
            .getImportResourceAlias()
            .orElse(currentImportAlias)
            .split("\\.")
        )
        .filter(
          key ->
            mapForCurrentContextAlias ==
            (
              childBindings.get(key) instanceof DeferredValue
                ? ((DeferredValue) childBindings.get(key)).getOriginalValue()
                : childBindings.get(key)
            )
        )
        .forEach(childBindings::remove);
      // Remove meta keys
      childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);
      childBindings.remove(Context.IMPORT_RESOURCE_ALIAS_KEY);
      mapForCurrentContextAlias.putAll(childBindings);
    }
  }
}
