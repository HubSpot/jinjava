package com.hubspot.jinjava.interpret.timing;

import java.time.Duration;

public enum TimingLevel {
  NONE(0),

  /**
   * Ignores the duration when calling {@link Timings#toString(TimingLevel, Duration)}
   */
  ALWAYS(1),
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
