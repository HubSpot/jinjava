package com.hubspot.jinjava.objects.collections;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;

public class PyListTest {

  private Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void itSupportsAppendOperation() {
    assertThat(jinjava.render("{% set test = [1, 2, 3] %}" +
        "{% do test.append(4) %}" +
        "{{ test }}", Collections.emptyMap())).isEqualTo("[1, 2, 3, 4]");
  }

  @Test
  public void itSupportsPrependOperation() {
    assertThat(jinjava.render("{% set test = [1, 2, 3] %}" +
        "{% do test.prepend(4) %}" +
        "{{ test }}", Collections.emptyMap())).isEqualTo("[4, 1, 2, 3]");
  }
}
