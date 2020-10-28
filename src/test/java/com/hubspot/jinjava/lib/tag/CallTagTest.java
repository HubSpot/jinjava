package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CallTagTest extends BaseTagTest {

  @Before
  public void setup() {
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void cleanup() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void testSimpleFn() {
    Document dom = Jsoup.parseBodyFragment(interpreter.render(fixture("simple")));
    assertThat(dom.select("div h2").text().trim()).isEqualTo("Hello World");
    assertThat(dom.select("div.contents").text().trim())
      .isEqualTo("This is a simple dialog rendered by using a macro and a call block.");
  }

  private String fixture(String name) {
    try {
      return Resources.toString(
        Resources.getResource(String.format("tags/calltag/%s.jinja", name)),
        StandardCharsets.UTF_8
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
