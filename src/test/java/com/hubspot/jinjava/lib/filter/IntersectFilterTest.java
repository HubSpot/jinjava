package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

public class IntersectFilterTest {
  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
    jinjava.getGlobalContext().registerClasses(EscapeJsFilter.class);
  }

  @Test
  public void itComputesSetIntersections() {
    assertThat(
        jinjava.render("{{ [1, 1, 2, 3]|intersect([1, 2, 5, 6]) }}", new HashMap<>())
      )
      .isEqualTo("[1, 2]");
    assertThat(
        jinjava.render("{{ ['do', 'ray']|intersect(['ray', 'me']) }}", new HashMap<>())
      )
      .isEqualTo("[ray]");
  }

  @Test
  public void itReturnsEmptyOnNullParameters() {
    assertThat(jinjava.render("{{ [1, 2, 3]|intersect(null) }}", new HashMap<>()))
      .isEqualTo("[]");
  }
}
