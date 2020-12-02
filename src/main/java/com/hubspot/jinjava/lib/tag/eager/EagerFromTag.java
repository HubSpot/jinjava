package com.hubspot.jinjava.lib.tag.eager;

import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.tag.FromTag;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.parse.TagToken;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class EagerFromTag extends EagerStateChangingTag<FromTag> {

  public EagerFromTag() {
    super(new FromTag());
  }

  @Override
  public String getEagerTagImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    List<String> helper = FromTag.getHelpers(tagToken);

    Optional<String> maybeTemplateFile = FromTag.getTemplateFile(
      helper,
      tagToken,
      interpreter
    );
    if (!maybeTemplateFile.isPresent()) {
      return "";
    }
    String templateFile = maybeTemplateFile.get();
    try {
      Map<String, String> imports = FromTag.getImportMap(helper);

      try {
        String template = interpreter.getResource(templateFile);
        Node node = interpreter.parse(template);

        JinjavaInterpreter child = interpreter
          .getConfig()
          .getInterpreterFactory()
          .newInstance(interpreter);
        child.getContext().put(Context.IMPORT_RESOURCE_PATH_KEY, templateFile);
        JinjavaInterpreter.pushCurrent(child);
        String output;
        try {
          output = child.render(node);
        } finally {
          JinjavaInterpreter.popCurrent();
        }

        interpreter.addAllChildErrors(templateFile, child.getErrorsCopy());

        if (!child.getContext().getDeferredNodes().isEmpty()) {
          FromTag.handleDeferredNodesDuringImport(
            tagToken,
            templateFile,
            imports,
            child,
            interpreter
          );
        }

        FromTag.integrateChild(imports, child, interpreter);
        Map<String, String> newToOldImportNames = renameMacros(imports, interpreter)
          .entrySet()
          .stream()
          .filter(e -> !e.getKey().equals(e.getValue()))
          .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        if (child.getContext().getEagerTokens().isEmpty() || output == null) {
          output = "";
        } else if (newToOldImportNames.size() > 0) {
          // Set after output
          output =
            output +
            buildSetTagForDeferredInChildContext(newToOldImportNames, interpreter, true);
        }
        return output;
      } catch (IOException e) {
        throw new InterpretException(
          e.getMessage(),
          e,
          tagToken.getLineNumber(),
          tagToken.getStartPosition()
        );
      }
    } finally {
      interpreter.getContext().popFromStack();
    }
  }

  private static Map<String, String> renameMacros(
    Map<String, String> oldToNewImportNames,
    JinjavaInterpreter interpreter
  ) {
    Set<String> toRemove = new HashSet<>();
    Map<String, MacroFunction> macroFunctions = oldToNewImportNames
      .entrySet()
      .stream()
      .filter(
        e ->
          !e.getKey().equals(e.getValue()) &&
          !interpreter.getContext().containsKey(e.getKey()) &&
          interpreter.getContext().isGlobalMacro(e.getKey())
      )
      .peek(entry -> toRemove.add(entry.getKey()))
      .collect(
        Collectors.toMap(
          Map.Entry::getValue,
          e -> interpreter.getContext().getGlobalMacro(e.getKey())
        )
      );

    macroFunctions.forEach(
      (key, value) ->
        interpreter.getContext().addGlobalMacro(new MacroFunction(value, key))
    );
    toRemove.forEach(oldToNewImportNames::remove);
    return oldToNewImportNames;
  }
}
