package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class AdvancedFilterTest {

  Jinjava jinjava;

  @Test
  public void testOnlyArgs() {
    jinjava = new Jinjava();

    Object[] expectedArgs = new Object[] {3L, 1L};
    Map<String, Object> expectedKwargs = new HashMap<>();

    jinjava.getGlobalContext().registerFilter(new MyMirrorFilter(expectedArgs, expectedKwargs));

    assertThat(jinjava.render("{{ 'test'|mirror(3, 1) }}", new HashMap<>())).isEqualTo("test");
  }

  @Test
  public void testOnlyKwargs() {
    jinjava = new Jinjava();

    Object[] expectedArgs = new Object[] {};
    Map<String, Object> expectedKwargs = new HashMap<String, Object>() {{
      put("named10", "str");
      put("named2", 3L);
      put("namedB", true);
    }};

    jinjava.getGlobalContext().registerFilter(new MyMirrorFilter(expectedArgs, expectedKwargs));

    assertThat(jinjava.render("{{ 'test'|mirror(named2=3, named10='str', namedB=true) }}", new HashMap<>())).isEqualTo("test");
  }

  @Test
  public void itTestsNullKwargs() {
    jinjava = new Jinjava();

    Object[] expectedArgs = new Object[] {};
    Map<String, Object> expectedKwargs = new HashMap<String, Object>() {{
      put("named1", null);
    }};

    jinjava.getGlobalContext().registerFilter(new MyMirrorFilter(expectedArgs, expectedKwargs));

    assertThat(jinjava.render("{{ 'test'|divide(named1) }}", new HashMap<>())).isEqualTo("test");
  }

  @Test
  public void testMixedArgsAndKwargs() {
    jinjava = new Jinjava();

    Object[] expectedArgs = new Object[] {1L, 2L};
    Map<String, Object> expectedKwargs = new HashMap<String, Object>() {{
      put("named", "test");
    }};

    jinjava.getGlobalContext().registerFilter(new MyMirrorFilter(expectedArgs, expectedKwargs));

    assertThat(jinjava.render("{{ 'test'|mirror(1, 2, named='test') }}", new HashMap<>())).isEqualTo("test");
  }

  @Test
  public void testUnorderedArgsAndKwargs() {
    jinjava = new Jinjava();

    Object[] expectedArgs = new Object[] {"1", 2L};
    Map<String, Object> expectedKwargs = new HashMap<String, Object>() {{
      put("named", "test");
    }};

    jinjava.getGlobalContext().registerFilter(new MyMirrorFilter(expectedArgs, expectedKwargs));

    assertThat(jinjava.render("{{ 'test'|mirror('1', named='test', 2) }}", new HashMap<>())).isEqualTo("test");
  }

  @Test
  public void testRepeatedKwargs() {
    jinjava = new Jinjava();

    Object[] expectedArgs = new Object[] {true};
    Map<String, Object> expectedKwargs = new HashMap<String, Object>() {{
      put("named", "overwrite");
    }};

    jinjava.getGlobalContext().registerFilter(new MyMirrorFilter(expectedArgs, expectedKwargs));

    assertThat(jinjava.render("{{ 'test'|mirror(true, named='test', named='overwrite') }}", new HashMap<>())).isEqualTo("test");
  }

  private static class MyMirrorFilter implements AdvancedFilter {
    private Object[] expectedArgs;
    private Map<String, Object> expectedKwargs;

    MyMirrorFilter(Object[] args, Map<String, Object> kwargs) {
      this.expectedArgs = args;
      this.expectedKwargs = kwargs;
    }

    @Override
    public String getName() {
      return "mirror";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, Object[] args, Map<String, Object> kwargs) {
      if (!Arrays.equals(expectedArgs, args)) {
        throw new RuntimeException(
            "Args are different than expected: " +
            Arrays.toString(args) +
            " to " +
            Arrays.toString(expectedArgs)
        );
      }

      if (!expectedKwargs.equals(kwargs)) {
        throw new RuntimeException(
            "Kwargs are different than expected: " +
            Arrays.toString(kwargs.entrySet().toArray()) +
            " to " +
            Arrays.toString(expectedKwargs.entrySet().toArray())
        );
      }

      return var;
    }
  }

}
