package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TextNode;
import com.hubspot.jinjava.tree.parse.TextToken;

@RunWith(MockitoJUnitRunner.class)
public class ValidationModeTest {

  JinjavaInterpreter interpreter;

  Jinjava jinjava;
  private Context context;

  @Before
  public void setup() {
    jinjava = new Jinjava();
    interpreter = jinjava.newInterpreter();
    context = interpreter.getContext();
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void tearDown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itResolvesAllIfExpressionsInValidationMode() {

    interpreter.setValidationMode(true);

    interpreter.render(
        "{{ badCode( }}" +
            "{% if false %}" +
            "  {{ badCode( }}" +
            "{% endif %}");

    assertThat(interpreter.getErrors().size()).isEqualTo(2);
  }

  @Test
  public void itResolvesAllUnlessExpressionsInValidationMode() {

    interpreter.setValidationMode(true);

    interpreter.render(
        "{{ badCode( }}" +
            "{% unless false %}" +
            "  {{ badCode( }}" +
            "{% endunless %}");

    assertThat(interpreter.getErrors().size()).isEqualTo(2);
  }

  @Test
  public void itDoesNotSetValuesInValidatedBlocks() {

    interpreter.setValidationMode(true);

    String output = interpreter.render(
        "{% set foo = \"orig value\" %}" +
            "{% if false %}" +
            "  {% set foo = \"in false block\" %}" +
            "{% endif %}" +
            "{{ foo }}");

    assertThat(output.trim()).isEqualTo("orig value");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itDoesNotSetValuesInNestedValidatedBlocks() {

    interpreter.setValidationMode(true);

    String output = interpreter.render(
        "{% set foo = \"orig value\" %}" +
            "{% if false %}" +
            " {% if true %}" +
            "  {% set foo = \"in nested block\" %}" +
            " {% endif %}" +
            "{% endif %}" +
            "{{ foo }}");

    assertThat(output.trim()).isEqualTo("orig value");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itDoesNotPrintValuesInNestedValidatedBlocks() {

    interpreter.setValidationMode(true);

    String output = interpreter.render(
        "hi " +
            "{% if false %}" +
            " hidey " +
            "  {% if true %}" +
            "    hey" +
            "  {% endif %}" +
            "{% endif %}" +
            "there");

    assertThat(output.trim()).isEqualTo("hi there");
    assertThat(interpreter.getErrors()).isEmpty();
  }

  private class InstrumentedMacroFunction extends MacroFunction {

    private int invocationCount = 0;

    InstrumentedMacroFunction(List<Node> content,
                              String name,
                              LinkedHashMap<String, Object> argNamesWithDefaults,
                              boolean catchKwargs,
                              boolean catchVarargs,
                              boolean caller, Context localContextScope) {
      super(content, name, argNamesWithDefaults, catchKwargs, catchVarargs, caller, localContextScope);
    }

    @Override
    public Object doEvaluate(Map<String, Object> argMap, Map<String, Object> kwargMap, List<Object> varArgs) {
      invocationCount++;
      return super.doEvaluate(argMap, kwargMap, varArgs);
    }

    int getInvocationCount() {
      return invocationCount;
    }
  }

  @Test
  public void itDoesNotExecuteMacrosInValidatedBlocks() {

    TextNode textNode = new TextNode(new TextToken("hello", 1, 1));
    InstrumentedMacroFunction macro = new InstrumentedMacroFunction(ImmutableList.of(textNode), "hello", new LinkedHashMap<>(), false, false, false, interpreter
        .getContext());
    interpreter.getContext().addGlobalMacro(macro);

    String template =
        "{{ hello() }}" +
            "{% if false %} " +
            "  {{ hello() }}" +
            "{% endif %}";

    assertThat(interpreter.getErrors()).isEmpty();
    assertThat(interpreter.render(template).trim()).isEqualTo("hello");
    assertThat(macro.getInvocationCount()).isEqualTo(1);

    interpreter.setValidationMode(true);

    assertThat(interpreter.render(template).trim()).isEqualTo("hello");
    assertThat(macro.getInvocationCount()).isEqualTo(3);
    assertThat(interpreter.getErrors()).isEmpty();
  }

  private static int functionExecutionCount = 0;

  public static int validationTestFunction() {
    return ++functionExecutionCount;
  }

  @Test
  public void itDoesNotExecuteFunctionsInValidatedBlocks() {

    functionExecutionCount = 0;

    ELFunctionDefinition func = new ELFunctionDefinition("", "validation_test", ValidationModeTest.class, "validationTestFunction");

    Jinjava jinjava = new Jinjava();
    jinjava.getGlobalContext().registerFunction(func);
    JinjavaInterpreter interpreter = jinjava.newInterpreter();
    JinjavaInterpreter.pushCurrent(interpreter);

    assertThat(functionExecutionCount).isEqualTo(0);

    String template =
        "{{ validation_test() }}" +
            "{% if false %}" +
            "  {{ validation_test() }}" +
            "  {{ hey( }}" +
            "{% endif %}";

    String result = interpreter.render(template);
    assertThat(interpreter.getErrors()).isEmpty();
    assertThat(result).isEqualTo("1");
    assertThat(functionExecutionCount).isEqualTo(1);

    interpreter.setValidationMode(true);
    result = interpreter.render(template);

    assertThat(interpreter.getErrors().size()).isEqualTo(1);
    assertThat(interpreter.getErrors().get(0).getMessage()).contains("hey(");
    assertThat(result).isEqualTo("2");
    assertThat(functionExecutionCount).isEqualTo(2);
  }

  class ValidationFilter implements Filter {

    private int executionCount = 0;

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      executionCount++;
      return var;
    }

    public int getExecutionCount() {
      return executionCount;
    }

    @Override
    public String getName() {
      return "validation_filter";
    }
  }

  @Test
  public void itDoesNotExecuteFiltersInValidatedBlocks() {

    ValidationFilter validationFilter = new ValidationFilter();
    Jinjava jinjava = new Jinjava();
    jinjava.getGlobalContext().registerFilter(validationFilter);
    JinjavaInterpreter interpreter = jinjava.newInterpreter();
    JinjavaInterpreter.pushCurrent(interpreter);

    assertThat(validationFilter.getExecutionCount()).isEqualTo(0);

    String template =
        "  {{ 10|validation_filter() }}" +
            "{% if false %}" +
            "  {{ 10|validation_filter() }}" +
            "  {{ hey( }}" +
            "{% endif %}";

    String result = interpreter.render(template).trim();
    assertThat(interpreter.getErrors()).isEmpty();
    assertThat(result).isEqualTo("10");
    assertThat(validationFilter.getExecutionCount()).isEqualTo(1);

    interpreter.setValidationMode(true);
    result = interpreter.render(template).trim();

    assertThat(interpreter.getErrors().size()).isEqualTo(1);
    assertThat(interpreter.getErrors().get(0).getMessage()).contains("hey(");
    assertThat(result).isEqualTo("10");
    assertThat(validationFilter.getExecutionCount()).isEqualTo(2);
  }

}
