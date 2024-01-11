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
    checkNumberTruthiness(1, 0);
  }

  @Test
  public void itEvaluatesDoubles() {
    checkNumberTruthiness(0.5, 0.0);
  }

  @Test
  public void itEvaluatesLongs() {
    checkNumberTruthiness(1L, 0L);
  }

  @Test
  public void itEvaluatesShorts() {
    checkNumberTruthiness((short) 1, (short) 0);
  }

  @Test
  public void itEvaluatesFloats() {
    checkNumberTruthiness(0.5f, 0.0f);
  }

  private void checkNumberTruthiness(Object a, Object b) {
    assertThat(ObjectTruthValue.evaluate(a)).isTrue();
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
