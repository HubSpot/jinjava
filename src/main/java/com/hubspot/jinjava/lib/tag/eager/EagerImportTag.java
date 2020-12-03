package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.tag.ImportTag;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.parse.TagToken;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class EagerImportTag extends EagerStateChangingTag<ImportTag> {

  public EagerImportTag() {
    super(new ImportTag());
  }

  @Override
  public String getEagerTagImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    List<String> helper = ImportTag.getHelpers(tagToken);

    String contextVar = ImportTag.getContextVar(helper);

    Optional<String> maybeTemplateFile = ImportTag.getTemplateFile(
      helper,
      tagToken,
      interpreter
    );
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
      if (!Strings.isNullOrEmpty(contextVar)) {
        if (interpreter.getContext().containsKey(Context.IMPORT_RESOURCE_ALIAS)) {
          child
            .getContext()
            .getScope()
            .put(
              Context.IMPORT_RESOURCE_ALIAS,
              String.format(
                "%s.%s",
                interpreter.getContext().get(Context.IMPORT_RESOURCE_ALIAS),
                contextVar
              )
            );
        } else {
          child.getContext().getScope().put(Context.IMPORT_RESOURCE_ALIAS, contextVar);
        }
        child
          .getContext()
          .getScope()
          .put(
            String.valueOf(child.getContext().get(Context.IMPORT_RESOURCE_ALIAS)),
            new PyMap(new HashMap<>())
          );
      }
      String output;
      try {
        output = child.render(node);
      } finally {
        JinjavaInterpreter.popCurrent();
      }

      interpreter.addAllChildErrors(templateFile, child.getErrorsCopy());

      Map<String, Object> childBindings = child.getContext().getSessionBindings();

      // If the template depends on deferred values it should not be rendered and all defined variables and macros should be deferred too
      if (!child.getContext().getDeferredNodes().isEmpty()) {
        ImportTag.handleDeferredNodesDuringImport(
          tagToken,
          node,
          contextVar,
          templateFile,
          childBindings,
          child,
          interpreter
        );
      }

      integrateChild(contextVar, childBindings, child, interpreter);
      if (child.getContext().getEagerTokens().isEmpty() || output == null) {
        output = "";
      } else if (child.getContext().containsKey(Context.IMPORT_RESOURCE_ALIAS)) {
        // Start it as a new dictionary before output
        output =
          buildSetTagForDeferredInChildContext(
            ImmutableMap.of(
              (String) child.getContext().get(Context.IMPORT_RESOURCE_ALIAS),
              "{}"
            ),
            interpreter,
            true
          ) +
          output;
      }
      child.getContext().getScope().remove(Context.IMPORT_RESOURCE_ALIAS);
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
    }
  }

  private static void integrateChild(
    String contextVar,
    Map<String, Object> childBindings,
    JinjavaInterpreter child,
    JinjavaInterpreter parent
  ) {
    if (StringUtils.isBlank(contextVar)) {
      for (MacroFunction macro : child.getContext().getGlobalMacros().values()) {
        parent.getContext().addGlobalMacro(macro);
      }
      childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);
      parent.getContext().putAll(childBindings);
    } else {
      for (Map.Entry<String, MacroFunction> macro : child
        .getContext()
        .getGlobalMacros()
        .entrySet()) {
        childBindings.put(macro.getKey(), macro.getValue());
      }

      if (childBindings.get(contextVar) instanceof DeferredValue) {
        flattenDeferredContextVar(contextVar, childBindings);
      } else {
        for (Map.Entry<String, Object> aliasBinding : (
          (PyMap) childBindings.get(contextVar)
        ).entrySet()) {
          childBindings.put(aliasBinding.getKey(), aliasBinding.getValue());
        }
      }
      childBindings.remove(contextVar);
      childBindings.remove(Context.GLOBAL_MACROS_SCOPE_KEY);
      childBindings.remove(Context.IMPORT_RESOURCE_ALIAS);
      putBindingsOnContext(contextVar, childBindings, parent);
    }
  }

  private static void putBindingsOnContext(
    String contextVar,
    Map<String, Object> childBindings,
    JinjavaInterpreter parent
  ) {
    Object parentContextVarValue = parent.getContext().getScope().get(contextVar);
    if (parentContextVarValue instanceof DeferredValue) {
      Object originalValue = ((DeferredValue) parentContextVarValue).getOriginalValue();
      if (originalValue instanceof PyMap) {
        ((PyMap) originalValue).putAll(childBindings);
        return;
      }
    }
    parent.getContext().put(contextVar, childBindings);
  }

  private static void flattenDeferredContextVar(
    String contextVar,
    Map<String, Object> childBindings
  ) {
    DeferredValue contextVarMap = (DeferredValue) childBindings.get(contextVar);
    if (contextVarMap.getOriginalValue() instanceof PyMap) {
      for (Map.Entry<String, Object> deferredBinding : (
        (PyMap) contextVarMap.getOriginalValue()
      ).entrySet()) {
        childBindings.put(
          deferredBinding.getKey(),
          deferredBinding.getValue() instanceof DeferredValue
            ? deferredBinding.getValue()
            : DeferredValue.instance(deferredBinding.getValue())
        );
      }
    }
  }
}
