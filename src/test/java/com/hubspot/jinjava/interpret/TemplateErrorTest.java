package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class TemplateErrorTest {

  @Test
  public void itShowsFriendlyNameOfBaseObjectForPropNotFound() {
    TemplateError e = TemplateError.fromUnknownProperty(new Object(), "foo", 123, 4);
    assertThat(e.getMessage()).isEqualTo("Cannot resolve property 'foo' in 'Object'");
  }

  @Test
  public void itUsesOverloadedToStringForBaseObject() {
    TemplateError e = TemplateError.fromUnknownProperty(ImmutableMap.of("foo", "bar"), "other", 123, 4);
    assertThat(e.getMessage()).isEqualTo("Cannot resolve property 'other' in '{foo=bar}'");
  }

  @Test
  public void itShowsFieldNameForUnknownTagError() {
    TemplateError e = TemplateError.fromException(new UnknownTagException("unknown", "{% unknown() %}", 11, 3));
    assertThat(e.getFieldName()).isEqualTo("unknown");
  }

  @Test
  public void itShowsFieldNameForSyntaxError() {
    TemplateError e = TemplateError.fromException(new TemplateSyntaxException("da codez", "{{ lolo lolo }}", 11));
    assertThat(e.getFieldName()).isEqualTo("da codez");
  }

}
