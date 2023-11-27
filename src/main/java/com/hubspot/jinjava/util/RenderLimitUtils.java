package com.hubspot.jinjava.util;

import com.hubspot.jinjava.JinjavaConfig;

public class RenderLimitUtils {

  public static long clampProvidedRenderLimitToConfig(
    long providedLimit,
    JinjavaConfig jinjavaConfig
  ) {
    long configMaxOutput = jinjavaConfig.getMaxOutputSize();

    if (configMaxOutput <= 0) {
      return providedLimit;
    }

    if (providedLimit <= 0) {
      return configMaxOutput;
    }

    return Math.min(providedLimit, configMaxOutput);
  }
}
