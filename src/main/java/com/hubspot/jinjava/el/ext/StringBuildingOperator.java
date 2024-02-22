package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;

public interface StringBuildingOperator {
  default LengthLimitingStringBuilder getStringBuilder() {
    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();

    long maxSize = (interpreter == null || interpreter.getConfig() == null)
      ? 0
      : interpreter.getConfig().getMaxOutputSize();

    return new LengthLimitingStringBuilder(maxSize);
  }
}
