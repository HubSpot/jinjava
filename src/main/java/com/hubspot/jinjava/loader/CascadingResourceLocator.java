package com.hubspot.jinjava.loader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class CascadingResourceLocator implements ResourceLocator {

  private Iterable<ResourceLocator> locators;

  public CascadingResourceLocator(ResourceLocator... locators) {
    this.locators = Arrays.asList(locators);
  }

  @Override
  public String getString(String fullName, Charset encoding,
      JinjavaInterpreter interpreter) throws IOException {

    for (ResourceLocator locator : locators) {
      try {
        return locator.getString(fullName, encoding, interpreter);
      } catch (ResourceNotFoundException e) { /* */
      }
    }

    throw new ResourceNotFoundException("Couldn't find resource: " + fullName);
  }

}
