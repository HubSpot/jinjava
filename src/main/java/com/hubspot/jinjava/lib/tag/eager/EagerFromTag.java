package com.hubspot.jinjava.lib.tag.eager;

import static com.hubspot.jinjava.lib.tag.SetTag.IGNORED_VARIABLE_NAME;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.tag.FromTag;
import com.hubspot.jinjava.loader.RelativePathResolver;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Beta
public class EagerFromTag extends EagerStateChangingTag<FromTag> {

  public EagerFromTag() {
    super(new FromTag());
  }

  public EagerFromTag(FromTag fromTag) {
    super(fromTag);
  }

  @Override
  public String getEagerTagImage(TagToken tagToken, JinjavaInterpreter interpreter) {
    List<String> helper = FromTag.getHelpers(tagToken);
    Map<String, String> imports = FromTag.getImportMap(helper);
    Optional<String> maybeTemplateFile;
    try {
      maybeTemplateFile = FromTag.getTemplateFile(helper, tagToken, interpreter);
    } catch (DeferredValueException e) {
      imports
        .values()
        .forEach(
          value -> {
            MacroFunction deferredMacro = new MacroFunction(
              null,
              value,
              null,
              false,
              null,
              tagToken.getLineNumber(),
              tagToken.getStartPosition()
            );
            deferredMacro.setDeferred(true);
            interpreter.getContext().addGlobalMacro(deferredMacro);
          }
        );
      return (
        EagerReconstructionUtils.buildSetTag(
          ImmutableMap.of(
            RelativePathResolver.CURRENT_PATH_CONTEXT_KEY,
            PyishObjectMapper.getAsPyishString(
              interpreter.getContext().get(RelativePathResolver.CURRENT_PATH_CONTEXT_KEY)
            )
          ),
          interpreter,
          false
        ) +
        tagToken.getImage()
      );
    }
    if (!maybeTemplateFile.isPresent()) {
      return "";
    }
    String templateFile = maybeTemplateFile.get();
    try {
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
        if (child.getContext().getDeferredTokens().isEmpty() || output == null) {
          return "";
        } else if (newToOldImportNames.size() > 0) {
          // Set after output
          output =
            output +
            EagerReconstructionUtils.buildSetTag(newToOldImportNames, interpreter, true);
        }
        return EagerReconstructionUtils.buildBlockSetTag(
          IGNORED_VARIABLE_NAME,
          output,
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
