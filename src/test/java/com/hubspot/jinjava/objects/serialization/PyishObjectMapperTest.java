package com.hubspot.jinjava.objects.serialization;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hubspot.jinjava.objects.collections.SizeLimitingPyMap;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class PyishObjectMapperTest {

  @Test
  public void itSerializesMapWithNullKeysAsEmptyString() {
    Map<String, Object> map = new SizeLimitingPyMap(new HashMap<>(), 10);
    map.put("foo", "bar");
    map.put(null, "null");
    assertThat(PyishObjectMapper.getAsPyishString(map))
      .isEqualTo("{'': 'null', 'foo': 'bar'}");
  }

  @Test
  public void itSerializesMapEntrySet() throws JsonProcessingException {
    SizeLimitingPyMap map = new SizeLimitingPyMap(new HashMap<>(), 10);
    map.put("foo", "bar");
    String result = PyishObjectMapper.getAsPyishString(map.items());
    assertThat(result).isEqualTo("[fn:map_entry('foo', 'bar')]");
  }

  @Test
  public void itSerializesMapWithNullValues() {
    Map<String, Object> map = new SizeLimitingPyMap(new HashMap<>(), 10);
    map.put("foo", "bar");
    map.put("foobar", null);
    assertThat(PyishObjectMapper.getAsPyishString(map))
      .isEqualTo("{'foobar': null, 'foo': 'bar'}");
  }
}
