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

  public static class OtherClass {
    private String state;

    public OtherClass(String state) {
      this.state = state;
    }

    public String prependState(String in) {
      return state + in;
    }
  }

  @Test
  public void testDefineProxy() throws Exception {
    Method m = MyClass.class.getDeclaredMethod("concatState", String.class);
    MyClass instance = new MyClass("bar");

    ELFunctionDefinition proxy = InjectedContextFunctionProxy.defineProxy(
      "ns",
      "fooproxy",
      m,
      instance
    );
    assertThat(proxy.getName()).isEqualTo("ns:fooproxy");
    assertThat(proxy.getMethod().getDeclaringClass().getName())
      .isEqualTo(
        MyClass.class.getName() +
        "$$" +
        InjectedContextFunctionProxy.class.getSimpleName() +
        "$$ns$$fooproxy"
      );

    assertThat(proxy.getMethod().invoke(null, "foo")).isEqualTo("foobar");
  }

  @Test
  public void testDefineMultipleProxies() throws Exception {
    Method concat = MyClass.class.getDeclaredMethod("concatState", String.class);
    MyClass myClassInstance = new MyClass("bar");

    ELFunctionDefinition myClassProxy = InjectedContextFunctionProxy.defineProxy(
      "ns",
      "fooproxy",
      concat,
      myClassInstance
    );
    Method prepend = OtherClass.class.getDeclaredMethod("prependState", String.class);
    OtherClass otherClassInstance = new OtherClass("bar");

    ELFunctionDefinition otherClassProxy = InjectedContextFunctionProxy.defineProxy(
      "ns",
      "fooproxy",
      prepend,
      otherClassInstance
    );
    assertThat(myClassProxy.getName()).isEqualTo("ns:fooproxy");
    assertThat(myClassProxy.getMethod().getDeclaringClass().getName())
      .isEqualTo(
        MyClass.class.getName() +
        "$$" +
        InjectedContextFunctionProxy.class.getSimpleName() +
        "$$ns$$fooproxy"
      );

    assertThat(myClassProxy.getMethod().invoke(null, "foo")).isEqualTo("foobar");
    assertThat(otherClassProxy.getName()).isEqualTo("ns:fooproxy");
    assertThat(otherClassProxy.getMethod().getDeclaringClass().getName())
      .isEqualTo(
        OtherClass.class.getName() +
        "$$" +
        InjectedContextFunctionProxy.class.getSimpleName() +
        "$$ns$$fooproxy"
      );

    assertThat(otherClassProxy.getMethod().invoke(null, "foo")).isEqualTo("barfoo");
  }
}
