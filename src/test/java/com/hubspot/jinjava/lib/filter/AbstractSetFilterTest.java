package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class AbstractSetFilterTest extends BaseJinjavaTest {
  private static final IntersectFilter concreteSetFilter = new IntersectFilter();

  @Before
  public void setup() {
    jinjava.getGlobalContext().registerClasses(EscapeJsFilter.class);
  }

  @Test
  public void itDoesNotThrowWarningOnMatchedTypes() {
    JinjavaInterpreter interpreter = jinjava.newInterpreter();

    // {{ [1, 2, 3]|intersect([1, 2, 3]) }}
    Set<Object> varSet = concreteSetFilter.objectToSet(new Long[] { 1L, 2L, 3L });
    Set<Object> argSet = concreteSetFilter.objectToSet(new Long[] { 1L, 2L, 3L });
    concreteSetFilter.attachMismatchedTypesWarning(interpreter, varSet, argSet);

    List<TemplateError> errors = interpreter.getErrors();
    assertThat(errors).isEmpty();
  }

  @Test
  public void itDoesNotThrowWarningOnEmptyVarSet() {
    JinjavaInterpreter interpreter = jinjava.newInterpreter();

    String renderedOutput = interpreter.render("{{ []|intersect([1, 2, 3]) }}");
    assertThat(renderedOutput).isEqualTo("[]");

    // {{ []|intersect([1, 2, 3]) }}
    Set<Object> varSet = concreteSetFilter.objectToSet(new Object[] {});
    Set<Object> argSet = concreteSetFilter.objectToSet(new Long[] { 1L, 2L, 3L });
    concreteSetFilter.attachMismatchedTypesWarning(interpreter, varSet, argSet);

    List<TemplateError> errors = interpreter.getErrors();
    assertThat(errors).isEmpty();
  }

  @Test
  public void itDoesNotThrowWarningOnEmptyArgSet() {
    JinjavaInterpreter interpreter = jinjava.newInterpreter();

    // {{ [1, 2, 3]|intersect([]) }}
    Set<Object> varSet = concreteSetFilter.objectToSet(new Long[] { 1L, 2L, 3L });
    Set<Object> argSet = concreteSetFilter.objectToSet(new Object[] {});
    concreteSetFilter.attachMismatchedTypesWarning(interpreter, varSet, argSet);

    List<TemplateError> errors = interpreter.getErrors();
    assertThat(errors).isEmpty();
  }

  @Test
  public void itThrowsWarningOnMismatchTypes() {
    JinjavaInterpreter interpreter = jinjava.newInterpreter();

    // {{ [1, 2, 3]|intersect(['1', '2', '3']) }}
    Set<Object> varSet = concreteSetFilter.objectToSet(new Long[] { 1L, 2L, 3L });
    Set<Object> argSet = concreteSetFilter.objectToSet(new String[] { "1", "2", "3" });
    concreteSetFilter.attachMismatchedTypesWarning(interpreter, varSet, argSet);

    List<TemplateError> errors = interpreter.getErrors();
    assertThat(errors).isNotEmpty();

    TemplateError error = errors.get(0);
    assertThat(error.getSeverity()).isEqualTo(TemplateError.ErrorType.WARNING);
    assertThat(error.getMessage())
      .isEqualTo(
        "Mismatched Types: input set has elements of type 'long' but arg set has elements of type 'str'. Use |map filter to convert sets to the same type for filter to work correctly."
      );
  }
}
