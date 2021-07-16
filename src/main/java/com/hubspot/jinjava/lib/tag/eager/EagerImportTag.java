package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.tag.ImportTag;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.parse.TagToken;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import org.apache.commons.lang3.StringUtils;

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

    Optional<String> maybeTemplateFile;
    try {
      maybeTemplateFile = ImportTag.getTemplateFile(helper, tagToken, interpreter);
    } catch (DeferredValueException e) {
      if (currentImportAlias.isEmpty()) {
        throw e;
      }
      interpreter.getContext().put(currentImportAlias, DeferredValue.instance());
      return tagToken.getImage();
    }
    if (!maybeTemplateFile.isPresent()) {
      return "";
    }
    String templateFile = maybeTemplateFile.get();
    try {
      Node node = ImportTag.parseTemplateAsNode(interpreter, templateFile);

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
      if (!child.getContext().getDeferredNodes().isEmpty()) {
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
      if (child.getContext().getEagerTokens().isEmpty() || output == null) {
        output = "";
      } else if (!Strings.isNullOrEmpty(currentImportAlias)) {
        // Since some values got deferred, output a DoTag that will load the currentImportAlias on the context.
        return output + getDoTagToPreserve(interpreter, currentImportAlias);
      }
      return output;
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
    if ((!(currentAliasMap instanceof DeferredValue))) {
      // Make sure that the map is deferred.
      if (!(currentAliasMap instanceof Map)) {
        currentAliasMap = new PyMap(new HashMap<>());
      }
      currentAliasMap = DeferredValue.instance(currentAliasMap);
      interpreter.getContext().put(currentImportAlias, currentAliasMap);
    }
    for (Map.Entry<String, Object> entry : (
      (Map<String, Object>) ((DeferredValue) currentAliasMap).getOriginalValue()
    ).entrySet()) {
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
      return buildDoUpdateTag(
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
    currentMap.put(allAliases[allAliases.length - 1], new PyMap(new HashMap<>()));
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
      child.getContext().getParent().put(currentImportAlias, newMap);
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
    if (StringUtils.isBlank(currentImportAlias)) {
      for (MacroFunction macro : child.getContext().getGlobalMacros().values()) {
        parent.getContext().addGlobalMacro(macro);
      }
      childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);
      childBindings.remove(Context.IMPORT_RESOURCE_ALIAS_KEY);
      parent.getContext().putAll(childBindings);
    } else {
      Map<String, MacroFunction> globalMacros = child.getContext().getGlobalMacros();
      for (Map.Entry<String, MacroFunction> macro : globalMacros.entrySet()) {
        childBindings.put(macro.getKey(), macro.getValue());
      }
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
