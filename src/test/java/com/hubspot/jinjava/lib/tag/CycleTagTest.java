package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Maps;
import com.hubspot.jinjava.BaseInterpretingTest;
import org.junit.Test;

public class CycleTagTest extends BaseInterpretingTest {

  @Test
  public void itDefaultsNullToImage() {
    String template = "{% for item in [0,1] %}{% cycle {{item}} %}{% endfor %}";
    assertThat(jinjava.render(template, Maps.newHashMap())).isEqualTo("{{item}}{{item}}");
  }
}
