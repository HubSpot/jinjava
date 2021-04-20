package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.TemplateError.ErrorItem;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.interpret.errorcategory.BasicTemplateErrorCategory;
import org.junit.Test;

public class TemplateErrorTest {

  @Test
  public void itShowsFriendlyNameOfBaseObjectForPropNotFound() {
    TemplateError e = TemplateError.fromUnknownProperty(new Object(), "foo", 123, 4);
    assertThat(e.getMessage()).isEqualTo("Cannot resolve property 'foo' in 'Object'");
  }

  @Test
  public void itUsesOverloadedToStringForBaseObject() {
    TemplateError e = TemplateError.fromUnknownProperty(
      ImmutableMap.of("foo", "bar"),
      "other",
      123,
      4
    );
    assertThat(e.getMessage())
      .isEqualTo("Cannot resolve property 'other' in '{foo=bar}'");
  }

  @Test
  public void itShowsFieldNameForUnknownTagError() {
    TemplateError e = TemplateError.fromException(
      new UnknownTagException("unKnown", "{% unKnown() %}", 11, 3)
    );
    assertThat(e.getFieldName()).isEqualTo("unKnown");
  }

  @Test
  public void itShowsFieldNameForSyntaxError() {
    TemplateError e = TemplateError.fromException(
      new TemplateSyntaxException("da codez", "{{ lolo lolo }}", 11)
    );
    assertThat(e.getFieldName()).isEqualTo("da codez");
  }

  @Test
  public void itRetainsFieldNameCaseForUnknownToken() {
    JinjavaInterpreter interpreter = new Jinjava().newInterpreter();
    interpreter.render("{% unKnown() %}");
    assertThat(interpreter.getErrorsCopy().get(0).getFieldName()).isEqualTo("unKnown");
  }

  @Test
  public void itSetsFieldNameCaseForSyntaxErrorInFor() {
    RenderResult renderResult = new Jinjava()
    .renderForResult("{% for item inna navigation %}{% endfor %}", ImmutableMap.of());
    assertThat(renderResult.getErrors().get(0).getFieldName())
      .isEqualTo("item inna navigation");
  }

  @Test
  public void itLimitsErrorStringToAReasonableSize() {
    String veryLong = "";

    for (int i = 0; i < 1500; i++) {
      veryLong = veryLong.concat("0");
    }

    TemplateError e = TemplateError.fromUnknownProperty(
      ImmutableMap.of("foo", veryLong),
      "other",
      123,
      4
    );
    assertThat(e.getMessage()).startsWith("Cannot resolve property 'other' in '{foo=");
    assertThat(e.getMessage().length()).isLessThan(1500);
  }

  @Test
  public void itComparesEquality() {
    TemplateError error1 = new TemplateError(
      ErrorType.WARNING,
      ErrorReason.SYNTAX_ERROR,
      ErrorItem.TAG,
      "error",
      "badField",
      10,
      100,
      new Exception(),
      BasicTemplateErrorCategory.FROM_CYCLE_DETECTED,
      ImmutableMap.of("test1", "test2")
    );

    TemplateError error2 = new TemplateError(
      ErrorType.WARNING,
      ErrorReason.SYNTAX_ERROR,
      ErrorItem.TAG,
      "error",
      "badField",
      10,
      100,
      new Exception(),
      BasicTemplateErrorCategory.FROM_CYCLE_DETECTED,
      ImmutableMap.of("test1", "test2")
    );

    TemplateError error3 = new TemplateError(
      ErrorType.WARNING,
      ErrorReason.SYNTAX_ERROR,
      ErrorItem.TAG,
      "error",
      "differentField",
      10,
      100,
      new Exception(),
      BasicTemplateErrorCategory.FROM_CYCLE_DETECTED,
      ImmutableMap.of("test1", "test2")
    );

    assertThat(error1).isEqualTo(error2);
    assertThat(error1).isNotEqualTo(error3);
  }
}
