package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
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
      jinjava.render(
        "{{ '\uD801\uDC37'|b64encode(encoding='utf-16le') }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("Adg33A==");
  }

  @Test
  public void itDecodesWithUtf16Le() {
    assertThat(
      jinjava.render(
        "{{ 'Adg33A=='|b64decode(encoding='utf-16le') }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("\uD801\uDC37");
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

  @Test
  public void itHandlesInvalidDecode() {
    RenderResult renderResult = jinjava.renderForResult(
      "{{ 'ß'|b64decode }}",
      Collections.emptyMap()
    );
    assertThat(renderResult.getErrors()).hasSize(1);
    assertThat(renderResult.getErrors().get(0).getException())
      .isInstanceOf(TemplateSyntaxException.class);
  }

  @Test
  public void itThrowsErrorForNonStringDecode() {
    RenderResult renderResult = jinjava.renderForResult(
      "{{ 123|b64decode }}",
      Collections.emptyMap()
    );
    assertThat(renderResult.getErrors()).hasSize(1);
    assertThat(renderResult.getErrors().get(0).getException())
      .isInstanceOf(InvalidArgumentException.class);
  }
}
