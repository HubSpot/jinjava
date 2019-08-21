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
  public void itSupportsExtendOperation() {
    assertThat(jinjava.render("{% set test = [1, 2, 3] %}" +
        "{% do test.extend([4, 5, 6]) %}" +
        "{{ test }}", Collections.emptyMap())).isEqualTo("[1, 2, 3, 4, 5, 6]");
  }

  @Test
  public void itSupportsInsertOperation() {
    assertThat(jinjava.render("{% set test = [1, 2, 3] %}" +
        "{% do test.insert(1, 4) %}" +
        "{{ test }}", Collections.emptyMap())).isEqualTo("[1, 4, 2, 3]");
  }

  @Test
  public void itSupportsPopOperation() {
    assertThat(jinjava.render("{% set test = [1, 2, 3] %}" +
        "{{ test.pop() }}" +
        "{{ test }}", Collections.emptyMap())).isEqualTo("3[1, 2]");
  }

  @Test
  public void itSupportsPopAtIndexOperation() {
    assertThat(jinjava.render("{% set test = [1, 2, 3] %}" +
        "{{ test.pop(1) }}" +
        "{{ test }}", Collections.emptyMap())).isEqualTo("2[1, 3]");
  }

  @Test
  public void itSupportsClearOperation() {
    assertThat(jinjava.render("{% set test = [1, 2, 3] %}" +
        "{% do test.clear() %}" +
        "{{ test }}", Collections.emptyMap())).isEqualTo("[]");
  }

  @Test
  public void itSupportsCountOperation() {
    assertThat(jinjava.render("{% set test = [1, 1, 2, 2, 2, 3] %}" +
        "{{ test.count(2) }}" +
        "{{ test }}", Collections.emptyMap())).isEqualTo("3[1, 1, 2, 2, 2, 3]");
  }

  @Test
  public void itSupportsReverseOperation() {
    assertThat(jinjava.render("{% set test = [1, 2, 3] %}" +
        "{% do test.reverse() %}" +
        "{{ test }}", Collections.emptyMap())).isEqualTo("[3, 2, 1]");
  }

  @Test
  public void itSupportsCopyOperation() {
    assertThat(jinjava.render("{% set test = [1, 2, 3] %}" +
        "{% set test2 = test.copy() %}" +
        "{% do test.append(4) %}" +
        "{{ test }}{{test2}}", Collections.emptyMap())).isEqualTo("[1, 2, 3, 4][1, 2, 3]");
  }

  @Test
  public void itSupportsIndexOperation() {
    assertThat(jinjava.render("{% set test = [10, 20, 30, 10, 20, 30] %}" +
        "{{ test.index(20) }}", Collections.emptyMap())).isEqualTo("1");
  }

  @Test
  public void itSupportsIndexWithinBoundsOperation() {
    assertThat(jinjava.render("{% set test = [10, 20, 30, 10, 20, 30] %}" +
        "{{ test.index(20, 2, 6) }}", Collections.emptyMap())).isEqualTo("4");
  }

  @Test
  public void itReturnsNegativeOneForMissingObjectForIndex() {
    assertThat(jinjava.render("{% set test = [10, 20, 30, 10, 20, 30] %}" +
        "{{ test.index(999) }}", Collections.emptyMap())).isEqualTo("-1");
  }

  @Test
  public void itReturnsNegativeOneForMissingObjectForIndexWithinBounds() {
    assertThat(jinjava.render("{% set test = [10, 20, 30, 10, 20, 30] %}" +
        "{{ test.index(999, 1, 5) }}", Collections.emptyMap())).isEqualTo("-1");
  }
}
