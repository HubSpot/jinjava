package com.hubspot.jinjava.lib.tag;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TextNode;
import com.hubspot.jinjava.tree.parse.DefaultTokenScannerSymbols;
import com.hubspot.jinjava.tree.parse.TextToken;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidationModeTest {
  JinjavaInterpreter interpreter;
  JinjavaInterpreter validatingInterpreter;

  Jinjava jinjava;

  ValidationFilter validationFilter;

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

  private static int functionExecutionCount = 0;

  public static int validationTestFunction() {
    return ++functionExecutionCount;
  }

  @Before
  public void setup() {
    validationFilter = new ValidationFilter();

    ELFunctionDefinition validationFunction = new ELFunctionDefinition(
      "",
      "validation_test",
      ValidationModeTest.class,
      "validationTestFunction"
    );

    jinjava = new Jinjava();
    jinjava.getGlobalContext().registerFilter(validationFilter);
    jinjava.getGlobalContext().registerFunction(validationFunction);
    interpreter = jinjava.newInterpreter();
    Context context = interpreter.getContext();

    validatingInterpreter =
      new JinjavaInterpreter(
        jinjava,
              context,
        JinjavaConfig.newBuilder().withValidationMode(true).build()
      );

    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void tearDown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itResolvesAllIfExpressionsInValidationMode() {
    validatingInterpreter.render(
      "{{ badCode( }}" + "{% if false %}" + "  {{ badCode( }}" + "{% endif %}"
    );

    assertThat(validatingInterpreter.getErrors().size()).isEqualTo(2);
  }

  @Test
  public void itResolvesAllUnlessExpressionsInValidationMode() {
    validatingInterpreter.render(
      "{{ badCode( }}" + "{% unless false %}" + "  {{ badCode( }}" + "{% endunless %}"
    );

    assertThat(validatingInterpreter.getErrors().size()).isEqualTo(2);
  }

  @Test
  public void itResolvesAllForExpressionsInValidationMode() {
    validatingInterpreter.render(
      "{{ badCode( }}" + "{% for i in [1, 2, 3] %}" + "  {{ badCode( }}" + "{% endfor %}"
    );

    assertThat(validatingInterpreter.getErrors().size()).isEqualTo(4);
  }

  @Test
  public void itResolvesNestedForExpressionsInValidationMode() {
    String output = validatingInterpreter.render(
      "{{ badCode( }}" +
      "{% for i in [] %}" +
      "  outer loop" +
      "  {% for i in [1, 2, 3] %}" +
      "    inner loop  {{ badCode( }}" +
      "  {% endfor %}" +
      "{% endfor %}" +
      "done"
    );

    assertThat(validatingInterpreter.getErrors().size()).isEqualTo(4);
    assertThat(output.trim()).isEqualTo("done");
  }

  @Test
  public void itResolvesZeroLoopForExpressionsInValidationMode() {
    String output = validatingInterpreter.render(
      "{{ badCode( }}" +
      "{% for i in [] %}" +
      "in loop {{ badCode( }}" +
      "{% endfor %}" +
      "hi"
    );

    assertThat(validatingInterpreter.getErrors().size()).isEqualTo(2);
    assertThat(output.trim()).isEqualTo("hi");
  }

  @Test
  public void itAllowsPropertyReferenceInForLoopInValidationMode() {
    String output = validatingInterpreter.render(
      "{% for i in [] %}" + "{{ i.test }}" + "{% endfor %}" + "hi"
    );

    assertThat(validatingInterpreter.getErrors().size()).isEqualTo(0);
    assertThat(output.trim()).isEqualTo("hi");
  }

  @Test
  public void itAllowsPropertyReferenceAndTypeCoercionInForLoopInValidationMode() {
    String output = validatingInterpreter.render(
      "{% for i in [] %}" +
      "{{ i.test + 100 }}" +
      "{{ i.nope ~ 'hello' }}" +
      "{% endfor %}" +
      "hi"
    );

    assertThat(validatingInterpreter.getErrors().size()).isEqualTo(0);
    assertThat(output.trim()).isEqualTo("hi");
  }

  @Test
  public void itResolvesZeroLoopTupleForExpressionsInValidationMode() {
    String output = validatingInterpreter.render(
      "{{ badCode( }}" +
      "{% set map = {} %}" +
      "{% for a, b in map.items() %}" +
      "in loop {{ badCode( }}" +
      "{% endfor %}" +
      "hi"
    );

    assertThat(validatingInterpreter.getErrors().size()).isEqualTo(2);
    assertThat(output.trim()).isEqualTo("hi");
  }

  @Test
  public void itDoesNotSetValuesInValidatedBlocks() {
    String output = validatingInterpreter.render(
      "{% set foo = \"orig value\" %}" +
      "{% if false %}" +
      "  {% set foo = \"in false block\" %}" +
      "{% endif %}" +
      "{{ foo }}"
    );

    assertThat(output.trim()).isEqualTo("orig value");
    assertThat(validatingInterpreter.getErrors()).isEmpty();
  }

  @Test
  public void itDoesNotSetValuesInNestedValidatedBlocks() {
    String output = validatingInterpreter.render(
      "{% set foo = \"orig value\" %}" +
      "{% if false %}" +
      " {% if true %}" +
      "  {% set foo = \"in nested block\" %}" +
      " {% endif %}" +
      "{% endif %}" +
      "{{ foo }}"
    );

    assertThat(output.trim()).isEqualTo("orig value");
    assertThat(validatingInterpreter.getErrors()).isEmpty();
  }

  @Test
  public void itDoesNotPrintValuesInNestedValidatedBlocks() {
    String output = validatingInterpreter.render(
      "hi " +
      "{% if false %}" +
      " hidey " +
      "  {% if true %}" +
      "    hey" +
      "  {% endif %}" +
      "{% endif %}" +
      "there"
    );

    assertThat(output.trim()).isEqualTo("hi there");
    assertThat(validatingInterpreter.getErrors()).isEmpty();
  }

  private class InstrumentedMacroFunction extends MacroFunction {
    private int invocationCount = 0;

    InstrumentedMacroFunction(
      List<Node> content,
      String name,
      LinkedHashMap<String, Object> argNamesWithDefaults,
      boolean caller,
      Context localContextScope
    ) {
      super(content, name, argNamesWithDefaults, caller, localContextScope, -1, -1);
    }

    @Override
    public Object doEvaluate(
      Map<String, Object> argMap,
      Map<String, Object> kwargMap,
      List<Object> varArgs
    ) {
      invocationCount++;
      return super.doEvaluate(argMap, kwargMap, varArgs);
    }

    int getInvocationCount() {
      return invocationCount;
    }
  }

  @Test
  public void itDoesNotExecuteMacrosInValidatedBlocks() {
    TextNode textNode = new TextNode(
      new TextToken("hello", 1, 1, new DefaultTokenScannerSymbols())
    );
    InstrumentedMacroFunction macro = new InstrumentedMacroFunction(
      ImmutableList.of(textNode),
      "hello",
      new LinkedHashMap<>(),
      false,
      interpreter.getContext()
    );
    interpreter.getContext().addGlobalMacro(macro);

    String template =
      "{{ hello() }}" + "{% if false %} " + "  {{ hello() }}" + "{% endif %}";

    assertThat(interpreter.getErrors()).isEmpty();
    assertThat(interpreter.render(template).trim()).isEqualTo("hello");
    assertThat(macro.getInvocationCount()).isEqualTo(1);

    assertThat(validatingInterpreter.render(template).trim()).isEqualTo("hello");
    assertThat(macro.getInvocationCount()).isEqualTo(3);
    assertThat(validatingInterpreter.getErrors()).isEmpty();
  }

  @Test
  public void itDoesNotExecuteFunctionsInValidatedBlocks() {
    functionExecutionCount = 0;

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

    result = validatingInterpreter.render(template);

    assertThat(validatingInterpreter.getErrors().size()).isEqualTo(1);
    assertThat(validatingInterpreter.getErrors().get(0).getMessage()).contains("hey(");
    assertThat(result).isEqualTo("2");
    assertThat(functionExecutionCount).isEqualTo(2);
  }

  @Test
  public void itDoesNotExecuteFiltersInValidatedBlocks() {
    assertThat(validationFilter.getExecutionCount()).isEqualTo(0);

    String template =
      "{{ 10|validation_filter() }}" +
      "{% if false %}" +
      "  {{ 10|validation_filter() }}" +
      "  {{ hey( }}" +
      "{% endif %}";

    String result = interpreter.render(template).trim();
    assertThat(interpreter.getErrors()).isEmpty();
    assertThat(result).isEqualTo("10");
    assertThat(validationFilter.getExecutionCount()).isEqualTo(1);

    JinjavaInterpreter.pushCurrent(validatingInterpreter);
    result = validatingInterpreter.render(template).trim();

    assertThat(validatingInterpreter.getErrors().size()).isEqualTo(1);
    assertThat(validatingInterpreter.getErrors().get(0).getMessage()).contains("hey(");
    assertThat(result).isEqualTo("10");
    assertThat(validationFilter.getExecutionCount()).isEqualTo(2);
    JinjavaInterpreter.popCurrent();
  }
}
