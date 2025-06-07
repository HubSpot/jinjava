package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.hubspot.jinjava.interpret.AutoCloseableWrapper;
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
      AutoCloseableWrapper<String> templateFile = ImportTag.getTemplateFileWithWrapper(
        importingData.getHelpers(),
        tagToken,
        interpreter
      )
    ) {
      if (templateFile.get() == null) {
        return "";
      }

      try (
        AutoCloseableWrapper<Node> node = ImportTag.parseTemplateAsNode(
          interpreter,
          templateFile.get()
        )
      ) {
        JinjavaInterpreter child = interpreter
          .getConfig()
          .getInterpreterFactory()
          .newInstance(interpreter);
        child.getContext().put(Context.IMPORT_RESOURCE_PATH_KEY, templateFile.get());
        JinjavaInterpreter.pushCurrent(child);
        String output;
        try {
          eagerImportingStrategy.setup(child);
          output = child.render(node.get());
        } finally {
          JinjavaInterpreter.popCurrent();
        }
        interpreter.addAllChildErrors(templateFile.get(), child.getErrorsCopy());
        Map<String, Object> childBindings = child.getContext().getSessionBindings();

        // If the template depends on deferred values it should not be rendered,
        // and all defined variables and macros should be deferred too.
        if (
          !child.getContext().getDeferredNodes().isEmpty() ||
          (interpreter.getContext().isDeferredExecutionMode() &&
            !child.getContext().getGlobalMacros().isEmpty())
        ) {
          ImportTag.handleDeferredNodesDuringImport(
            node.get(),
            ImportTag.getContextVar(importingData.getHelpers()),
            childBindings,
            child,
            interpreter
          );
          throw new DeferredValueException(
            templateFile.get(),
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
            templateFile.get(),
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
