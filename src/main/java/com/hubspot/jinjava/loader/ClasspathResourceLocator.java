package com.hubspot.jinjava.loader;

import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.io.Resources;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class ClasspathResourceLocator implements ResourceLocator {

  @Override
  public String getString(String fullName, Charset encoding,
      JinjavaInterpreter interpreter) throws IOException {
    final String renderName = String.format("InterpreterGetResource:%s", fullName);
    try {
      if (interpreter != null) {
        interpreter.startRender(renderName);
      }
      return Resources.toString(Resources.getResource(fullName), encoding);
    } catch (IllegalArgumentException e) {
      throw new ResourceNotFoundException("Couldn't find resource: " + fullName);
    } finally {
      if (interpreter != null) {
        interpreter.endRender(renderName);
      }
    }
  }

}
