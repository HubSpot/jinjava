package com.hubspot.jinjava;

import com.hubspot.jinjava.el.ext.AllowlistMethodValidator;
import com.hubspot.jinjava.el.ext.AllowlistReturnTypeValidator;
import com.hubspot.jinjava.el.ext.MethodValidatorConfig;
import com.hubspot.jinjava.el.ext.ReturnTypeValidatorConfig;
import org.junit.Before;

public abstract class BaseJinjavaTest {

  public static final AllowlistMethodValidator METHOD_VALIDATOR =
    AllowlistMethodValidator.create(
      MethodValidatorConfig
        .builder()
        .addDefaultAllowlistGroups()
        .addAllowedDeclaredMethodsFromCanonicalClassPrefixes(
          "com.hubspot.jinjava.testobjects"
        )
        .build()
    );
  public static final AllowlistReturnTypeValidator RETURN_TYPE_VALIDATOR =
    AllowlistReturnTypeValidator.create(
      ReturnTypeValidatorConfig
        .builder()
        .addDefaultAllowlistGroups()
        .addAllowedCanonicalClassPrefixes("com.hubspot.jinjava.testobjects")
        .build()
    );
  public Jinjava jinjava;

  @Before
  public void baseSetup() {
    jinjava = new Jinjava(BaseJinjavaTest.newConfigBuilder().build());
  }

  public static JinjavaConfig.Builder newConfigBuilder() {
    return JinjavaConfig
      .newBuilder()
      .withMethodValidator(METHOD_VALIDATOR)
      .withReturnTypeValidator(RETURN_TYPE_VALIDATOR);
  }
}
