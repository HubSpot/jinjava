package com.hubspot.jinjava.testobjects;

import com.hubspot.jinjava.util.HasObjectTruthValue;

public class IfTagTestObject implements HasObjectTruthValue {

  private boolean objectTruthValue = false;

  public IfTagTestObject setObjectTruthValue(boolean objectTruthValue) {
    this.objectTruthValue = objectTruthValue;
    return this;
  }

  @Override
  public boolean getObjectTruthValue() {
    return objectTruthValue;
  }
}
