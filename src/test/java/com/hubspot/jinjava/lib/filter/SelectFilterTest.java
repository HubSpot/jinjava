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
    jinjava.getGlobalContext().put("numbers", Lists.newArrayList(1L, 2L, 3L, 4L, 5L));
  }

  @Test
  public void testSelect() {
    assertThat(jinjava.render("{{numbers|select('odd')}}", new HashMap<>())).isEqualTo("[1, 3, 5]");
    assertThat(jinjava.render("{{numbers|select('even')}}", new HashMap<>())).isEqualTo("[2, 4]");
  }

  @Test
  public void testSelectWithEqualToAttr() {
    assertThat(jinjava.render("{{numbers|select('equalto', 3)}}", new HashMap<>())).isEqualTo("[3]");
  }

}
