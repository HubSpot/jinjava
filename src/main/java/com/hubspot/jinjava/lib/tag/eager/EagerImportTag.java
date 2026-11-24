package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.hubspot.algebra.Result;
import com.hubspot.jinjava.interpret.AutoCloseableSupplier.AutoCloseableImpl;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.interpret.TagCycleException;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorItem;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.interpret.errorcategory.BasicTemplateErrorCategory;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.fn.eager.EagerMacroFunction;
import com.hubspot.jinjava.lib.tag.DoTag;
import com.hubspot.jinjava.lib.tag.ImportTag;
import com.hubspot.jinjava.lib.tag.eager.importing.EagerImportingStrategy;
import com.hubspot.jinjava.lib.tag.eager.importing.EagerImportingStrategyFactory;
import com.hubspot.jinjava.lib.tag.eager.importing.ImportingData;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.DeferredValueUtils;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import com.hubspot.jinjava.util.PrefixToPreserveState;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
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
    ImportingData importingData = EagerImportingStrategyFactory.getImportingData(
      tagToken,
      interpreter
    );
    EagerImportingStrategy eagerImportingStrategy = EagerImportingStrategyFactory.create(
      importingData
    );

    Set<String> namesReadByDeferredImport = new HashSet<>();

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
              eagerImportingStrategy.setup(child);
              output = child.render(node.value());

              interpreter.addAllChildErrors(templateFile, child.getErrorsCopy());
              Map<String, Object> childBindings = child.getContext().getSessionBindings();

              // If the template depends on deferred values it should not be rendered,
              // and all defined variables and macros should be deferred too.
              if (
                !child.getContext().getDeferredNodes().isEmpty() ||
                (interpreter.getContext().isDeferredExecutionMode() &&
                  !child.getContext().getGlobalMacros().isEmpty())
              ) {
                namesReadByDeferredImport.addAll(
                  DeferredValueUtils.getPropertiesUsedInDeferredNodes(
                    interpreter.getContext(),
                    node
                      .value()
                      .getChildren()
                      .stream()
                      .map(Node::reconstructImage)
                      .collect(Collectors.joining()),
                    false
                  )
                );
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
      String deferredImportImage = eagerImportingStrategy.handleDeferredTemplateFile(e);

      boolean hasStateToPreserve = !namesReadByDeferredImport.isEmpty();
      if (!hasStateToPreserve) {
        return deferredImportImage;
      }

      PrefixToPreserveState prefixToPreserveState;
      try (InterpreterScopeClosable c = interpreter.enterNonStackingScope()) {
        // A parent macro the imported template calls with {% call %} may use caller().
        // Like EagerCallTag, make caller() exist and be deferred while reconstructing,
        // so it stays in the reconstructed macro instead of failing to resolve.
        MacroFunction caller = new EagerMacroFunction(
          Collections.emptyList(),
          "caller",
          new LinkedHashMap<>(),
          true,
          interpreter.getContext(),
          interpreter.getLineNumber(),
          interpreter.getPosition()
        );
        caller.setDeferred(true);
        interpreter.getContext().addGlobalMacro(caller);
        prefixToPreserveState =
          EagerReconstructionUtils.hydrateReconstructionFromContextBeforeDeferring(
            new PrefixToPreserveState(),
            namesReadByDeferredImport,
            interpreter
          );
      }
      prefixToPreserveState.withAllInFront(
        EagerReconstructionUtils.handleDeferredTokenAndReconstructReferences(
          interpreter,
          DeferredToken
            .builderFromToken(tagToken)
            .addUsedDeferredWords(namesReadByDeferredImport)
            .build()
        )
      );
      return prefixToPreserveState + deferredImportImage;
    }
  }
}
