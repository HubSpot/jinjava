package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class SafeFilterTest {

  private static final String HTML = "<a>Link</a>";
  private static final List<Integer> TEST_NUMBERS = ImmutableList.of(43, 1, 24);
  private static final List<String> TEST_STRINGS = ImmutableList.of("-100","1000", "short", "WeIrD", "The quick Brown fox Jumped Over The Lazy Dog.", "  some whitespace here  ");
  private static final List<String> STRING_FILTERS = ImmutableList.of("ipaddr", "length", "lower", "upper", "md5", "reverse", "trim", "capitalize", "cut('a')", "center");
  private static final List<String> NUMBER_FILTERS = ImmutableList.of("divide('5')", "ipaddr", "length", "log", "lower", "upper", "md5", "multiply('9')", "reverse", "root");

  private JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
    interpreter.getContext().setAutoEscape(true);
  }

  @After
  public void tearDown() throws Exception {
    assertThat(interpreter.getErrorsCopy()).isEmpty();
  }

  @Test
  public void itDoesNotEscapeStringMarkedAsSafe() throws Exception {
    interpreter.getContext().put("html", HTML);
    assertThat(interpreter.renderFlat("{{ html|safe }}")).isEqualTo(HTML);
  }

  @Test
  public void itPassesVarThroughIfNotInstanceOfString() throws Exception {
    interpreter.getContext().put("number", -3);
    assertThat(interpreter.renderFlat("{{ number|safe|abs }}")).isEqualTo("3");
  }

  @Test
  public void itWorksWhenChainingFilters() throws Exception {
    interpreter.getContext().put("safe_html", HTML);
    assertThat(interpreter.renderFlat("{{ safe_html|safe|upper }}")).isEqualTo(HTML.toUpperCase());
    assertThat(interpreter.renderFlat("{{ safe_html|upper|safe }}")).isEqualTo(HTML.toUpperCase());
    assertThat(interpreter.renderFlat("{{ safe_html|safe|length }}")).isEqualTo(String.valueOf(HTML.length()));
    assertThat(interpreter.renderFlat("{{ safe_html|safe|length|safe }}")).isEqualTo(String.valueOf(HTML.length()));
    assertThat(interpreter.renderFlat("{{ safe_html|length }}")).isEqualTo(String.valueOf(HTML.length()));
  }

  @Test
  public void itWorksForAllRelevantFilters() throws Exception {
    for (String testFilter : STRING_FILTERS) {
      for (String testString : TEST_STRINGS ) {
        interpreter.getContext().put("string_under_test", testString);
        assertThat(interpreter.renderFlat("{{ string_under_test|safe|" + testFilter + "|safe }}")).as("Testing behaviour of filter with and without safe filter: " + testFilter + " on string " + testString)
            .isEqualTo(interpreter.renderFlat("{{ string_under_test|" + testFilter + " }}"));
      }
    }
    for (String testFilter : NUMBER_FILTERS) {
      for (Integer testInt : TEST_NUMBERS ) {
        interpreter.getContext().put("string_under_test", testInt);
        assertThat(interpreter.renderFlat("{{ string_under_test|safe|" + testFilter + " }}")).as("Testing behaviour of filter with and without safe filter: " + testFilter + " on string " + testInt)
            .isEqualTo(interpreter.renderFlat("{{ string_under_test|" + testFilter + " }}"));
      }
    }
    assertThat(interpreter.renderFlat("{{ 1|safe|random }}")).isEqualTo("0");
  }
}
