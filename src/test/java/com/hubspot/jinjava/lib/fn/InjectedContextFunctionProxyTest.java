package com.hubspot.jinjava.lib.fn;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.Test;

public class InjectedContextFunctionProxyTest {

  public static class MyClass {
    private String state;

    public MyClass(String state) {
      this.state = state;
    }

    public String concatState(String in) {
      return in + state;
    }
  }

  @Test
  public void testDefineProxy() throws Exception {
    Method m = MyClass.class.getDeclaredMethod("concatState", String.class);
    MyClass instance = new MyClass("bar");

    ELFunctionDefinition proxy = InjectedContextFunctionProxy.defineProxy("ns", "fooproxy", m, instance);
    assertThat(proxy.getName()).isEqualTo("ns:fooproxy");
    assertThat(proxy.getMethod().getDeclaringClass().getSimpleName()).isEqualTo(
        InjectedContextFunctionProxy.class.getSimpleName() + "$$ns$$fooproxy");

    assertThat(proxy.getMethod().invoke(null, "foo")).isEqualTo("foobar");
  }

}
