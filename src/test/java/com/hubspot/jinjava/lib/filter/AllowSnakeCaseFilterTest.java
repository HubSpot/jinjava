package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import org.junit.Test;

public class AllowSnakeCaseFilterTest extends BaseInterpretingTest {

  @Test
  public void itDoesNotChangeNonMaps() {
    assertThat(interpreter.render("{{ 'fooBar'|allow_snake_case }}")).isEqualTo("fooBar");
  }

  @Test
  public void itMakesMapKeysAccessibleWithSnakeCase() {
    assertThat(interpreter.render("{{ ({'fooBar': 'foo'}|allow_snake_case).foo_bar }}"))
      .isEqualTo("foo");
  }

  @Test
  public void itReserializesAsSnakeCaseAccessibleMap() {
    interpreter.render("{% set map = {'fooBar': 'foo'}|allow_snake_case %}");
    assertThat(PyishObjectMapper.getAsPyishString(interpreter.getContext().get("map")))
      .isEqualTo("{'fooBar': 'foo'} |allow_snake_case");
  }
}
