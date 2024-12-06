package com.hubspot.jinjava.lib.tag.eager.importing;

import com.google.common.base.Strings;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.ImportTag;
import com.hubspot.jinjava.loader.RelativePathResolver;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerReconstructionUtils;
import java.util.List;

public class EagerImportingStrategyFactory {

  public static ImportingData getImportingData(
    TagToken tagToken,
    JinjavaInterpreter interpreter
  ) {
    List<String> helpers = ImportTag.getHelpers(tagToken);
    String initialPathSetter = getSetTagForCurrentPath(interpreter);
    return new ImportingData(interpreter, tagToken, helpers, initialPathSetter);
  }

  public static EagerImportingStrategy create(ImportingData importingData) {
    String currentImportAlias = ImportTag.getContextVar(importingData.getHelpers());
    if (Strings.isNullOrEmpty(currentImportAlias)) {
      return new FlatEagerImportingStrategy(importingData);
    }
    return new AliasedEagerImportingStrategy(importingData, currentImportAlias);
  }

  public static String getSetTagForCurrentPath(JinjavaInterpreter interpreter) {
    return EagerReconstructionUtils.buildBlockOrInlineSetTag(
      RelativePathResolver.CURRENT_PATH_CONTEXT_KEY,
      RelativePathResolver.getCurrentPathFromStackOrKey(interpreter),
      interpreter
    );
  }
}
