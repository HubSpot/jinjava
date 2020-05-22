package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.objects.date.PyishDate;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ComparisonExpTestsTest {
  private Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void itComparesNumbers() {
    assertThat(jinjava.render("{{ 4 is lt 5 }}", new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render("{{ 4.1 is lt 5 }}", new HashMap<>())).isEqualTo("true");
  }

  @Test
  public void itComparesStringsLexicographically() {
    assertThat(jinjava.render("{{ 'aa' is lt 'aa' }}", new HashMap<>()))
      .isEqualTo("false");
    assertThat(jinjava.render("{{ 'aa' is lt 'aaa' }}", new HashMap<>()))
      .isEqualTo("true");
    assertThat(jinjava.render("{{ 'aa' is lt 'b' }}", new HashMap<>())).isEqualTo("true");
  }

  @Test
  public void itComparesDates() {
    Map<String, Object> vars = ImmutableMap.of(
      "now",
      PyishDate.from(Instant.now()),
      "then",
      new PyishDate(1490171923745L)
    );
    assertThat(jinjava.render("{{ now is lt then}}", vars)).isEqualTo("true");
    assertThat(jinjava.render("{{ then is lt now}}", vars)).isEqualTo("false");
  }

  @Test
  public void testAliases() {
    assertThat(jinjava.render("{{ 4 is lessthan 5 }}", new HashMap<>()))
      .isEqualTo("true");
  }
}
