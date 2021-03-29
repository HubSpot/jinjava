package com.hubspot.jinjava;

import org.junit.Before;

public abstract class BaseJinjavaTest {
  public Jinjava jinjava;

  @Before
  public void baseSetup() {
    jinjava =
      new Jinjava(
        JinjavaConfig
          .newBuilder()
          .withLegacyOverrides(
            LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
          )
          .build()
      );
  }
}
