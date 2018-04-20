package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.Locale;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class IntFilterTest {

  private static final Locale FRENCH_LOCALE = new Locale("fr", "FR");
  private static final JinjavaConfig FRENCH_LOCALE_CONFIG = new JinjavaConfig(StandardCharsets.UTF_8, FRENCH_LOCALE, ZoneOffset.UTC, 10);

  IntFilter filter;
  JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
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
    assertThat(filter.filter("123\u00A0123,12", interpreter)).isEqualTo(123123);
  }
}
