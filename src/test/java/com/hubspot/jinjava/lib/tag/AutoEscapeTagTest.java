package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class AutoEscapeTagTest extends BaseTagTest {

  @Test
  public void itEscapesVarsInScope() throws IOException {
    Map<String, Object> context = new HashMap<>();
    context.put("myvar", "foo < bar");

    String template = Resources.toString(
      Resources.getResource("tags/autoescapetag/autoescape.jinja"),
      StandardCharsets.UTF_8
    );
    String result = jinjava.render(template, context);

    assertThat(result).contains("1. foo < bar", "2. foo &lt; bar", "3. foo < bar");
  }
}
