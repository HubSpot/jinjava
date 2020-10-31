package com.hubspot.jinjava.lib.filter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AbstractFilterTest extends BaseInterpretingTest {
  private ArgCapturingFilter filter;


  public static class NoJinjavaDocFilter extends ArgCapturingFilter{}

  @Test
  public void itErrorsWhenNoJinjavaDoc() {
    assertThatThrownBy(() -> new NoJinjavaDocFilter()).hasMessageContaining("@JinjavaDoc must be configured");
  }


  @JinjavaDoc
  public static class NoJinjavaParamsFilter extends ArgCapturingFilter{}

  @Test
  public void itDoesNotRequireParams() {
    filter = new NoJinjavaParamsFilter();
  }

  @JinjavaDoc(
          params = {
                  @JinjavaParam(
                          value = "1st",
                          desc = "1st"
                  ),
                  @JinjavaParam(
                          value = "2nd",
                          desc = "2nd"
                  )
                  ,
                  @JinjavaParam(
                          value = "3rd",
                          desc = "3rd"
                  )
          }
  )
  public static class TwoParamTypesFilter extends ArgCapturingFilter{}

  @Test
  public void itSupportsMixingOfPositionalAndNamedArgs() {
    filter = new TwoParamTypesFilter();

    filter.filter(null, interpreter, new Object[]{"1"}, ImmutableMap.of("3rd", "3"));


    assertThat(filter.parsedArgs).isEqualTo(ImmutableMap.of("1st", "1", "3rd", "3"));
  }

  @JinjavaDoc(
          params = {
                  @JinjavaParam(
                          value = "boolean",
                          type = "boolean",
                          desc = "boolean",
                          required = true
                  ),
                  @JinjavaParam(
                          value = "int",
                          type = "int",
                          desc = "int"
                  ),
                  @JinjavaParam(
                          value = "long",
                          type = "long",
                          desc = "long"
                  ),
                  @JinjavaParam(
                          value = "float",
                          type = "float",
                          desc = "float"
                  ),
                  @JinjavaParam(
                          value = "double",
                          type = "double",
                          desc = "double"
                  ),
                  @JinjavaParam(
                          value = "number",
                          type = "number",
                          desc = "number"
                  ),
                  @JinjavaParam(
                          value = "object",
                          type = "object",
                          desc = "object"
                  ),
                  @JinjavaParam(
                          value = "dict",
                          type = "dict",
                          desc = "dict"
                  )
          }
  )
  public static class AllParamTypesFilter extends ArgCapturingFilter{}

    @Test
    public void itParsesNumericAndBooleanInput() {
        filter = new AllParamTypesFilter();

        Map<String, Object> kwArgs = new HashMap<>();
        kwArgs.put("boolean", "true");
        kwArgs.put("int", "1");
        kwArgs.put("long", "2");
        kwArgs.put("double", "3");
        kwArgs.put("float", "4");
        kwArgs.put("number", "5");
        kwArgs.put("object", new Object());
        kwArgs.put("dict", new Object());

        filter.filter(null, interpreter, new Object[]{}, kwArgs);

        Map<String, Object> expected = new HashMap<>();
        expected.put("boolean", true);
        expected.put("int", 1);
        expected.put("long", 2L);
        expected.put("double", 3.0);
        expected.put("float", 4.0f);
        expected.put("number", new BigDecimal(5));
        expected.put("object", kwArgs.get("object"));
        expected.put("dict", kwArgs.get("dict"));

        assertThat(filter.parsedArgs).isEqualTo(expected);
    }

    @Test
    public void itValidatesRequiredArgs() {
        filter = new AllParamTypesFilter();

        assertThatThrownBy(() -> filter.filter(null, interpreter, new Object[]{}, Collections.emptyMap())).hasMessageContaining("Argument named 'boolean' is required");

    }

    @Test
    public void itErrorsOnTooManyArgs() {
        filter = new AllParamTypesFilter();

        assertThatThrownBy(() -> filter.filter(null, interpreter, new Object[]{true, null, null, null, null, null, null, null, null},
                                               Collections.emptyMap())).hasMessageContaining("Argument at index").hasMessageContaining("is invalid");
    }

    @Test
    public void itErrorsUnknownNamedArg() {
        filter = new AllParamTypesFilter();

        assertThatThrownBy(() -> filter.filter(null, interpreter, new Object[]{true},
                                               ImmutableMap.of("unknown", "unknown"))).hasMessageContaining("Argument named 'unknown' is invalid");
    }

  public static class ArgCapturingFilter extends AbstractFilter {
    public Map<String, Object> parsedArgs;

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, Map<String, Object> parsedArgs) {
      this.parsedArgs = parsedArgs;
      return null;
    }

    @Override public String getName() {
      return getClass().getSimpleName();
    }
  }


}
