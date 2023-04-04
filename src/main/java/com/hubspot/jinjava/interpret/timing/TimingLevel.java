package com.hubspot.jinjava.interpret.timing;

public enum TimingLevel {
  NONE(0),
  LOW(10),
  HIGH(20),
  ALL(30);

  private final int value;

  TimingLevel(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
