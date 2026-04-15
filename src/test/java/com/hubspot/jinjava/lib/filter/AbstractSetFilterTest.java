package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.features.BuiltInFeatures;
import com.hubspot.jinjava.features.FeatureConfig;
import com.hubspot.jinjava.features.FeatureStrategies;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  @Test
  public void itDoesNotThrowWarningOnIntegerLongMismatch() {
    JinjavaInterpreter interpreter = jinjava.newInterpreter();

    Set<Object> varSet = concreteSetFilter.objectToSet(new Long[] { 1L, 2L, 3L });
    Set<Object> argSet = concreteSetFilter.objectToSet(new Integer[] { 1, 2, 3 });
    concreteSetFilter.attachMismatchedTypesWarning(interpreter, varSet, argSet);

    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itConvertsIntegerToLongWhenFeatureActive() {
    Jinjava jinjavaWithFeature = new Jinjava(
      JinjavaConfig
        .newBuilder()
        .withLegacyOverrides(
          LegacyOverrides
            .newBuilder()
            .withUsePyishObjectMapper(true)
            .withKeepNullableLoopValues(true)
            .build()
        )
        .withFeatureConfig(
          FeatureConfig
            .newBuilder()
            .add(BuiltInFeatures.INTEGER_SET_TO_LONG_CONVERSION, FeatureStrategies.ACTIVE)
            .build()
        )
        .build()
    );

    Map<String, Object> vars = new HashMap<>();
    vars.put("longList", new Long[] { 1L, 2L, 3L });
    vars.put("intList", new Integer[] { 2, 3, 4 });

    String result = jinjavaWithFeature.render("{{ longList|intersect(intList) }}", vars);

    assertThat(result).isEqualTo("[2, 3]");
  }

  @Test
  public void itDoesNotConvertWhenFeatureInactive() {
    Map<String, Object> vars = new HashMap<>();
    vars.put("longList", new Long[] { 1L, 2L, 3L });
    vars.put("intList", new Integer[] { 2, 3, 4 });

    RenderResult renderResult = jinjava.renderForResult(
      "{{ longList|intersect(intList) }}",
      vars
    );

    assertThat(renderResult.getOutput()).isEqualTo("[]");
    assertThat(renderResult.getErrors()).isEmpty();
  }
}
