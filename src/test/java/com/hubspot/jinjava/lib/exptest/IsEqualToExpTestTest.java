package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;

public class IsEqualToExpTestTest extends BaseJinjavaTest {

  private static final String EQUAL_TEMPLATE = "{{ %s is equalto %s }}";

  @Test
  public void itEquatesNumbers() {
    assertThat(jinjava.render(String.format(EQUAL_TEMPLATE, "4", "4"), new HashMap<>()))
      .isEqualTo("true");
    assertThat(jinjava.render(String.format(EQUAL_TEMPLATE, "4", "5"), new HashMap<>()))
      .isEqualTo("false");
  }

  @Test
  public void itEquatesStrings() {
    assertThat(
      jinjava.render(
        String.format(EQUAL_TEMPLATE, "\"jinjava\"", "\"jinjava\""),
        new HashMap<>()
      )
    )
      .isEqualTo("true");
    assertThat(
      jinjava.render(
        String.format(EQUAL_TEMPLATE, "\"jinjava\"", "\"not jinjava\""),
        new HashMap<>()
      )
    )
      .isEqualTo("false");
  }

  @Test
  public void itEquatesCollectionsToStrings() {
    assertThat(
      jinjava.render(
        String.format(EQUAL_TEMPLATE, "\"[1, 2, 3]\"", "[1, 2, 3]"),
        new HashMap<>()
      )
    )
      .isEqualTo("true");

    assertThat(
      jinjava.render(
        String.format(EQUAL_TEMPLATE, "\"[1, 2, 3]\"", "[1, 2, 4]"),
        new HashMap<>()
      )
    )
      .isEqualTo("false");
  }

  @Test
  public void itEquatesLargeCollectionsAndStrings() {
    assertThat(compareStringAndCollection(100_000)).isEqualTo("true");
  }

  @Test
  public void itDoesNotEquateHugeCollectionsAndStrings() {
    assertThat(compareStringAndCollection(500_000)).isEqualTo("false");
  }

  private String compareStringAndCollection(int size) {
    List<Integer> bigList = new ArrayList<>();

    for (int i = 0; i < size; i++) {
      bigList.add(1);
    }

    String bigString = bigList.toString();

    return jinjava.render(
      String.format(EQUAL_TEMPLATE, "\"" + bigString + "\"", bigString),
      new HashMap<>()
    );
  }

  @Test
  public void itEquatesBooleans() {
    assertThat(
      jinjava.render(String.format(EQUAL_TEMPLATE, "true", "true"), new HashMap<>())
    )
      .isEqualTo("true");
    assertThat(
      jinjava.render(String.format(EQUAL_TEMPLATE, "true", "false"), new HashMap<>())
    )
      .isEqualTo("false");
  }

  @Test
  public void itEquatesDifferentTypes() {
    assertThat(
      jinjava.render(String.format(EQUAL_TEMPLATE, "4", "\"4\""), new HashMap<>())
    )
      .isEqualTo("true");
    assertThat(
      jinjava.render(String.format(EQUAL_TEMPLATE, "4", "\"5\""), new HashMap<>())
    )
      .isEqualTo("false");
    assertThat(
      jinjava.render(String.format(EQUAL_TEMPLATE, "'c'", "\"c\""), new HashMap<>())
    )
      .isEqualTo("true");
    assertThat(
      jinjava.render(String.format(EQUAL_TEMPLATE, "'c'", "\"b\""), new HashMap<>())
    )
      .isEqualTo("false");
  }

  @Test
  public void testAliases() {
    assertThat(jinjava.render("{{ 4 is eq 4 }}", new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render("{{ 4 is == 4 }}", new HashMap<>())).isEqualTo("true");
  }
}
