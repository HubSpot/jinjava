package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.hubspot.algebra.Result;
import com.hubspot.jinjava.interpret.AutoCloseableSupplier.AutoCloseableImpl;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TagCycleException;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorItem;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.interpret.errorcategory.BasicTemplateErrorCategory;
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
    ImportingData importingData = EagerImportingStrategyFactory.getImportingData(
      tagToken,
      interpreter
    );
    EagerImportingStrategy eagerImportingStrategy = EagerImportingStrategyFactory.create(
      importingData
    );

    try (
      AutoCloseableImpl<Result<String, TagCycleException>> templateFileResult = ImportTag
        .getTemplateFileWithWrapper(importingData.getHelpers(), tagToken, interpreter)
        .get()
    ) {
      return templateFileResult
        .value()
        .match(
          err -> {
            String path = StringUtils.trimToEmpty(importingData.getHelpers().get(0));
            interpreter.addError(
              new TemplateError(
                ErrorType.WARNING,
                ErrorReason.EXCEPTION,
                ErrorItem.TAG,
                "Import cycle detected for path: '" + path + "'",
                null,
                tagToken.getLineNumber(),
                tagToken.getStartPosition(),
                err,
                BasicTemplateErrorCategory.IMPORT_CYCLE_DETECTED,
                ImmutableMap.of("path", path)
              )
            );
            return "";
          },
          templateFile -> {
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
              String output;
              try (
                AutoCloseableImpl<JinjavaInterpreter> a = JinjavaInterpreter
                  .closeablePushCurrent(child)
                  .get()
              ) {
                eagerImportingStrategy.setup(child);
                output = child.render(node.value());
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
          }
        );
    } catch (DeferredValueException e) {
      return eagerImportingStrategy.handleDeferredTemplateFile(e);
    }
  }
}
