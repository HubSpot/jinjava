package com.hubspot.jinjava.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ObjectTruthValueTest {

  @Test
  public void itEvaluatesObjectsWithObjectTruthValue() {
    assertThat(ObjectTruthValue.evaluate(new TestObject().setObjectTruthValue(true)))
      .isTrue();
    assertThat(ObjectTruthValue.evaluate(new TestObject().setObjectTruthValue(false)))
      .isFalse();
  }

  @Test
  public void itEvaluatesIntegers() {
    int a = 1;
    assertThat(ObjectTruthValue.evaluate(a)).isTrue();
    int b = 0;
    assertThat(ObjectTruthValue.evaluate(b)).isFalse();
  }

  @Test
  public void itEvaluatesDoubles() {
    double a = 0.5;
    assertThat(ObjectTruthValue.evaluate(a)).isTrue();
    double b = 0.0;
    assertThat(ObjectTruthValue.evaluate(b)).isFalse();
  }

  @Test
  public void itEvaluatesLongs() {
    long a = 1;
    assertThat(ObjectTruthValue.evaluate(a)).isTrue();
    long b = 0;
    assertThat(ObjectTruthValue.evaluate(b)).isFalse();
  }

  @Test
  public void itEvaluatesShorts() {
    short a = 1;
    assertThat(ObjectTruthValue.evaluate(a)).isTrue();
    short b = 0;
    assertThat(ObjectTruthValue.evaluate(b)).isFalse();
  }

  @Test
  public void itEvaluatesFloats() {
    float a = 0.5f;
    assertThat(ObjectTruthValue.evaluate(a)).isTrue();
    float b = 0.0f;
    assertThat(ObjectTruthValue.evaluate(b)).isFalse();
  }

  private class TestObject implements HasObjectTruthValue {
    private boolean objectTruthValue = false;

    public TestObject setObjectTruthValue(boolean objectTruthValue) {
      this.objectTruthValue = objectTruthValue;
      return this;
    }

    @Override
    public boolean getObjectTruthValue() {
      return objectTruthValue;
    }
  }
}
