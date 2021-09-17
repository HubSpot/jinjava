package com.hubspot.jinjava.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class ObjectTruthValueTest {

  @Test
  public void itEvaluatesObjectsWithObjectTruthValue() {
    assertThat(ObjectTruthValue.evaluate(new TestObject().setObjectTruthValue(true)))
      .isTrue();
    assertThat(ObjectTruthValue.evaluate(new TestObject().setObjectTruthValue(false)))
      .isFalse();
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
