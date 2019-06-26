package com.hubspot.jinjava.lib.filter;

import java.util.HashMap;

import com.hubspot.jinjava.Jinjava;
import org.junit.Before;
import org.junit.Test;


import static org.assertj.core.api.Assertions.assertThat;

public class DifferenceFilterTest {

  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
    jinjava.getGlobalContext().registerClasses(EscapeJsFilter.class);
  }

  @Test
  public void itComputesSetDifferences() {
    assertThat(jinjava.render("{{ [1, 2, 3, 3, 4]|difference([1, 2, 5, 6]) }}", new HashMap<>())).isEqualTo("[3, 4]");
    assertThat(jinjava.render("{{ ['do', 'ray']|difference(['ray', 'me']) }}", new HashMap<>())).isEqualTo("[do]");
  }

  @Test
  public void itReturnsEmptyOnNullParameters() {
    assertThat(jinjava.render("{{ [1, 2, 3, 3]|difference(null) }}", new HashMap<>())).isEqualTo("[1, 2, 3]");
  }
}
