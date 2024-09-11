package com.hubspot.jinjava.interpret;

import com.google.common.annotations.Beta;
import java.util.Objects;

@Beta
public class MetaContextVariables {

  public static final String TEMPORARY_META_CONTEXT_PREFIX = "__temp_meta_";
  private static final String TEMPORARY_IMPORT_ALIAS_PREFIX =
    TEMPORARY_META_CONTEXT_PREFIX + "import_alias_";

  private static final String TEMPORARY_IMPORT_ALIAS_FORMAT =
    TEMPORARY_IMPORT_ALIAS_PREFIX + "%d__";
  private static final String TEMP_CURRENT_PATH_PREFIX =
    TEMPORARY_META_CONTEXT_PREFIX + "current_path_";
  private static final String TEMP_CURRENT_PATH_FORMAT =
    TEMP_CURRENT_PATH_PREFIX + "%d__";

  public static boolean isMetaContextVariable(String varName, Context context) {
    if (isTemporaryMetaContextVariable(varName)) {
      return true;
    }
    return (
      context.getMetaContextVariables().contains(varName) &&
      !context.getNonMetaContextVariables().contains(varName)
    );
  }

  private static boolean isTemporaryMetaContextVariable(String varName) {
    return varName.startsWith(TEMPORARY_META_CONTEXT_PREFIX);
  }

  public static boolean isTemporaryImportAlias(String varName) {
    // This is just faster than checking a regex
    return varName.startsWith(TEMPORARY_IMPORT_ALIAS_PREFIX);
  }

  public static String getTemporaryImportAlias(String fullAlias) {
    return String.format(
      TEMPORARY_IMPORT_ALIAS_FORMAT,
      Math.abs(Objects.hashCode(fullAlias))
    );
  }

  public static String getTemporaryCurrentPathVarName(String newPath) {
    return String.format(
      TEMP_CURRENT_PATH_FORMAT,
      Math.abs(Objects.hash(newPath, TEMPORARY_META_CONTEXT_PREFIX) >> 1)
    );
  }
}
