package com.hubspot.jinjava.lib.filter;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.BaseJinjavaTest;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RejectFilterTest extends BaseJinjavaTest {

  @Before
  public void setup() {
    jinjava.getGlobalContext().put("numbers", Lists.newArrayList(1L, 2L, 3L, 4L, 5L));
  }

  @Test
  public void testReject() {
    assertThat(jinjava.render("{{numbers|reject('odd')}}", new HashMap<>()))
      .isEqualTo("[2, 4]");
    assertThat(jinjava.render("{{numbers|reject('even')}}", new HashMap<>()))
      .isEqualTo("[1, 3, 5]");
  }

  @Test
  public void testRejectWithEqualToAttr() {
    assertThat(jinjava.render("{{numbers|reject('equalto', 3)}}", new HashMap<>()))
      .isEqualTo("[1, 2, 4, 5]");
  }

  @Test
  public void itThrowsInvalidArgumentForNullExpTestArgument() {
    Map<String, Object> context = new HashMap<>();
    context.put("test", null);
    assertThatThrownBy(() -> jinjava.render("{{numbers|reject(test, 3)}}", context))
      .hasMessageContaining("1st argument cannot be null");
  }
}
