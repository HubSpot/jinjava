package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import java.util.HashMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class IntersectFilterTest extends BaseJinjavaTest {

  @Before
  public void setup() {
    jinjava.getGlobalContext().registerClasses(EscapeJsFilter.class);
  }

  @Test
  public void itComputesSetIntersections() {
    assertThat(
        jinjava.render("{{ [1, 1, 2, 3]|intersect([1, 2, 5, 6]) }}", new HashMap<>())
      )
      .isEqualTo("[1, 2]");
    assertThat(
        jinjava.render("{{ ['do', 'ray']|intersect(['ray', 'me']) }}", new HashMap<>())
      )
      .isEqualTo("['ray']");
  }

  @Test
  public void itReturnsEmptyOnNullParameters() {
    assertThat(jinjava.render("{{ [1, 2, 3]|intersect(null) }}", new HashMap<>()))
      .isEqualTo("[]");
  }

  @Test
  public void itDoesNotThrowWarningOnMatchedTypes() {
    JinjavaInterpreter interpreter = jinjava.newInterpreter();

    String renderedOutput = interpreter.render("{{ [1, 2, 3]|intersect([1, 2, 3]) }}");
    assertThat(renderedOutput).isEqualTo("[1, 2, 3]");

    List<TemplateError> errors = interpreter.getErrors();
    assertThat(errors).isEmpty();
  }

  @Test
  public void itThrowsWarningOnMismatchTypes() {
    JinjavaInterpreter interpreter = jinjava.newInterpreter();

    String renderedOutput = interpreter.render(
      "{{ [1, 2, 3]|intersect(['1', '2', '3']) }}"
    );
    assertThat(renderedOutput).isEqualTo("[]");

    List<TemplateError> errors = interpreter.getErrors();
    assertThat(errors).isNotEmpty();

    TemplateError error = errors.get(0);
    assertThat(error.getSeverity()).isEqualTo(TemplateError.ErrorType.WARNING);
    assertThat(error.getMessage())
      .isEqualTo(
        "Mismatched types. `value` elements are of type `long` and `list` elements are of type `str`. This may lead to unexpected behavior."
      );
  }
}
