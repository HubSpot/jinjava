package com.hubspot.jinjava.lib.fn;

import static com.hubspot.jinjava.interpret.JinjavaInterpreter.pushCurrent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.Arrays;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RandomIntFunctionTest {
  private Jinjava jinjava;

  @Before
  public void beforeEach() {
    jinjava = new Jinjava(JinjavaConfig.newBuilder().build());
    pushCurrent(new JinjavaInterpreter(jinjava.newInterpreter()));
  }

  @After
  public void afterEach() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void interpreterInstanceIsMandatory() {
    JinjavaInterpreter.popCurrent();
    assertThatThrownBy(Functions::randomInt).isInstanceOf(NullPointerException.class);
  }

  @Test
  public void itGeneratesRandomIntWithBound() {
    int bound = 100;
    assertThat(Functions.randomInt(bound)).isBetween(0, bound);
  }

  @Test
  public void itGeneratesRandomInt() {
    assertThat(Functions.randomInt()).isBetween(0, Integer.MAX_VALUE);
  }

  @Test
  public void jinJavaRendersRandomIntFunctionWithBound() {
    int bound = 100;
    assertThat(
        Integer.valueOf(
          jinjava.render("{{ random_int(" + bound + ") }}", Collections.emptyMap())
        )
      )
      .isBetween(0, bound);
  }

  @Test
  public void jinJavaRendersRandomIntFunction() {
    assertThat(
        Integer.valueOf(jinjava.render("{{ random_int() }}", Collections.emptyMap()))
      )
      .isBetween(0, Integer.MAX_VALUE);
  }
}
