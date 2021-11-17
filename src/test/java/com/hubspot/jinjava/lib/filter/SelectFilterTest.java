package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class SelectFilterTest extends BaseJinjavaTest {

  @Before
  public void setup() {
    jinjava.getGlobalContext().put("numbers", Lists.newArrayList(1L, 2L, 3L, 4L, 5L));
  }

  @Test
  public void testSelect() {
    assertThat(jinjava.render("{{numbers|select('odd')}}", new HashMap<>()))
      .isEqualTo("[1, 3, 5]");
    assertThat(jinjava.render("{{numbers|select('even')}}", new HashMap<>()))
      .isEqualTo("[2, 4]");
  }

  @Test
  public void testSelectWithEqualToAttr() {
    assertThat(jinjava.render("{{numbers|select('equalto', 3)}}", new HashMap<>()))
      .isEqualTo("[3]");
  }

  @Test
  public void itThrowsInvalidArgumentForNullExpTestArgument() {
    Map<String, Object> context = new HashMap<>();
    context.put("test", null);
    assertThatThrownBy(() -> jinjava.render("{{numbers|select(test, 3)}}", context))
      .hasMessageContaining("'exp_test' argument cannot be null");
  }
}
