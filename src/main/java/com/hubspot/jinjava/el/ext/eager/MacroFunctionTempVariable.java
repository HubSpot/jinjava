package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.objects.serialization.PyishBlockSetSerializable;
import java.util.Objects;

public class MacroFunctionTempVariable implements PyishBlockSetSerializable {

  private static final String CONTEXT_KEY_PREFIX = "__macro_%s_%d_temp_variable_%d__";
  private final String deferredResult;

  public MacroFunctionTempVariable(String deferredResult) {
    this.deferredResult = deferredResult;
  }

  public static String getVarName(String macroFunctionName, int hashCode, int callCount) {
    return String.format(
      CONTEXT_KEY_PREFIX,
      macroFunctionName,
      Math.abs(hashCode),
      callCount
    );
  }

  @Override
  public String getBlockSetBody() {
    return deferredResult;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MacroFunctionTempVariable that = (MacroFunctionTempVariable) o;
    return deferredResult.equals(that.deferredResult);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deferredResult);
  }
}
