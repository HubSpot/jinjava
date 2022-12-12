package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormatSymbols;
import java.time.ZoneOffset;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;

public class IntFilterTest extends BaseInterpretingTest {
  private static final Locale FRENCH_LOCALE = new Locale("fr", "FR");
  private static final JinjavaConfig FRENCH_LOCALE_CONFIG = new JinjavaConfig(
    StandardCharsets.UTF_8,
    FRENCH_LOCALE,
    ZoneOffset.UTC,
    10
  );

  IntFilter filter;

  @Before
  public void setup() {
    filter = new IntFilter();
  }

  @Test
  public void itReturnsSameWhenVarIsNumber() {
    Integer var = 123;
    assertThat(filter.filter(var, interpreter)).isSameAs(var);
  }

  @Test
  public void itReturnsDefaultWhenVarIsNull() {
    assertThat(filter.filter(null, interpreter)).isEqualTo(0);
    assertThat(filter.filter(null, interpreter, "123")).isEqualTo(123);
  }

  @Test
  public void itIgnoresGivenDefaultIfNaN() {
    assertThat(filter.filter(null, interpreter, "foo")).isEqualTo(0);
  }

  @Test
  public void itReturnsVarAsInt() {
    assertThat(filter.filter("123", interpreter))
      .isInstanceOf(Integer.class)
      .isEqualTo(123);
  }

  @Test
  public void itReturnsVarWithFloatingPointAsInt() {
    assertThat(filter.filter("123.1", interpreter)).isEqualTo(123);
  }

  @Test
  public void itReturnsVarWithLeadingPercentAsDefault() {
    assertThat(filter.filter("%60", interpreter)).isEqualTo(0);
  }

  @Test
  public void itReturnsVarWithTrailingPercentAsDefault() {
    assertThat(filter.filter("60%", interpreter)).isEqualTo(0);
  }

  @Test
  public void itReturnsVarWithLeadingLettersAsDefault() {
    assertThat(filter.filter("abc60", interpreter)).isEqualTo(0);
  }

  @Test
  public void itReturnsVarWithTrailingLettersAsDefault() {
    assertThat(filter.filter("60abc", interpreter)).isEqualTo(0);
  }

  @Test
  public void itReturnsVarWithLeadingCurrencySymbolAsDefault() {
    assertThat(filter.filter("$60", interpreter)).isEqualTo(0);
  }

  @Test
  public void itReturnsVarWithTrailingCurrencySymbolAsDefault() {
    assertThat(filter.filter("60$", interpreter)).isEqualTo(0);
  }

  @Test
  public void itInterpretsUsCommasAndPeriodsWithUsLocale() {
    assertThat(filter.filter("123,123.12", interpreter)).isEqualTo(123123);
  }

  @Test
  public void itDoesntInterpretFrenchCommasAndPeriodsWithUsLocale() {
    assertThat(filter.filter("123.123,12", interpreter)).isEqualTo(0);
  }

  @Test
  public void itReturnsDefaultWhenUnableToParseVar() {
    assertThat(filter.filter("foo", interpreter)).isEqualTo(0);
  }

  @Test
  public void itDoesntInterpretUsCommasAndPeriodsWithFrenchLocale() {
    interpreter = new Jinjava(FRENCH_LOCALE_CONFIG).newInterpreter();
    assertThat(filter.filter("123,123.12", interpreter)).isEqualTo(0);
  }

  @Test
  public void itInterpretsFrenchCommasAndPeriodsWithFrenchLocale() {
    interpreter = new Jinjava(FRENCH_LOCALE_CONFIG).newInterpreter();
    assertThat(
        filter.filter(
          String.format(
            "123%c123,12",
            DecimalFormatSymbols.getInstance(Locale.FRENCH).getGroupingSeparator()
          ),
          interpreter
        )
      )
      .isEqualTo(123123);
  }

  @Test
  public void itUsesLongsForLargeValues() {
    assertThat(filter.filter("1000000000001", interpreter)).isEqualTo(1000000000001L);
  }

  @Test
  public void itUsesLongsForLargeValueDefaults() {
    assertThat(filter.filter("not a number", interpreter, "1000000000001"))
      .isEqualTo(1000000000001L);
  }

  @Test
  public void itUsesLongsForVerySmallValues() {
    assertThat(filter.filter("-42595200000", interpreter)).isEqualTo(-42595200000L);
  }

  @Test
  public void itConvertsProperlyInExpressionTest() {
    assertThat(interpreter.render("{{ '3'|int in [null, 4, 5, 6, null, 3] }}"))
      .isEqualTo("true");
  }

  @Test
  public void itConvertsProperlyInExpressionTestWithWrongType() {
    assertThat(interpreter.render("{{ 'test' in [null, 4, 5, 6, null, 3] }}"))
      .isEqualTo("false");
  }
}
