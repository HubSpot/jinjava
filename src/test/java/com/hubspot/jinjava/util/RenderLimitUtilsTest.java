package com.hubspot.jinjava.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.JinjavaConfig;
import org.junit.Test;

public class RenderLimitUtilsTest {

  @Test
  public void itPicksLowerLimitWhenConfigIsSet() {
    assertThat(
      RenderLimitUtils.clampProvidedRenderLimitToConfig(100, configWithOutputSize(10))
    )
      .isEqualTo(10);
  }

  @Test
  public void itKeepsConfigLimitWhenConfigSetAndUnlimitedProvided() {
    assertThat(
      RenderLimitUtils.clampProvidedRenderLimitToConfig(0, configWithOutputSize(10))
    )
      .isEqualTo(10);
    assertThat(
      RenderLimitUtils.clampProvidedRenderLimitToConfig(-10, configWithOutputSize(10))
    )
      .isEqualTo(10);
  }

  @Test
  public void itUsesProvidedLimitWhenConfigIsUnlimited() {
    assertThat(
      RenderLimitUtils.clampProvidedRenderLimitToConfig(10, configWithOutputSize(0))
    )
      .isEqualTo(10);

    assertThat(
      RenderLimitUtils.clampProvidedRenderLimitToConfig(10, configWithOutputSize(-10))
    )
      .isEqualTo(10);
  }

  private JinjavaConfig configWithOutputSize(long size) {
    return BaseJinjavaTest.newConfigBuilder().withMaxOutputSize(size).build();
  }
}
