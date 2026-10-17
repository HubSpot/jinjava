package com.hubspot.jinjava.interpret;

public class RenderingDisabledException extends InterpretException {

  public RenderingDisabledException() {
    super("Rendering is disabled");
  }
}
