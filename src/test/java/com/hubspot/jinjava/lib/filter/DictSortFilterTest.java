package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

public class DictSortFilterTest extends BaseJinjavaTest {
  private static Map<String, Object> context;

  @BeforeClass
  public static void initTemplate() {
    context = new HashMap<>();

    Map<String, String> countryCapitals = new HashMap<>();
    countryCapitals.put("Bhutan", "Thimpu");
    countryCapitals.put("Australia", "Canberra");
    countryCapitals.put("none", "none");
    countryCapitals.put("India", "New Delhi");
    countryCapitals.put("France", "Paris");

    context.put("countryCapitals", countryCapitals);
  }

  @Test
  public void sortByKeyCaseInsensitive() {
    String template =
      "{% for key, value in countryCapitals|dictsort %}" +
      " {{key}},{{value}}" +
      " {% endfor %}";

    String expected =
      " Australia,Canberra  Bhutan,Thimpu  France,Paris  India,New Delhi  none,none ";

    String actual = jinjava.render(template, context);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void sortByValueAndCaseInsensitive() {
    String template =
      "{% for key, value in countryCapitals|dictsort(false,'value') %}" +
      " {{key}},{{value}}" +
      " {% endfor %}";

    String expected =
      " Australia,Canberra  India,New Delhi  none,none  France,Paris  Bhutan,Thimpu ";

    String actual = jinjava.render(template, context);

    assertThat(actual).isEqualTo(expected);
  }
}
