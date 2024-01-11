package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

public class UnionFilterTest extends BaseJinjavaTest {

  @Before
  public void setup() {
    jinjava.getGlobalContext().registerClasses(EscapeJsFilter.class);
  }

  @Test
  public void itComputesSetUnions() {
    assertThat(jinjava.render("{{ [1, 2, 3, 3]|union([1, 2, 5, 6]) }}", new HashMap<>()))
      .isEqualTo("[1, 2, 3, 5, 6]");
    assertThat(
      jinjava.render("{{ ['do', 'ray']|union(['ray', 'me']) }}", new HashMap<>())
    )
      .isEqualTo("['do', 'ray', 'me']");
  }

  @Test
  public void itReturnsSetOnNullParameters() {
    assertThat(jinjava.render("{{ [1, 2, 3, 3]|union(null) }}", new HashMap<>()))
      .isEqualTo("[1, 2, 3]");
  }
}
