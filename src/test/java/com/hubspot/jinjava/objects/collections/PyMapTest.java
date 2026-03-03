package com.hubspot.jinjava.objects.collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.IndexOutOfRangeException;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.junit.Test;

public class PyMapTest extends BaseJinjavaTest {

  @Test
  public void itSupportsAppendOperation() {
    assertThat(
      jinjava.render(
        "{% set test = [1, 2, 3] %}" + "{% do test.append(4) %}" + "{{ test }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("[1, 2, 3, 4]");
  }

  @Test
  public void itSupportsExtendOperation() {
    assertThat(
      jinjava.render(
        "{% set test = [1, 2, 3] %}" + "{% do test.extend([4, 5, 6]) %}" + "{{ test }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("[1, 2, 3, 4, 5, 6]");
  }

  @Test
  public void itSupportsInsertOperation() {
    assertThat(
      jinjava.render(
        "{% set test = [1, 2, 3] %}" + "{% do test.insert(1, 4) %}" + "{{ test }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("[1, 4, 2, 3]");
  }

  @Test
  public void itSupportsInsertOperationWithNegativeIndex() {
    assertThat(
      jinjava.render(
        "{% set test = [1, 2, 3] %}" + "{% do test.insert(-1, 4) %}" + "{{ test }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("[1, 2, 4, 3]");
  }

  @Test
  public void itSupportsInsertOperationWithLargeNegativeIndex() {
    assertThat(
      jinjava.render(
        "{% set test = [1, 2, 3] %}" + "{% do test.insert(-99, 4) %}" + "{{ test }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("[4, 1, 2, 3]");
  }

  @Test
  public void itHandlesInsertOperationOutOfRange() {
    RenderResult renderResult = jinjava.renderForResult(
      "{% set test = [1, 2, 3] %}" + "{% do test.insert(99, 4) %}" + "{{ test }}",
      Collections.emptyMap()
    );

    assertThat(renderResult.getOutput()).isEqualTo("[1, 2, 3]");
    assertThat(renderResult.getErrors().get(0)).isInstanceOf(TemplateError.class);
    assertThat(renderResult.getErrors().get(0).getException().getCause())
      .isInstanceOf(IndexOutOfRangeException.class);
  }

  @Test
  public void itSupportsPopOperation() {
    assertThat(
      jinjava.render(
        "{% set test = [1, 2, 3] %}" + "{{ test.pop() }}" + "{{ test }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("3[1, 2]");
  }

  @Test
  public void itSupportsPopAtIndexOperation() {
    assertThat(
      jinjava.render(
        "{% set test = [1, 2, 3] %}" + "{{ test.pop(1) }}" + "{{ test }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("2[1, 3]");
  }

  @Test
  public void itSupportsPopAtNegativeIndexOperation() {
    assertThat(
      jinjava.render(
        "{% set test = [1, 2, 3] %}" + "{{ test.pop(-1) }}" + "{{ test }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("3[1, 2]");
  }

  @Test
  public void itThrowsIndexOutOfRangeForPopOfEmptyList() {
    RenderResult renderResult = jinjava.renderForResult(
      "{% set test = [] %}" + "{{ test.pop() }}",
      Collections.emptyMap()
    );

    assertThat(renderResult.getOutput()).isEqualTo("");
    assertThat(renderResult.getErrors().get(0)).isInstanceOf(TemplateError.class);
    assertThat(renderResult.getErrors().get(0).getException().getCause())
      .isInstanceOf(IndexOutOfRangeException.class);
  }

  @Test
  public void itThrowsIndexOutOfRangeForPopOutOfRange() {
    RenderResult renderResult = jinjava.renderForResult(
      "{% set test = [1] %}" + "{{ test.pop(1) }},{{ test.pop(0) }},{{ test.pop(0) }}",
      Collections.emptyMap()
    );

    assertThat(renderResult.getOutput()).isEqualTo(",1,");
    assertThat(renderResult.getErrors().get(0)).isInstanceOf(TemplateError.class);
    assertThat(renderResult.getErrors().get(0).getException().getCause())
      .isInstanceOf(IndexOutOfRangeException.class);
  }

  @Test
  public void itSupportsClearOperation() {
    assertThat(
      jinjava.render(
        "{% set test = [1, 2, 3] %}" + "{% do test.clear() %}" + "{{ test }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("[]");
  }

  @Test
  public void itSupportsCountOperation() {
    assertThat(
      jinjava.render(
        "{% set test = [1, 1, 2, 2, 2, 3] %}" + "{{ test.count(2) }}" + "{{ test }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("3[1, 1, 2, 2, 2, 3]");
  }

  @Test
  public void itSupportsReverseOperation() {
    assertThat(
      jinjava.render(
        "{% set test = [1, 2, 3] %}" + "{% do test.reverse() %}" + "{{ test }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("[3, 2, 1]");
  }

  @Test
  public void itSupportsCopyOperation() {
    assertThat(
      jinjava.render(
        "{% set test = [1, 2, 3] %}" +
        "{% set test2 = test.copy() %}" +
        "{% do test.append(4) %}" +
        "{{ test }}{{test2}}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("[1, 2, 3, 4][1, 2, 3]");
  }

  @Test
  public void itSupportsIndexOperation() {
    assertThat(
      jinjava.render(
        "{% set test = [10, 20, 30, 10, 20, 30] %}" + "{{ test.index(20) }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("1");
  }

  @Test
  public void itSupportsIndexWithinBoundsOperation() {
    assertThat(
      jinjava.render(
        "{% set test = [10, 20, 30, 10, 20, 30] %}" + "{{ test.index(20, 2, 6) }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("4");
  }

  @Test
  public void itReturnsNegativeOneForMissingObjectForIndex() {
    assertThat(
      jinjava.render(
        "{% set test = [10, 20, 30, 10, 20, 30] %}" + "{{ test.index(999) }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("-1");
  }

  @Test
  public void itReturnsNegativeOneForMissingObjectForIndexWithinBounds() {
    assertThat(
      jinjava.render(
        "{% set test = [10, 20, 30, 10, 20, 30] %}" + "{{ test.index(999, 1, 5) }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("-1");
  }

  @Test
  public void itDisallowsSelfReferencingPut() {
    PyMap pyMap = new PyMap(new HashMap<String, Object>());
    assertThatThrownBy(() -> pyMap.put("x", pyMap))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Can't add map object to itself");
  }

  @Test
  public void itDisallowsSelfReferencingUpdate() {
    PyMap pyMap = new PyMap(new HashMap<String, Object>());
    assertThatThrownBy(() -> pyMap.update(pyMap))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Can't update map object with itself");
  }

  @Test
  public void itDisallowsSelfReferencingPutAll() {
    PyMap pyMap = new PyMap(new HashMap<String, Object>());
    assertThatThrownBy(() -> pyMap.putAll(pyMap))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Map putAll() operation can't be used to add map to itself");
  }

  @Test
  public void itUpdatesKeysWithStaticName() {
    assertThat(
      jinjava.render(
        "{% set test = {\"key1\": \"value1\"} %}" +
        "{% do test.update({\"key1\": \"value2\"}) %}" +
        "{{ test[\"key1\"] }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("value2");
  }

  @Test
  public void itDoesntSetKeysWithVariableNameWhenLegacy() {
    jinjava =
      new Jinjava(
        BaseJinjavaTest
          .newConfigBuilder()
          .withLegacyOverrides(
            LegacyOverrides.newBuilder().withEvaluateMapKeys(false).build()
          )
          .build()
      );
    assertThat(
      jinjava.render(
        "{% set keyName = \"key1\" %}" +
        "{% set test = {keyName: \"value1\"} %}" +
        "{{ test['keyName'] }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("value1");
  }

  @Test
  public void itSetsKeysWithVariableName() {
    assertThat(
      jinjava.render(
        "{% set keyName = \"key1\" %}" +
        "{% set test = {keyName: \"value1\"} %}" +
        "{{ test[keyName] }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("value1");
  }

  @Test
  public void itGetsKeysWithVariableName() {
    assertThat(
      jinjava.render(
        "{% set test = {\"key1\": \"value1\"} %}" +
        "{% set keyName = \"key1\" %}" +
        "{{ test[keyName] }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("value1");
  }

  @Test
  public void itSupportsGetWithOptionalDefault() {
    assertThat(
      jinjava.render(
        "{% set test = {\"key1\": \"value1\"} %}" +
        "{{ test.get(\"key2\", \"default\") }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("default");
  }

  @Test
  public void itFallsBackUnknownVariableNameToString() {
    assertThat(
      jinjava.render(
        "{% set test = {keyName: \"value1\"} %}" + "{{ test[\"keyName\"] }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("value1");
  }

  @Test
  public void itDoesntUpdateKeysWithVariableNameWhenLegacy() {
    jinjava =
      new Jinjava(
        BaseJinjavaTest
          .newConfigBuilder()
          .withLegacyOverrides(LegacyOverrides.THREE_POINT_0.withEvaluateMapKeys(false))
          .build()
      );
    assertThat(
      jinjava.render(
        "{% set test = {\"key1\": \"value1\"} %}" +
        "{% set keyName = \"key1\" %}" +
        "{% do test.update({keyName: \"value2\"}) %}" +
        "{{ test['key1'] }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("value1");
  }

  @Test
  public void itUpdatesKeysWithVariableName() {
    jinjava =
      new Jinjava(
        BaseJinjavaTest
          .newConfigBuilder()
          .withLegacyOverrides(LegacyOverrides.THREE_POINT_0.withEvaluateMapKeys(true))
          .build()
      );
    assertThat(
      jinjava.render(
        "{% set test = {\"key1\": \"value1\"} %}" +
        "{% set keyName = \"key1\" %}" +
        "{% do test.update({keyName: \"value2\"}) %}" +
        "{{ test[keyName] }}",
        Collections.emptyMap()
      )
    )
      .isEqualTo("value2");
  }

  @Test
  public void itComputesHashCodeWhenContainedWithinItself() {
    PyMap map = new PyMap(new HashMap<>());
    map.put("map1key1", "value1");

    PyMap map2 = new PyMap(new HashMap<>());
    map2.put("map2key1", map);

    map.put("map1key2", map2);

    assertThat(map.hashCode()).isNotEqualTo(0);
  }

  @Test
  public void itComputesHashCodeWhenContainedWithinItselfWithFurtherEntries() {
    PyMap map = new PyMap(new HashMap<>());
    map.put("map1key1", "value1");

    PyMap map2 = new PyMap(new HashMap<>());
    map2.put("map2key1", map);

    map.put("map1key2", map2);

    int originalHashCode = map.hashCode();
    map2.put("newKey", "newValue");
    int newHashCode = map.hashCode();
    assertThat(originalHashCode).isNotEqualTo(newHashCode);
  }

  @Test
  public void itComputesHashCodeWhenContainedWithinItselfInsideList() {
    PyMap map = new PyMap(new HashMap<>());
    map.put("map1key1", "value1");

    PyMap map2 = new PyMap(new HashMap<>());
    map2.put("map2key1", map);

    map.put("map1key2", new PyList(ImmutableList.of((map2))));

    assertThat(map.hashCode()).isNotEqualTo(0);
  }

  @Test
  public void itComputesHashCodeWithNullKeysAndValues() {
    PyMap map = new PyMap(new HashMap<>());
    map.put(null, "value1");

    PyMap map2 = new PyMap(new HashMap<>());
    map2.put("map2key1", map);

    PyList list = new PyList(new ArrayList<>());
    list.add(null);
    map.put("map1key2", new PyList(list));

    assertThat(map.hashCode()).isNotEqualTo(0);
  }
}
