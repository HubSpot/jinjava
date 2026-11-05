/**
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
 */
package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.el.TruthyTypeConverter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.objects.DummyObject;
import de.odysseus.el.misc.TypeConverter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class StringConcatOperatorTest extends BaseJinjavaTest {

  @Test
  public void itConcatenatesMapsWithUndefinedValues() {
    assertThat(jinjava.render("{{ {'g':'v'} ~ length }}", Collections.emptyMap()))
      .isEqualTo("{'g': 'v'}");
  }

  @Test
  public void itConcatenatesMapsOnEitherSide() {
    assertRender("{{ {'g': 'v'} ~ '!' }}", "{'g': 'v'}!");
    assertRender("{{ '!' ~ {'g': 'v'} }}", "!{'g': 'v'}");
    assertRender("{{ {'g': 'v'} ~ {'h': 'w'} }}", "{'g': 'v'}{'h': 'w'}");
    assertRender("{{ {} ~ '!' }}", "{}!");
  }

  @Test
  public void itFormatsNestedContextMapsLikeDirectOutput() {
    Map<String, Object> context = Collections.singletonMap(
      "value",
      Collections.singletonMap(
        "outer",
        Collections.singletonMap("inner", List.of("a", "b"))
      )
    );
    String directOutput = jinjava.render("{{ value }}", context);
    RenderResult result = jinjava.renderForResult("{{ value ~ '!' }}", context);
    assertThat(result.getOutput()).isEqualTo(directOutput + "!");
    assertThat(result.getErrors()).isEmpty();
  }

  @Test
  public void itPreservesLegacyMapFormatting() {
    jinjava =
      new Jinjava(newConfigBuilder().withLegacyOverrides(LegacyOverrides.NONE).build());
    assertRender("{{ {'g': 'v'} ~ '!' }}", "{g=v}!");
    assertRender("{{ '!' ~ {'g': 'v'} }}", "!{g=v}");
  }

  @Test
  public void itPreservesNonMapFormatting() {
    assertRender("{{ 'a' ~ 42 ~ true }}", "a42true");
    assertRender("{{ ['a', 'b'] ~ [1, 2] }}", "[a, b][1, 2]");
    assertThat(
      jinjava.render(
        "{{ nullValue ~ 'a' ~ nullValue }}",
        Collections.singletonMap("nullValue", null)
      )
    )
      .isEqualTo("a");
    assertThat(jinjava.render("{{ missing ~ 'a' ~ missing }}", Collections.emptyMap()))
      .isEqualTo("a");
  }

  @Test
  public void itPreservesDummyObjectFormatting() {
    try (
      var a = JinjavaInterpreter.closeablePushCurrent(jinjava.newInterpreter()).get()
    ) {
      TypeConverter converter = new TruthyTypeConverter();
      assertThat(StringConcatOperator.OP.apply(converter, new DummyObject(), "a"))
        .isEqualTo("a");
      assertThat(StringConcatOperator.OP.apply(converter, "a", new DummyObject()))
        .isEqualTo("a");
    }
  }

  @Test
  public void itUsesTheConverterWithoutAnInterpreter() {
    assertThat(JinjavaInterpreter.getCurrent()).isNull();
    assertThat(
      StringConcatOperator.OP.apply(
        new TruthyTypeConverter(),
        Collections.singletonMap("g", "v"),
        "!"
      )
    )
      .isEqualTo("{g=v}!");
  }

  @Test
  public void itPreservesCustomConversionInLegacyMode() {
    jinjava =
      new Jinjava(newConfigBuilder().withLegacyOverrides(LegacyOverrides.NONE).build());
    Map<String, String> value = Collections.singletonMap("g", "v");
    TypeConverter converter = mock(TypeConverter.class);
    when(converter.convert(value, String.class)).thenReturn("custom");
    when(converter.convert("!", String.class)).thenReturn("!");

    try (
      var a = JinjavaInterpreter.closeablePushCurrent(jinjava.newInterpreter()).get()
    ) {
      assertThat(StringConcatOperator.OP.apply(converter, value, "!"))
        .isEqualTo("custom!");
    }
  }

  @Test
  public void itLimitsTheCombinedOutput() {
    jinjava = new Jinjava(newConfigBuilder().withMaxOutputSize(12).build());
    Map<String, String> value = Collections.singletonMap("g", "v");
    TypeConverter converter = new TruthyTypeConverter();

    try (
      var a = JinjavaInterpreter.closeablePushCurrent(jinjava.newInterpreter()).get()
    ) {
      assertThat(StringConcatOperator.OP.apply(converter, value, "ab"))
        .isEqualTo("{'g': 'v'}ab");
      assertThatThrownBy(() -> StringConcatOperator.OP.apply(converter, value, "abc"))
        .isInstanceOf(OutputTooBigException.class);
    }
  }

  @Test
  public void itLimitsMapSerialization() {
    jinjava = new Jinjava(newConfigBuilder().withMaxOutputSize(12).build());
    try (
      var a = JinjavaInterpreter.closeablePushCurrent(jinjava.newInterpreter()).get()
    ) {
      assertThatThrownBy(() ->
          StringConcatOperator.OP.apply(
            new TruthyTypeConverter(),
            Collections.singletonMap("g", "abcdefghijk"),
            ""
          )
        )
        .isInstanceOf(OutputTooBigException.class);
    }
  }

  private void assertRender(String template, String expected) {
    RenderResult result = jinjava.renderForResult(template, Collections.emptyMap());
    assertThat(result.getOutput()).isEqualTo(expected);
    assertThat(result.getErrors()).isEmpty();
  }
}
