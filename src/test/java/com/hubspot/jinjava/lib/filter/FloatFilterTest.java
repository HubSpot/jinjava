/**********************************************************************
 Copyright (c) 2018 HubSpot Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **********************************************************************/
package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class FloatFilterTest {

  private static final Locale FRENCH_LOCALE = new Locale("fr", "FR");
  private static final JinjavaConfig FRENCH_LOCALE_CONFIG = new JinjavaConfig(StandardCharsets.UTF_8, FRENCH_LOCALE, ZoneOffset.UTC, 10);

  FloatFilter filter;
  JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
    filter = new FloatFilter();
  }

  @Test
  public void itReturnsSameWhenVarIsNumber() {
    Float var = 123.4f;
    assertThat(filter.filter(var, interpreter)).isSameAs(var);
  }

  @Test
  public void itReturnsDefaultWhenVarIsNull() {
    assertThat(filter.filter(null, interpreter)).isEqualTo(0.0f);
    assertThat(filter.filter(null, interpreter, "123.45")).isEqualTo(123.45f);
  }

  @Test
  public void itIgnoresGivenDefaultIfNaN() {
    assertThat(filter.filter(null, interpreter, "foo")).isEqualTo(0.0f);
  }

  @Test
  public void itReturnsVarAsFloat() {
    assertThat(filter.filter("123.45", interpreter)).isEqualTo(123.45f);
    assertThat(filter.filter("1.100000", interpreter)).isEqualTo(1.100000f);
  }

  @Test
  public void itReturnsVarWithTrailingPercentAsDefault() {
    assertThat(filter.filter("123%", interpreter)).isEqualTo(0.0f);
  }

  @Test
  public void itReturnsVarWithLeadingLettersAsDefault() {
    assertThat(filter.filter("abc123", interpreter)).isEqualTo(0.0f);
  }

  @Test
  public void itReturnsVarWithTrailingLettersAsDefault() {
    assertThat(filter.filter("123abc", interpreter)).isEqualTo(0.0f);
  }

  @Test
  public void itReturnsVarWithLeadingCurrencySymbolAsDefault() {
    assertThat(filter.filter("$123", interpreter)).isEqualTo(0.0f);
  }

  @Test
  public void itReturnsVarWithTrailingCurrencySymbolAsDefault() {
    assertThat(filter.filter("123$", interpreter)).isEqualTo(0.0f);
  }

  @Test
  public void itInterpretsUsCommasAndPeriodsWithUsLocale() {
    assertThat(filter.filter("123,123.45", interpreter)).isEqualTo(123123.45f);
  }

  @Test
  public void itDoesntInterpretFrenchCommasAndPeriodsWithUsLocale() {
    assertThat(filter.filter("123.123,45", interpreter)).isEqualTo(0.0f);
  }

  @Test
  public void itReturnsDefaultWhenUnableToParseVar() {
    assertThat(filter.filter("foo", interpreter)).isEqualTo(0.0f);
  }

  @Test
  public void itDoesntInterpretUsCommasAndPeriodsWithFrenchLocale() {
    interpreter = new Jinjava(FRENCH_LOCALE_CONFIG).newInterpreter();
    assertThat(filter.filter("123,123.12", interpreter)).isEqualTo(0.0f);
  }

  @Test
  public void itInterpretsFrenchCommasAndPeriodsWithFrenchLocale() {
    interpreter = new Jinjava(FRENCH_LOCALE_CONFIG).newInterpreter();
    assertThat(filter.filter("123\u00A0123,45", interpreter)).isEqualTo(123123.45f);
  }
}
