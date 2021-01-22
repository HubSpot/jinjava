package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.io.Resources;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.FromTag;
import com.hubspot.jinjava.lib.tag.FromTagTest;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.loader.LocationResolver;
import com.hubspot.jinjava.loader.RelativePathResolver;
import com.hubspot.jinjava.loader.ResourceLocator;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EagerFromTagTest extends FromTagTest {

  @Before
  public void eagerSetup() {
    jinjava.setResourceLocator(
      new ResourceLocator() {
        private RelativePathResolver relativePathResolver = new RelativePathResolver();

        @Override
        public String getString(
          String fullName,
          Charset encoding,
          JinjavaInterpreter interpreter
        ) throws IOException {
          return Resources.toString(
            Resources.getResource(String.format("tags/macrotag/%s", fullName)),
            StandardCharsets.UTF_8
          );
        }

        @Override
        public Optional<LocationResolver> getLocationResolver() {
          return Optional.of(relativePathResolver);
        }
      }
    );
    context.put("padding", 42);
    interpreter =
      new JinjavaInterpreter(
        jinjava,
        context,
        JinjavaConfig
          .newBuilder()
          .withExecutionMode(EagerExecutionMode.instance())
          .build()
      );
    Tag tag = EagerTagFactory
      .getEagerTagDecorator(new FromTag())
      .orElseThrow(RuntimeException::new);
    context.registerTag(tag);
    context.put("deferred", DeferredValue.instance());
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  @Ignore
  @Override
  public void itDefersImport() {}
}
