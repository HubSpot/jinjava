package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by manishdevgan on 25/06/19.
 */

public class DefaultFilterTest extends BaseJinjavaTest {

  @Before
  public void setup() {
    jinjava.getGlobalContext().registerClasses(DefaultFilter.class);
  }

  @Test
  public void itKeepsDefinedValuesWithoutADefaultArgument() {
    assertThat(jinjava.render("{{ 'a' | default }}", new HashMap<>())).isEqualTo("a");
    assertThat(jinjava.render("{{ 0 | default() }}", new HashMap<>())).isEqualTo("0");
  }

  @Test
  public void itDefaultsUndefinedValuesToAnEmptyString() {
    assertThat(
      jinjava.render(
        "{% set value = missing | default %}{{ type(value) }}:{{ value | length }}",
        new HashMap<>()
      )
    )
      .isEqualTo("str:0");
  }

  @Test
  public void itDefaultsFalsyValuesWithoutAnExplicitDefault() {
    assertThat(
      jinjava.render(
        "{% set value = false | default(truthy=True) %}{{ type(value) }}:{{ value | length }}",
        new HashMap<>()
      )
    )
      .isEqualTo("str:0");
  }

  @Test
  public void itAllowsOmittedArgumentsForTheAlias() {
    assertThat(jinjava.render("{{ 'a' | d }}:{{ missing | d }}", new HashMap<>()))
      .isEqualTo("a:");
  }

  @Test
  public void itSetsDefaultStringValues() {
    assertThat(
      jinjava.render(
        "{% set d=d | default(\"some random value\") %}{{ d }}",
        new HashMap<>()
      )
    )
      .isEqualTo("some random value");
  }

  @Test
  public void itSetsDefaultObjectValue() {
    assertThat(
      jinjava.render(
        "{% set d=d | default({\"key\": \"value\"}) %}Value = {{ d.key }}",
        new HashMap<>()
      )
    )
      .isEqualTo("Value = value");
  }

  @Test
  public void itChecksForType() {
    assertThat(
      jinjava.render(
        "{% set d=d | default({\"key\": \"value\"}) %}Type = {{ type(d.key) }}",
        new HashMap<>()
      )
    )
      .isEqualTo("Type = str");
    assertThat(
      jinjava.render(
        "{% set d=d | default(\"some random value\") %}{{ type(d) }}",
        new HashMap<>()
      )
    )
      .isEqualTo("str");
  }

  @Test
  public void itCorrectlyProcessesNamedParameters() {
    assertThat(
      jinjava.render(
        "{% set d=d | default(truthy=False, default_value={\"key\": \"value\"}) %}Type = {{ type(d.key) }}",
        new HashMap<>()
      )
    )
      .isEqualTo("Type = str");
  }

  @Test
  public void itIgnoresBadTruthyValue() {
    assertThat(
      jinjava.render(
        "{% set d=d | default({\"key\": \"value\"}, \"Blah\") %}Type = {{ type(d.key) }}",
        new HashMap<>()
      )
    )
      .isEqualTo("Type = str");
  }

  @Test
  public void itDefaultsNullToNull() {
    assertThat(
      jinjava.render(
        "{% set d=d | default(null) %}{% if (d == null) %}default yields real null{% else %}default yields something other than null{% endif %}",
        new HashMap<>()
      )
    )
      .isEqualTo("default yields real null");
  }

  @Test
  public void itDefaultsNullToNullWithTruthyParam() {
    assertThat(
      jinjava.render(
        "{% set d=d | default(null, true) %}{% if (d == null) %}default with truthy yields real null{% else %}default with truthy yields something other than null{% endif %}",
        new HashMap<>()
      )
    )
      .isEqualTo("default with truthy yields real null");
  }
}
