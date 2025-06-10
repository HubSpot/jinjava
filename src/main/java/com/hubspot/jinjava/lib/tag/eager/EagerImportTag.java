package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.AutoCloseableSupplier.AutoCloseableImpl;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.DoTag;
import com.hubspot.jinjava.lib.tag.ImportTag;
import com.hubspot.jinjava.lib.tag.eager.importing.EagerImportingStrategy;
import com.hubspot.jinjava.lib.tag.eager.importing.EagerImportingStrategyFactory;
import com.hubspot.jinjava.lib.tag.eager.importing.ImportingData;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

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
    ImportingData importingData = EagerImportingStrategyFactory.getImportingData(
      tagToken,
      interpreter
    );
    EagerImportingStrategy eagerImportingStrategy = EagerImportingStrategyFactory.create(
      importingData
    );

    try (
      AutoCloseableImpl<Optional<String>> maybeTemplateFile = ImportTag
        .getTemplateFileWithWrapper(importingData.getHelpers(), tagToken, interpreter)
        .get()
    ) {
      if (maybeTemplateFile.value().isEmpty()) {
        return "";
      }
      String templateFile = maybeTemplateFile.value().get();

      try (
        AutoCloseableImpl<Node> node = ImportTag
          .parseTemplateAsNode(interpreter, templateFile)
          .get()
      ) {
        JinjavaInterpreter child = interpreter
          .getConfig()
          .getInterpreterFactory()
          .newInstance(interpreter);
        child.getContext().put(Context.IMPORT_RESOURCE_PATH_KEY, templateFile);
        JinjavaInterpreter.pushCurrent(child);
        String output;
        try {
          eagerImportingStrategy.setup(child);
          output = child.render(node.value());
        } finally {
          JinjavaInterpreter.popCurrent();
        }
        interpreter.addAllChildErrors(templateFile, child.getErrorsCopy());
        Map<String, Object> childBindings = child.getContext().getSessionBindings();

        // If the template depends on deferred values it should not be rendered,
        // and all defined variables and macros should be deferred too.
        if (
          !child.getContext().getDeferredNodes().isEmpty() ||
          (interpreter.getContext().isDeferredExecutionMode() &&
            !child.getContext().getGlobalMacros().isEmpty())
        ) {
          ImportTag.handleDeferredNodesDuringImport(
            node.value(),
            ImportTag.getContextVar(importingData.getHelpers()),
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
        eagerImportingStrategy.integrateChild(child);
        if (child.getContext().getDeferredTokens().isEmpty() || output == null) {
          return "";
        }
        return EagerReconstructionUtils.wrapInTag(
          EagerReconstructionUtils.wrapPathAroundText(
            eagerImportingStrategy.getFinalOutput(output, child),
            templateFile,
            interpreter
          ),
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
    } catch (DeferredValueException e) {
      return eagerImportingStrategy.handleDeferredTemplateFile(e);
    }
  }
}
