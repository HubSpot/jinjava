package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.objects.date.PyishDate;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class ComparisonExpTestsTest extends BaseJinjavaTest {

  @Test
  public void itComparesNumbers() {
    assertThat(jinjava.render("{{ 4 is lt 5 }}", new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render("{{ 5 is le 4 }}", new HashMap<>())).isEqualTo("false");
    assertThat(jinjava.render("{{ 4 is le 4 }}", new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render("{{ 4 is gt 5 }}", new HashMap<>())).isEqualTo("false");
    assertThat(jinjava.render("{{ 4 is gt 4 }}", new HashMap<>())).isEqualTo("false");
    assertThat(jinjava.render("{{ 4 is ge 4 }}", new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render("{{ 4 is ge 5 }}", new HashMap<>())).isEqualTo("false");
    assertThat(jinjava.render("{{ 4 is ne 5 }}", new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render("{{ 4 is ne 4 }}", new HashMap<>())).isEqualTo("false");
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
    assertThat(jinjava.render("{{ now is lt then}}", vars)).isEqualTo("false");
    assertThat(jinjava.render("{{ then is lt now}}", vars)).isEqualTo("true");
  }

  @Test
  public void itComparesAcrossType() {
    assertThat(jinjava.render("{{ 4.1 is lt 5 }}", new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render("{{ true ne 'true' }}", new HashMap<>()))
      .isEqualTo("false");
    assertThat(jinjava.render("{{ true ne '' }}", new HashMap<>())).isEqualTo("true");
  }

  @Test
  public void testAliases() {
    assertThat(jinjava.render("{{ 4 is lessthan 5 }}", new HashMap<>()))
      .isEqualTo("true");
    assertThat(jinjava.render("{{ 4 is greaterthan 5 }}", new HashMap<>()))
      .isEqualTo("false");
    assertThat(jinjava.render("{{ 4 is < 5 }}", new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render("{{ 4 is > 5 }}", new HashMap<>())).isEqualTo("false");
    assertThat(jinjava.render("{{ 4 is <= 5 }}", new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render("{{ 4 is >= 5 }}", new HashMap<>())).isEqualTo("false");
    assertThat(jinjava.render("{{ 4 is != 5 }}", new HashMap<>())).isEqualTo("true");
  }

  @Test
  public void testFormattedStringParsing() {
    assertThat(jinjava.render("{{ \"1,050.25\" is ge 4 }}", new HashMap<>()))
      .isEqualTo("true");
    assertThat(jinjava.render("{{ \"4.1\" is gt 4 }}", new HashMap<>()))
      .isEqualTo("false");
    assertThat(jinjava.render("{{ 4.0 is le 5.00 }}", new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render("{{ \"4,500.75\" is le 10000.00 }}", new HashMap<>()))
      .isEqualTo("true");
  }
}
