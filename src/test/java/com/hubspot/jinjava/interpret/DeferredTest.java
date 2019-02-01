package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.errorcategory.DeferredValue;

public class DeferredTest {
  private Jinjava jinjava;
  private JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    jinjava = new Jinjava();
    interpreter = jinjava.newInterpreter();
  }

  @Test
  public void test() {
    DeferredValue deferredValue = new DeferredValue();
    interpreter.getContext().put("deferred", deferredValue);
    interpreter.getContext().put("resolved", "resolvedValue");

    String output = interpreter.render("deferred");
    assertThat(output).isEqualTo("deferred");

    output = interpreter.render("hello {{deferred}} hello");
    assertThat(output).isEqualTo("hello {{deferred}} hello");
    assertThat(interpreter.getErrors()).isEmpty();

    output = interpreter.render("hello {{deferred.nested}} hello");
    assertThat(output).isEqualTo("hello {{deferred.nested}} hello");
    assertThat(interpreter.getErrors()).isEmpty();

    output = interpreter.render("hello {{deferred.nested}} hello");
    assertThat(output).isEqualTo("hello {{deferred.nested}} hello");
    assertThat(interpreter.getErrors()).isEmpty();

    output = interpreter.render("hello {{deferred}} {{resolved}} hello");
    assertThat(output).isEqualTo("hello {{deferred}} resolvedValue hello");
    assertThat(interpreter.getErrors()).isEmpty();
  }
}
