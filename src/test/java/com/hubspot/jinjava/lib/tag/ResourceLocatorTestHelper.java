package com.hubspot.jinjava.lib.tag;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.loader.LocationResolver;
import com.hubspot.jinjava.loader.RelativePathResolver;
import com.hubspot.jinjava.loader.ResourceLocator;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

public class ResourceLocatorTestHelper {

  public static ResourceLocator getTestResourceLocator(Map<String, String> templates) {
    return new ResourceLocator() {
      private final RelativePathResolver relativePathResolver =
        new RelativePathResolver();

      @Override
      public String getString(
        String fullName,
        Charset encoding,
        JinjavaInterpreter interpreter
      ) throws IOException {
        String template = templates.get(fullName);
        if (template == null) {
          throw new IOException("Template not found: " + fullName);
        }
        return template;
      }

      @Override
      public Optional<LocationResolver> getLocationResolver() {
        return Optional.of(relativePathResolver);
      }
    };
  }
}
