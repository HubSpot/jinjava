package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.el.JinjavaELContext;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import javax.el.ELContext;
import org.junit.Before;
import org.junit.Test;

public class JinjavaBeanELResolverTest {

  private JinjavaBeanELResolver jinjavaBeanELResolver;
  private ELContext elContext;
  private Jinjava jinjava;

  @Before
  public void setUp() throws Exception {
    jinjavaBeanELResolver = new JinjavaBeanELResolver();
    elContext = new JinjavaELContext();
    jinjava = new Jinjava();
  }

  @Test
  public void itInvokesProperStringReplace() {
    assertThat(
      jinjavaBeanELResolver.invoke(
        elContext,
        "abcd",
        "replace",
        null,
        new Object[] { "abcd", "efgh" }
      )
    )
      .isEqualTo("efgh");
    assertThat(
      jinjavaBeanELResolver.invoke(
        elContext,
        "abcd",
        "replace",
        null,
        new Object[] { 'a', 'e' }
      )
    )
      .isEqualTo("ebcd");
  }

  @Test
  public void itInvokesBestMethodWithSingleParam() {
    TempItInvokesBestMethodWithSingleParam var =
      new TempItInvokesBestMethodWithSingleParam();
    assertThat(
      jinjavaBeanELResolver.invoke(elContext, var, "getResult", null, new Object[] { 1 })
    )
      .isEqualTo("int");
    assertThat(
      jinjavaBeanELResolver.invoke(
        elContext,
        var,
        "getResult",
        null,
        new Object[] { "1" }
      )
    )
      .isEqualTo("String");
    assertThat(
      jinjavaBeanELResolver.invoke(
        elContext,
        var,
        "getResult",
        null,
        new Object[] { new Object() }
      )
    )
      .isEqualTo("Object");
  }

  @Test
  public void itPrefersPrimitives() {
    TempItPrefersPrimitives var = new TempItPrefersPrimitives();
    assertThat(
      jinjavaBeanELResolver.invoke(
        elContext,
        var,
        "getResult",
        null,
        new Object[] { 1, 2 }
      )
    )
      .isEqualTo("int Integer");
    assertThat(
      jinjavaBeanELResolver.invoke(
        elContext,
        var,
        "getResult",
        null,
        new Object[] { 1, Integer.valueOf(2) }
      )
    )
      .isEqualTo("int Integer"); // should be "int object", but we can't figure that out
    assertThat(
      jinjavaBeanELResolver.invoke(
        elContext,
        var,
        "getResult",
        null,
        new Object[] { Integer.valueOf(1), 2 }
      )
    )
      .isEqualTo("int Integer"); // should be "Number int", but we can't figure that out
  }

  @Test
  public void itDoesNotAllowAccessingPropertiesOfInterpreter() {
    JinjavaInterpreter interpreter = jinjava.newInterpreter();
    assertThat(jinjavaBeanELResolver.getValue(elContext, interpreter, "config")).isNull();
  }

  private static class TempItInvokesBestMethodWithSingleParam {

    public String getResult(int a) {
      return "int";
    }

    public String getResult(String a) {
      return "String";
    }

    public String getResult(Object a) {
      return "Object";
    }

    public String getResult(CharSequence a) {
      return "CharSequence";
    }
  }

  private static class TempItPrefersPrimitives {

    public String getResult(int a, Integer b) {
      return "int Integer";
    }

    public String getResult(int a, Object b) {
      return "int Object";
    }

    public String getResult(Number a, int b) {
      return "Number int";
    }
  }
}
