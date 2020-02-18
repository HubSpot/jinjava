package com.hubspot.jinjava.objects.collections;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;

public class PyMapTest {

  private Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void itUpdatesKeysWithStaticName() {
    assertThat(jinjava.render("{% set test = {\"key1\": \"value1\"} %}" +
        "{% do test.update({\"key1\": \"value2\"}) %}" +
        "{{ test[\"key1\"] }}", Collections.emptyMap())).isEqualTo("value2");
  }

  @Test
  public void itSetsKeysWithVariableName() {
    assertThat(jinjava.render("{% set keyName = \"key1\" %}" +
        "{% set test = {keyName: \"value1\"} %}" +
        "{{ test[keyName] }}", Collections.emptyMap())).isEqualTo("value1");
  }

  @Test
  public void itGetsKeysWithVariableName() {
    assertThat(jinjava.render("{% set test = {\"key1\": \"value1\"} %}" +
        "{% set keyName = \"key1\" %}" +
        "{{ test[keyName] }}", Collections.emptyMap())).isEqualTo("value1");
  }

  @Test
  public void itUpdatesKeysWithVariableName() {
    assertThat(jinjava.render("{% set test = {\"key1\": \"value1\"} %}" +
        "{% set keyName = \"key1\" %}" +
        "{% do test.update({keyName: \"value2\"}) %}" +
        "{{ test[keyName] }}", Collections.emptyMap())).isEqualTo("value2");
  }

}
