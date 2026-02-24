package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.objects.serialization.PyishBlockSetSerializable;
import java.util.Objects;

public class RenderFlatTempVariable implements PyishBlockSetSerializable {

  private static final String CONTEXT_KEY_PREFIX = "__render_%d_temp_variable__";
  private final String deferredResult;

  public RenderFlatTempVariable(String deferredResult) {
    this.deferredResult = deferredResult;
  }

  public static String getVarName(String result) {
    return String.format(CONTEXT_KEY_PREFIX, Math.abs(result.hashCode() >> 1));
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
    RenderFlatTempVariable that = (RenderFlatTempVariable) o;
    return deferredResult.equals(that.deferredResult);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deferredResult);
  }
}
