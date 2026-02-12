package com.hubspot.jinjava;

import com.hubspot.jinjava.el.ext.MethodValidator;
import com.hubspot.jinjava.el.ext.MethodValidatorConfig;
import org.junit.Before;

public abstract class BaseJinjavaTest {

  public Jinjava jinjava;

  @Before
  public void baseSetup() {
    jinjava =
      new Jinjava(
        JinjavaConfig
          .newBuilder()
          .withMethodValidator(
            MethodValidator.create(
              MethodValidatorConfig.builder().addDefaultAllowlistGroups().build()
            )
          )
          .build()
      );
  }
}
