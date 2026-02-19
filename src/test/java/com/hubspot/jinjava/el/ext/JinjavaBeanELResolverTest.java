package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.el.JinjavaELContext;
import com.hubspot.jinjava.interpret.AutoCloseableSupplier;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.testobjects.JinjavaBeanELResolverTestObjects;
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
    jinjava = new Jinjava(BaseJinjavaTest.newConfigBuilder().build());
  }

  @Test
  public void itInvokesProperStringReplace() {
    try (
      var a = JinjavaInterpreter.closeablePushCurrent(jinjava.newInterpreter()).get()
    ) {
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
  }

  @Test
  public void itInvokesBestMethodWithSingleParam() {
    try (
      var a = JinjavaInterpreter.closeablePushCurrent(jinjava.newInterpreter()).get()
    ) {
      JinjavaBeanELResolverTestObjects.TempItInvokesBestMethodWithSingleParam var =
        new JinjavaBeanELResolverTestObjects.TempItInvokesBestMethodWithSingleParam();
      assertThat(
        jinjavaBeanELResolver.invoke(
          elContext,
          var,
          "getResult",
          null,
          new Object[] { 1 }
        )
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
  }

  @Test
  public void itPrefersPrimitives() {
    try (
      var a = JinjavaInterpreter.closeablePushCurrent(jinjava.newInterpreter()).get()
    ) {
      JinjavaBeanELResolverTestObjects.TempItPrefersPrimitives var =
        new JinjavaBeanELResolverTestObjects.TempItPrefersPrimitives();
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
  }

  @Test
  public void itDoesNotAllowAccessingPropertiesOfInterpreter() {
    try (
      AutoCloseableSupplier.AutoCloseableImpl<JinjavaInterpreter> a = JinjavaInterpreter
        .closeablePushCurrent(jinjava.newInterpreter())
        .get()
    ) {
      assertThat(jinjavaBeanELResolver.getValue(elContext, a.value(), "config")).isNull();
    }
  }
}
