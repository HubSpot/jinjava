package com.hubspot.jinjava.el.ext.eager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class EagerAstMethodTest extends BaseInterpretingTest {

  @Before
  public void setup() {
    JinjavaConfig config = JinjavaConfig
      .newBuilder()
      .withRandomNumberGeneratorStrategy(RandomNumberGeneratorStrategy.DEFERRED)
      .withExecutionMode(EagerExecutionMode.instance())
      .withNestedInterpretationEnabled(true)
      .withLegacyOverrides(
        LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
      )
      .withMaxMacroRecursionDepth(5)
      .withEnableRecursiveMacroCalls(true)
      .build();
    JinjavaInterpreter parentInterpreter = new JinjavaInterpreter(
      jinjava,
      new Context(),
      config
    );
    interpreter = new JinjavaInterpreter(parentInterpreter);

    interpreter.getContext().put("deferred", DeferredValue.instance());
    interpreter.getContext().put("foo", "bar");
    List<Object> fooList = new ArrayList<>();
    fooList.add("val");
    interpreter.getContext().put("foo_list", new PyList(fooList));
    Map<String, Object> fooMap = new HashMap<>();
    fooMap.put("foo_list", fooList);
    interpreter.getContext().put("foo_map", new PyMap(fooMap));
    interpreter.getContext().put("list_name", "foo_list");
    interpreter.getContext().put("map_name", "foo_map");
    Map<String, Object> barMap = new HashMap<>();
    barMap.put("foo_map", interpreter.getContext().get("foo_map"));
    interpreter.getContext().put("bar_map", new PyMap(barMap));
  }

  @Test
  public void itPreservesIdentifier() {
    try {
      interpreter.resolveELExpression("foo_list.append(deferred)", -1);
      fail("Should throw DeferredParsingException");
    } catch (DeferredParsingException e) {
      assertThat(e.getDeferredEvalResult()).isEqualTo("foo_list.append(deferred)");
    }
  }

  @Test
  public void itPreservesAstDot() {
    try {
      interpreter.resolveELExpression("foo_map.foo_list.append(deferred)", -1);
      fail("Should throw DeferredParsingException");
    } catch (DeferredParsingException e) {
      assertThat(e.getDeferredEvalResult())
        .isEqualTo("foo_map.foo_list.append(deferred)");
    }
  }

  @Test
  public void itPreservesDoubleAstDot() {
    try {
      interpreter.resolveELExpression("bar_map.foo_map.foo_list.append(deferred)", -1);
      fail("Should throw DeferredParsingException");
    } catch (DeferredParsingException e) {
      assertThat(e.getDeferredEvalResult())
        .isEqualTo("bar_map.foo_map.foo_list.append(deferred)");
    }
  }

  @Test
  public void itPreservesAstBracket() {
    try {
      interpreter.resolveELExpression("foo_map[list_name].append(deferred)", -1);
      fail("Should throw DeferredParsingException");
    } catch (DeferredParsingException e) {
      assertThat(e.getDeferredEvalResult())
        .isEqualTo("foo_map['foo_list'].append(deferred)");
    }
  }

  @Test
  public void itPreservesDoubleAstBracket() {
    try {
      interpreter.resolveELExpression(
        "bar_map[map_name][list_name].append(deferred)",
        -1
      );
      fail("Should throw DeferredParsingException");
    } catch (DeferredParsingException e) {
      assertThat(e.getDeferredEvalResult())
        .isEqualTo("bar_map['foo_map']['foo_list'].append(deferred)");
    }
  }

  @Test
  public void itPreservesAstDotThenAstBracket() {
    try {
      interpreter.resolveELExpression("bar_map.foo_map[list_name].append(deferred)", -1);
      fail("Should throw DeferredParsingException");
    } catch (DeferredParsingException e) {
      assertThat(e.getDeferredEvalResult())
        .isEqualTo("bar_map.foo_map['foo_list'].append(deferred)");
    }
  }

  @Test
  public void itPreservesAstBracketThenAstDot() {
    try {
      interpreter.resolveELExpression("bar_map[map_name].foo_list.append(deferred)", -1);
      fail("Should throw DeferredParsingException");
    } catch (DeferredParsingException e) {
      assertThat(e.getDeferredEvalResult())
        .isEqualTo("bar_map['foo_map'].foo_list.append(deferred)");
    }
  }

  @Test
  public void itPreservesAstMethod() {
    try {
      interpreter.resolveELExpression("foo_map.get(list_name).append(deferred)", -1);
      fail("Should throw DeferredParsingException");
    } catch (DeferredParsingException e) {
      assertThat(e.getDeferredEvalResult())
        .isEqualTo("foo_map.get('foo_list').append(deferred)");
    }
  }

  @Test
  public void itPreservesAstChoice() {
    try {
      interpreter.resolveELExpression("(deferred ? [] : foo_list).append(deferred)", -1);
      fail("Should throw DeferredParsingException");
    } catch (DeferredParsingException e) {
      assertThat(e.getDeferredEvalResult())
        .isEqualTo("(deferred ? [] : foo_list).append(deferred)");
    }
  }

  @Test
  public void itPreservesAstList() {
    try {
      interpreter.resolveELExpression("[foo_list][0].append(deferred)", -1);
      fail("Should throw DeferredParsingException");
    } catch (DeferredParsingException e) {
      assertThat(e.getDeferredEvalResult()).isEqualTo("[foo_list][0].append(deferred)");
    }
  }
}
