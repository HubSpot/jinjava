package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.Collections;
import org.junit.Test;

public class Base64FilterTest extends BaseJinjavaTest {

  @Test
  public void itEncodesWithDefaultCharset() {
    assertThat(jinjava.render("{{ 'ß'|b64encode }}", Collections.emptyMap()))
      .isEqualTo("w58=");
  }

  @Test
  public void itEncodesWithUtf16Le() {
    assertThat(
        jinjava.render("{{ 'ß'|b64encode(encoding='utf-16le') }}", Collections.emptyMap())
      )
      .isEqualTo("3wA=");
  }

  @Test
  public void itDecodesWithUtf16Le() {
    assertThat(
        jinjava.render(
          "{{ '3wA='|b64decode(encoding='utf-16le') }}",
          Collections.emptyMap()
        )
      )
      .isEqualTo("ß");
  }

  @Test
  public void itEncodesAndDecodesDefaultCharset() {
    assertThat(
        jinjava.render("{{ 123456789|b64encode|b64decode }}", Collections.emptyMap())
      )
      .isEqualTo("123456789");
  }

  @Test
  public void itEncodesAndDecodesUtf16Le() {
    assertThat(
        jinjava.render(
          "{{ 123456789|b64encode(encoding='utf-16le')|b64decode(encoding='utf-16le') }}",
          Collections.emptyMap()
        )
      )
      .isEqualTo("123456789");
  }

  @Test
  public void itEncodesObject() {
    assertThat(jinjava.render("{{ {'foo': ['bar']}|b64encode }}", Collections.emptyMap()))
      .isEqualTo("eydmb28nOiBbJ2JhciddfQ==");
  }
}
