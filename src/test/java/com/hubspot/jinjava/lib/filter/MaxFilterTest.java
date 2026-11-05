/**********************************************************************
 * Copyright (c) 2026 HubSpot Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************/
package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.el.ext.AllowlistMethodValidator;
import com.hubspot.jinjava.el.ext.AllowlistReturnTypeValidator;
import com.hubspot.jinjava.el.ext.MethodValidatorConfig;
import com.hubspot.jinjava.el.ext.ReturnTypeValidatorConfig;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.DisabledException;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.mode.EagerExecutionMode;
import com.hubspot.jinjava.testobjects.PartiallyDeferredValueTestObjects.GoodPyishSerializable;
import com.hubspot.jinjava.testobjects.SortFilterTestObjects.MyBar;
import com.hubspot.jinjava.testobjects.SortFilterTestObjects.MyFoo;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class MaxFilterTest extends BaseJinjavaTest {

  private final MaxFilter filter = new MaxFilter();

  @Test
  public void itReturnsTheMaximum() {
    assertRender("{{ [1, 2, 3]|max }}", "3");
    assertRender("{{ [-5, -1, -3]|max }}", "-1");
    assertRender("{{ [7]|max }}", "7");
  }

  @Test
  public void itComparesMixedNumbersWithoutReplacingTheOriginalItem() {
    BigDecimal largest = new BigDecimal("4.5");
    assertThat(max(List.of(1, 2L, 3.5D, largest), Map.of())).isSameAs(largest);
    assertThat(max(List.of(1, 2L, 3.5D), Map.of())).isEqualTo(3.5D);
  }

  @Test
  public void itComparesLargeNumbers() {
    BigInteger largest = new BigInteger("123456789012345678901234567890");
    assertThat(max(List.of(largest.subtract(BigInteger.ONE), largest), Map.of()))
      .isSameAs(largest);
    BigDecimal decimal = new BigDecimal("123456789012345678901234567890.1");
    assertThat(max(List.of(largest, decimal), Map.of())).isSameAs(decimal);
    assertThat(max(List.of(Long.MAX_VALUE - 1, Long.MAX_VALUE), Map.of()))
      .isEqualTo(Long.MAX_VALUE);
  }

  @Test
  public void itSupportsCaseSensitivityAndNamedArguments() {
    assertRender("{{ ['Z', 'a']|max }}", "Z");
    assertRender("{{ ['Z', 'a']|max(true) }}", "a");
    assertRender("{{ ['Z', 'a']|max(case_sensitive=true) }}", "a");
    assertRender("{{ ['Z', 'a']|max(case_sensitive=false) }}", "Z");
  }

  @Test
  public void itPreservesTheFirstItemForEqualKeys() {
    Map<String, Object> first = Map.of("name", "Z", "id", 1);
    Map<String, Object> second = Map.of("name", "z", "id", 2);
    assertThat(max(List.of(first, second), Map.of("attribute", "name"))).isSameAs(first);
    Integer firstNumber = 2;
    assertThat(max(List.of(firstNumber, 2L), Map.of())).isSameAs(firstNumber);
  }

  @Test
  public void itSelectsOriginalObjectsByAttribute() {
    Map<String, Object> first = Map.of("score", 1);
    Map<String, Object> second = Map.of("score", 3);
    assertThat(max(List.of(first, second), Map.of("attribute", "score")))
      .isSameAs(second);
    assertRender("{{ ([{'name':'Z'}, {'name':'a'}]|max(true, 'name')).name }}", "a");
    assertRender("{{ ([{'name':'Z'}, {'name':'a'}]|max(attribute='name')).name }}", "Z");
  }

  @Test
  public void itResolvesNestedAttributesAndListIndices() {
    assertRender(
      "{{ ([{'a':{'b':2} }, {'a':{'b':4} }]|max(attribute='a.b')).a.b }}",
      "4"
    );
    assertRender("{{ ([['a', 3], ['b', 2]]|max(attribute=1))[0] }}", "a");
    assertRender("{{ ([{'a':[1]}, {'a':[3]}]|max(attribute='a.0')).a[0] }}", "3");
    assertRender("{{ [1, 2]|max(attribute=null) }}", "2");
  }

  @Test
  public void itResolvesAllowedBeanAttributes() {
    MyBar first = new MyBar(new MyFoo(new Date(1)));
    MyBar second = new MyBar(new MyFoo(new Date(2)));
    assertThat(max(List.of(first, second), Map.of("attribute", "foo.date")))
      .isSameAs(second);
  }

  @Test
  public void itPreservesMethodRestrictions() {
    Jinjava restricted = new Jinjava(
      newConfigBuilder()
        .withMethodValidator(
          AllowlistMethodValidator.create(MethodValidatorConfig.builder().build())
        )
        .build()
    );
    JinjavaInterpreter interpreter = restricted.newInterpreter();
    assertThatThrownBy(() ->
        filter.filter(
          List.of(new MyFoo(new Date(1))),
          interpreter,
          Map.of("attribute", "date")
        )
      )
      .isInstanceOf(InvalidArgumentException.class);
  }

  @Test
  public void itPreservesReturnTypeRestrictions() {
    Jinjava restricted = new Jinjava(
      newConfigBuilder()
        .withReturnTypeValidator(
          AllowlistReturnTypeValidator.create(
            ReturnTypeValidatorConfig
              .builder()
              .addDefaultAllowlistGroups()
              .addAllowedCanonicalClassPrefixes("com.hubspot.jinjava.testobjects.")
              .build(),
            value -> value instanceof MyFoo ? null : value
          )
        )
        .build()
    );
    assertThatThrownBy(() ->
        filter.filter(
          List.of(Map.of("score", new MyFoo(new Date(1)))),
          restricted.newInterpreter(),
          Map.of("attribute", "score")
        )
      )
      .isInstanceOf(InvalidArgumentException.class);
  }

  @Test
  public void itReturnsNullForEmptyInputs() {
    for (Object value : Arrays.asList(
      null,
      List.of(),
      Map.of(),
      new Object[0],
      List.of().iterator(),
      ""
    )) {
      assertThat(max(value, Map.of())).isNull();
    }
    assertRender("{{ []|max|default('empty') }}", "empty");
    assertRender("{{ missing|max|default('empty') }}", "empty");
  }

  @Test
  public void itRejectsNullItemsAndMissingAttributes() {
    assertThatThrownBy(() -> max(Arrays.asList(1, null), Map.of()))
      .isInstanceOf(InvalidInputException.class)
      .hasMessageContaining("null item");
    assertThatThrownBy(() ->
        max(List.of(Map.of("score", 1), Map.of()), Map.of("attribute", "score"))
      )
      .isInstanceOf(InvalidArgumentException.class)
      .hasMessageContaining("attribute");
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("score", null);
    assertThatThrownBy(() -> max(List.of(item), Map.of("attribute", "score")))
      .isInstanceOf(InvalidArgumentException.class);
  }

  @Test
  public void itRejectsIncomparableItems() {
    assertThatThrownBy(() -> max(List.of(new Object(), new Object()), Map.of()))
      .isInstanceOf(InvalidInputException.class)
      .hasMessageContaining("comparable");
    assertThatThrownBy(() -> max(List.of(1, "not a number"), Map.of()))
      .isInstanceOf(InvalidInputException.class)
      .hasMessageContaining("max");
  }

  @Test
  public void itRejectsInvalidArguments() {
    for (String template : List.of(
      "{{ [1]|max(unknown=true) }}",
      "{{ [1]|max(false, null, 1) }}"
    )) {
      assertThat(jinjava.renderForResult(template, Map.of()).getErrors()).isNotEmpty();
    }
  }

  @Test
  public void itHandlesArraysAndDoesNotModifyCollections() {
    List<Integer> values = new ArrayList<>(List.of(3, 1, 2));
    assertThat(max(values, Map.of())).isEqualTo(3);
    assertThat(values).containsExactly(3, 1, 2);
    Object[] array = { 3, 1, 2 };
    assertThat(max(array, Map.of())).isEqualTo(3);
    assertThat(array).containsExactly(3, 1, 2);
  }

  @Test
  public void itConsumesAnIterableOnlyOnce() {
    AtomicInteger iterations = new AtomicInteger();
    AtomicInteger reads = new AtomicInteger();
    Iterable<Integer> input = () -> {
      assertThat(iterations.incrementAndGet()).isEqualTo(1);
      Iterator<Integer> delegate = List.of(2, 3, 1).iterator();
      return new Iterator<Integer>() {
        public boolean hasNext() {
          return delegate.hasNext();
        }

        public Integer next() {
          reads.incrementAndGet();
          return delegate.next();
        }
      };
    };
    assertThat(max(input, Map.of())).isEqualTo(3);
    assertThat(reads.get()).isEqualTo(3);
    assertThat(max(List.of(2, 3, 1).iterator(), Map.of())).isEqualTo(3);
  }

  @Test
  public void itUsesTheConfiguredMapIterationBehavior() {
    Map<String, Integer> values = Map.of("a", 3, "z", 1);
    for (boolean keys : List.of(false, true)) {
      JinjavaInterpreter interpreter = new Jinjava(
        newConfigBuilder()
          .withLegacyOverrides(
            LegacyOverrides.newBuilder().withIterateOverMapKeys(keys).build()
          )
          .build()
      )
        .newInterpreter();
      JinjavaInterpreter.pushCurrent(interpreter);
      try {
        assertThat(filter.filter(values, interpreter, Map.of()))
          .isEqualTo(keys ? "z" : 3);
      } finally {
        JinjavaInterpreter.popCurrent();
      }
    }
  }

  @Test
  public void itIteratesStringsUsingUtf16Characters() {
    assertRender("{{ 'abc'|max }}", "c");
    assertRender("{{ 'Za'|max(true) }}", "a");
    assertThat(max("a\uD83D\uDE00", Map.of())).isEqualTo("\uDE00");
  }

  @Test
  public void itHonorsRegistrationDisablingAndOverrides() {
    assertThat(new FilterLibrary(true, Set.of()).getFilter("max"))
      .isInstanceOf(MaxFilter.class);
    assertThatThrownBy(() -> new FilterLibrary(true, Set.of("max")).getFilter("max"))
      .isInstanceOf(DisabledException.class);
    jinjava.getGlobalContext().registerFilter(new CustomMaxFilter());
    assertRender("{{ [1, 2]|max }}", "custom");
  }

  @Test
  public void itPreservesDeferredInputsAndElements() {
    assertThatThrownBy(() -> max(DeferredValue.instance(), Map.of()))
      .isInstanceOf(DeferredValueException.class);
    assertThatThrownBy(() -> max(List.of(1, DeferredValue.instance()), Map.of()))
      .isInstanceOf(DeferredValueException.class);
    assertThatThrownBy(() ->
        max(
          List.of(Map.of("score", DeferredValue.instance())),
          Map.of("attribute", "score")
        )
      )
      .isInstanceOf(DeferredValueException.class);
  }

  @Test
  public void itCanSelectResolvedAttributesOfPartiallyDeferredObjects() {
    GoodPyishSerializable item = new GoodPyishSerializable();
    assertThat(max(List.of(item), Map.of("attribute", "resolved"))).isSameAs(item);
    assertThatThrownBy(() -> max(List.of(item), Map.of("attribute", "deferred")))
      .hasRootCauseInstanceOf(DeferredValueException.class);
  }

  @Test
  public void itWorksInNormalOptimizedAndEagerFilterChains() {
    for (boolean optimized : List.of(false, true)) {
      Jinjava engine = new Jinjava(
        newConfigBuilder().withEnableFilterChainOptimization(optimized).build()
      );
      RenderResult result = engine.renderForResult(
        "{{ [1, 3, 2]|max|string|upper }}",
        Map.of()
      );
      assertThat(result.getOutput()).isEqualTo("3");
      assertThat(result.getErrors()).isEmpty();
    }
    RenderResult eager = eagerEngine()
      .renderForResult("{{ [1, 3, 2]|max|string|upper }}", Map.of());
    assertThat(eager.getOutput()).isEqualTo("3");
    assertThat(eager.getErrors()).isEmpty();
  }

  @Test
  public void itResolvesDeferredCollectionsAndArgumentsOnTheSecondPass() {
    for (String template : List.of(
      "{{ values|max }}",
      "{{ values|max(case_sensitive=setting) }}",
      "{{ values|max(attribute=attr) }}"
    )) {
      Map<String, Object> deferred = Map.of(
        "values",
        DeferredValue.instance(),
        "setting",
        DeferredValue.instance(),
        "attr",
        DeferredValue.instance()
      );
      Map<String, Object> resolved = template.contains("attribute")
        ? Map.of(
          "values",
          List.of(Map.of("score", 1), Map.of("score", 3)),
          "attr",
          "score"
        )
        : Map.of("values", List.of("Z", "a"), "setting", true);
      assertSecondPass(template, deferred, resolved);
    }
  }

  @Test
  public void itResolvesDeferredAttributesOnTheSecondPass() {
    String template =
      "{{ ([{'name':'a', 'score':score}, {'name':'b', 'score':2}]|max(attribute='score')).name }}";
    assertSecondPass(
      template,
      Map.of("score", DeferredValue.instance()),
      Map.of("score", 3)
    );
  }

  @Test
  public void itDefersArgumentsWithAnAlreadyResolvedCollection() {
    assertSecondPass(
      "{{ values|max(case_sensitive=setting) }}",
      Map.of("values", List.of("Z", "a"), "setting", DeferredValue.instance()),
      Map.of("values", List.of("Z", "a"), "setting", true)
    );
    List<Map<String, Integer>> values = List.of(Map.of("score", 1), Map.of("score", 3));
    assertSecondPass(
      "{{ (values|max(attribute=attr)).score }}",
      Map.of("values", values, "attr", DeferredValue.instance()),
      Map.of("values", values, "attr", "score")
    );
  }

  @Test
  public void itReportsInvalidTemplateValues() {
    for (String template : List.of(
      "{{ [1, null]|max }}",
      "{{ [{'a': 1}, {}]|max(attribute='a') }}",
      "{{ [1, 'bad']|max }}"
    )) {
      RenderResult result = jinjava.renderForResult(template, Map.of());
      assertThat(result.getErrors()).isNotEmpty();
      assertThat(result.getErrors().get(0).getMessage()).contains("max");
    }
  }

  @Test
  public void itPreservesOutputLimitsAndEscaping() {
    Jinjava limited = new Jinjava(newConfigBuilder().withMaxOutputSize(3).build());
    assertThat(limited.renderForResult("{{ ['abc']|max }}", Map.of()).getOutput())
      .isEqualTo("abc");
    assertThat(limited.renderForResult("{{ ['abcd']|max }}", Map.of()).getErrors())
      .isNotEmpty();
    assertRender(
      "{% autoescape true %}{{ ['<b>']|max }}{% endautoescape %}",
      "&lt;b&gt;"
    );
  }

  public static class CustomMaxFilter implements Filter {

    @Override
    public String getName() {
      return "max";
    }

    @Override
    public Object filter(Object value, JinjavaInterpreter interpreter, String... args) {
      return "custom";
    }
  }

  private Object max(Object input, Map<String, Object> args) {
    JinjavaInterpreter interpreter = jinjava.newInterpreter();
    JinjavaInterpreter.pushCurrent(interpreter);
    try {
      return filter.filter(input, interpreter, args);
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  private void assertRender(String template, String expected) {
    RenderResult result = jinjava.renderForResult(template, Map.of());
    assertThat(result.getOutput()).isEqualTo(expected);
    assertThat(result.getErrors()).isEmpty();
  }

  private Jinjava eagerEngine() {
    return new Jinjava(
      newConfigBuilder()
        .withExecutionMode(EagerExecutionMode.instance())
        .withLegacyOverrides(
          LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
        )
        .build()
    );
  }

  private void assertSecondPass(
    String template,
    Map<String, Object> deferred,
    Map<String, Object> resolved
  ) {
    RenderResult first = eagerEngine().renderForResult(template, deferred);
    assertThat(first.getErrors()).isEmpty();
    assertThat(first.getOutput()).contains("max");
    RenderResult second = jinjava.renderForResult(first.getOutput(), resolved);
    RenderResult direct = jinjava.renderForResult(template, resolved);
    assertThat(second.getOutput()).isEqualTo(direct.getOutput());
    assertThat(second.getErrors()).isEmpty();
    assertThat(direct.getErrors()).isEmpty();
  }
}
