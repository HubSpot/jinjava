package com.hubspot.jinjava.interpret;

public interface OneTimeReconstructible extends DeferredValue {
  boolean isReconstructed();

  void setReconstructed(boolean reconstructed);
}
