package com.hubspot.jinjava.testobjects;

import com.hubspot.jinjava.util.HasObjectTruthValue;

public class IfTagTestObjects {

  public static class Foo implements HasObjectTruthValue {

    private boolean objectTruthValue = false;

    public Foo setObjectTruthValue(boolean objectTruthValue) {
      this.objectTruthValue = objectTruthValue;
      return this;
    }

    @Override
    public boolean getObjectTruthValue() {
      return objectTruthValue;
    }
  }
}
