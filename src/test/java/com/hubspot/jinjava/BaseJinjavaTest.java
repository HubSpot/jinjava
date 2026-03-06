package com.hubspot.jinjava;

import org.junit.Before;

public abstract class BaseJinjavaTest {

  public Jinjava jinjava;

  @Before
  public void baseSetup() {
    jinjava =
      new Jinjava(
        BaseJinjavaTest
          .newConfigBuilder()
          .withLegacyOverrides(
            LegacyOverrides
              .newBuilder()
              .withUsePyishObjectMapper(true)
              .withKeepNullableLoopValues(true)
              .build()
          )
          .build()
      );
  }

  public static JinjavaConfig.Builder newConfigBuilder() {
    return JinjavaConfig.builder();
  }
}
