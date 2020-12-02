package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.ImportTag;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.parse.TagToken;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

      ImportTag.integrateChild(contextVar, childBindings, child, interpreter);
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
}
