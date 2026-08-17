package com.hubspot.jinjava.loader;

import com.google.common.io.Resources;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClasspathResourceLocator implements ResourceLocator {

  @Override
  public String getString(
    String fullName,
    Charset encoding,
    JinjavaInterpreter interpreter
  ) throws IOException {
    if (escapesClasspathRoot(fullName)) {
      throw new ResourceNotFoundException("Path escapes classpath root: " + fullName);
    }

    try {
      return Resources.toString(Resources.getResource(fullName), encoding);
    } catch (IllegalArgumentException e) {
      throw new ResourceNotFoundException("Couldn't find resource: " + fullName);
    }
  }

  private static boolean escapesClasspathRoot(String fullName) {
    Path path = Paths.get(fullName);

    if (path.isAbsolute()) {
      return true;
    }

    for (Path segment : path) {
      if (segment.toString().equals("..")) {
        return true;
      }
    }

    return false;
  }
}
