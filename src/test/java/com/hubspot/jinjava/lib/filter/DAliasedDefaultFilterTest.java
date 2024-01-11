package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.HashMap;
import org.junit.Test;

public class DAliasedDefaultFilterTest extends BaseJinjavaTest {

  @Test
  public void itSetsDefaultStringValues() {
    assertThat(
      jinjava.render("{% set d=d |d(\"some random value\") %}{{ d }}", new HashMap<>())
    )
      .isEqualTo("some random value");
  }
}
