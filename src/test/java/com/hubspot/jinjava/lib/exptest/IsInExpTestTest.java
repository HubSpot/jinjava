package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.HashMap;
import org.junit.Test;

public class IsInExpTestTest extends BaseJinjavaTest {

  @Test
  public void testIsInList() {
    assertThat(jinjava.render("{{ 2 is in [1, 2] }}", new HashMap<>())).isEqualTo("true");
    //    TODO: Uncomment out when CollectionMemberShipOperator.java changes get approved
    //    assertThat(jinjava.render("{{ 2 is in ['one', 2] }}", new HashMap<>()))
    //      .isEqualTo("true");
    assertThat(jinjava.render("{{ 2 is in [1] }}", new HashMap<>())).isEqualTo("false");
  }

  @Test
  public void testNull() {
    assertThat(jinjava.render("{{ null is in [null] }}", new HashMap<>()))
      .isEqualTo("true");
    assertThat(jinjava.render("{{ null is in [2] }}", new HashMap<>()))
      .isEqualTo("false");
    assertThat(jinjava.render("{{ 2 is in [null] }}", new HashMap<>()))
      .isEqualTo("false");
    assertThatThrownBy(() -> jinjava.render("{{ 2 is in null }}", new HashMap<>()))
      .hasMessageContaining("1st argument with value 'null' must be iterable");
  }

  @Test
  public void itEvaluatesWithoutIn() {
    assertThat(jinjava.render("{{ 2 in [1, 2] }}", new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render("{{ 2 in [1] }}", new HashMap<>())).isEqualTo("false");

    assertThat(jinjava.render("{{ 2 not in [1, 2] }}", new HashMap<>()))
      .isEqualTo("false");
    assertThat(jinjava.render("{{ 2 not in [1] }}", new HashMap<>())).isEqualTo("true");
  }
}
