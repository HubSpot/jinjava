package com.hubspot.jinjava.loader;

import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.io.Resources;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class ClasspathResourceLocator implements ResourceLocator {

  @Override
  public String getString(String fullName, Charset encoding,
      JinjavaInterpreter interpreter) throws IOException {
    try {
      return Resources.toString(Resources.getResource(fullName), encoding);
    } catch (IllegalArgumentException e) {
      throw new ResourceNotFoundException("Couldn't find resource: " + fullName);
    }
  }

}
