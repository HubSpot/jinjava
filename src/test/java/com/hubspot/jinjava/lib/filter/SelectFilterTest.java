package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.Jinjava;

public class SelectFilterTest {

  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
    jinjava.getGlobalContext().put("numbers", Lists.newArrayList(1, 2, 3, 4, 5));
  }

  @Test
  public void testSelect() {
    assertThat(jinjava.render("{{numbers|select('odd')}}", new HashMap<String, Object>())).isEqualTo("[1, 3, 5]");
    assertThat(jinjava.render("{{numbers|select('even')}}", new HashMap<String, Object>())).isEqualTo("[2, 4]");
  }

}
