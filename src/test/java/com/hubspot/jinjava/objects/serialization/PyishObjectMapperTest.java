package com.hubspot.jinjava.objects.serialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.collections.SizeLimitingPyMap;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class PyishObjectMapperTest {

  @Test
  public void itSerializesMapWithNullKeysAsEmptyString() {
    Map<String, Object> map = new SizeLimitingPyMap(new HashMap<>(), 10);
    map.put("foo", "bar");
    map.put(null, "null");
    assertThat(PyishObjectMapper.getAsPyishString(map))
      .isEqualTo("{'': 'null', 'foo': 'bar'} ");
  }

  @Test
  public void itSerializesMapEntrySet() throws JsonProcessingException {
    SizeLimitingPyMap map = new SizeLimitingPyMap(new HashMap<>(), 10);
    map.put("foo", "bar");
    map.put("bar", ImmutableMap.of("foobar", new ArrayList<>()));
    String result = PyishObjectMapper.getAsPyishString(map.items());
    assertThat(result)
      .isEqualTo("[fn:map_entry('bar', {'foobar': []} ), fn:map_entry('foo', 'bar')]");
  }

  @Test
  public void itSerializesMapEntrySetWithLimit() throws JsonProcessingException {
    SizeLimitingPyMap map = new SizeLimitingPyMap(new HashMap<>(), 10);
    map.put("foo", "bar");
    map.put("bar", ImmutableMap.of("foobar", new ArrayList<>()));

    Jinjava jinjava = new Jinjava(
      JinjavaConfig.newBuilder().withMaxOutputSize(10000).build()
    );
    try {
      JinjavaInterpreter.pushCurrent(jinjava.newInterpreter());
      String result = PyishObjectMapper.getAsPyishString(map.items());
      assertThat(result)
        .isEqualTo("[fn:map_entry('bar', {'foobar': []} ), fn:map_entry('foo', 'bar')]");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  @Test
  public void itSerializesMapWithNullValues() {
    Map<String, Object> map = new SizeLimitingPyMap(new HashMap<>(), 10);
    map.put("foo", "bar");
    map.put("foobar", null);
    assertThat(PyishObjectMapper.getAsPyishString(map))
      .isEqualTo("{'foobar': null, 'foo': 'bar'} ");
  }

  @Test
  public void itLimitsDepth() {
    final List<Object> original = new ArrayList<>();
    List<Object> list = original;
    for (int i = 0; i < 20; i++) {
      List<Object> temp = new ArrayList<>();
      list.add("abcdefghijklmnopqrstuvwxyz");
      list.add(temp);
      list = temp;
    }
    list.add("a");
    list.add(original);
    try {
      Jinjava jinjava = new Jinjava(
        JinjavaConfig.newBuilder().withMaxOutputSize(10000).build()
      );
      JinjavaInterpreter.pushCurrent(jinjava.newInterpreter());
      assertThatThrownBy(() -> PyishObjectMapper.getAsPyishStringOrThrow(original))
        .as("The string to be serialized is larger than the max output size")
        .isInstanceOf(LengthLimitingJsonProcessingException.class);
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  @Test
  public void itSerializesToSnakeCaseAccessibleMap() {
    assertThat(PyishObjectMapper.getAsPyishString(new Foo("bar")))
      .isEqualTo("{'fooBar': 'bar'} |allow_snake_case");
  }

  @Test
  public void itSerializesToSnakeCaseAccessibleMapWhenInMapEntry() {
    assertThat(
        PyishObjectMapper.getAsPyishString(
          new AbstractMap.SimpleImmutableEntry<>("foo", new Foo("bar"))
        )
      )
      .isEqualTo("fn:map_entry('foo', {'fooBar': 'bar'} |allow_snake_case)");
  }

  @Test
  public void itDoesNotConvertToSnakeCaseMapWhenResultIsForOutput() {
    Jinjava jinjava = new Jinjava(
      JinjavaConfig
        .newBuilder()
        .withLegacyOverrides(
          LegacyOverrides.newBuilder().withUsePyishObjectMapper(true).build()
        )
        .build()
    );
    JinjavaInterpreter interpreter = jinjava.newInterpreter();
    interpreter.getContext().put("foo", new Foo("bar"));
    assertThat(interpreter.render("{{ foo }}")).isEqualTo("{'fooBar': 'bar'}");
  }

  @Test
  public void itSerializesToSnakeCaseWhenLegacyOverrideIsSet() {
    Jinjava jinjava = new Jinjava(
      JinjavaConfig
        .newBuilder()
        .withLegacyOverrides(
          LegacyOverrides
            .newBuilder()
            .withUsePyishObjectMapper(true)
            .withUseSnakeCasePropertyNaming(true)
            .build()
        )
        .build()
    );
    JinjavaInterpreter interpreter = jinjava.newInterpreter();
    try {
      JinjavaInterpreter.pushCurrent(interpreter);
      interpreter.getContext().put("foo", new Foo("bar"));
      assertThat(interpreter.render("{{ foo }}")).isEqualTo("{'foo_bar': 'bar'}");
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  static class Foo {
    private final String bar;

    public Foo(String bar) {
      this.bar = bar;
    }

    public String getFooBar() {
      return bar;
    }
  }
}
