package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.fn.eager.EagerMacroFunction;
import com.hubspot.jinjava.lib.tag.DoTag;
import com.hubspot.jinjava.lib.tag.FromTag;
import com.hubspot.jinjava.lib.tag.eager.importing.EagerImportingStrategyFactory;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import com.hubspot.jinjava.util.PrefixToPreserveState;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    String initialPathSetter = EagerImportingStrategyFactory.getSetTagForCurrentPath(
      interpreter
    );
    List<String> helper = FromTag.getHelpers(tagToken);
    Map<String, String> imports = FromTag.getImportMap(helper);
    Optional<String> maybeTemplateFile;
    try {
      maybeTemplateFile = FromTag.getTemplateFile(helper, tagToken, interpreter);
    } catch (DeferredValueException e) {
      imports
        .values()
        .forEach(value -> {
          MacroFunction deferredMacro = new EagerMacroFunction(
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
        });
      return (
        initialPathSetter +
        new PrefixToPreserveState(
          EagerReconstructionUtils.handleDeferredTokenAndReconstructReferences(
            interpreter,
            DeferredToken
              .builderFromToken(tagToken)
              .addUsedDeferredWords(Stream.of(helper.get(0)))
              .addUsedDeferredWords(imports.keySet())
              .addSetDeferredWords(imports.values())
              .build()
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
      try {
        interpreter
          .getContext()
          .getCurrentPathStack()
          .push(templateFile, tagToken.getLineNumber(), tagToken.getStartPosition());

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
          interpreter.getContext().getCurrentPathStack().pop();
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
        Map<String, String> newToOldImportNames = getNewToOldWithoutMacros(
          imports,
          interpreter
        );
        if (child.getContext().getDeferredTokens().isEmpty() || output == null) {
          return "";
        } else if (newToOldImportNames.size() > 0) {
          // Set after output
          output =
            output +
            EagerReconstructionUtils.buildSetTag(newToOldImportNames, interpreter, true);
        }
        return EagerReconstructionUtils.wrapInTag(
          output,
          DoTag.TAG_NAME,
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

  private static Map<String, String> getNewToOldWithoutMacros(
    Map<String, String> oldToNewImportNames,
    JinjavaInterpreter interpreter
  ) {
    return oldToNewImportNames
      .entrySet()
      .stream()
      .filter(e -> !e.getKey().equals(e.getValue()))
      .filter(e ->
        interpreter.getContext().containsKey(e.getValue()) ||
        !interpreter.getContext().isGlobalMacro(e.getValue())
      )
      .collect(Collectors.toMap(Entry::getValue, Entry::getKey)); // flip order
  }
}
