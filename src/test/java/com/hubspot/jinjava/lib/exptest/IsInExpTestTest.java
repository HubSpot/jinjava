package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.Jinjava;
import java.util.HashMap;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class IsInExpTestTest {
  private Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void testIsInList() {
    assertThat(jinjava.render("{{ 2 is in [1, 2] }}", new HashMap<>())).isEqualTo("true");
    //    TODO: Uncomment out when CollectionMemberShipOperator.java changes get approved
    //    assertThat(jinjava.render("{{ 2 is in ['one', 2] }}", new HashMap<>()))
    //      .isEqualTo("true");
    assertThat(jinjava.render("{{ 2 is in [1] }}", new HashMap<>())).isEqualTo("false");
  }

  @Test
  public void testIsInString() {
    assertThat(jinjava.render("{{ 'b' is in 'ab' }}", new HashMap<>())).isEqualTo("true");
    assertThat(jinjava.render("{{ 'b' is in 'a' }}", new HashMap<>())).isEqualTo("false");
  }

  @Test
  public void testIsInDict() {
    //    TODO: Uncomment out when CollectionMemberShipOperator.java changes get approved
    //    assertThat(jinjava.render("{{ 'k2' is in {'k1':'v1', 'k2':'v2'} }}", new HashMap<>()))
    //      .isEqualTo("true");
    assertThat(jinjava.render("{{ 'k2' is in {'k1':'v1'} }}", new HashMap<>()))
      .isEqualTo("false");
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
}
