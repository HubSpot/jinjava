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
package com.hubspot.jinjava.el;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.objects.SafeString;
import de.odysseus.el.util.SimpleContext;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotWritableException;
import org.junit.Test;

public class JinjavaStringELResolverTest {

  private final JinjavaStringELResolver resolver = new JinjavaStringELResolver();

  @Test
  public void itResolvesPositiveAndNegativeIndices() {
    for (Object index : new Object[] { 0, 0L, "0", "-0", -5, "-5" }) {
      assertThat(resolve("stuff", index)).isEqualTo("s");
    }
    for (Object index : new Object[] { 4, "4", -1L, "-1" }) {
      assertThat(resolve("stuff", index)).isEqualTo("f");
    }
  }

  @Test
  public void itReturnsNullOutsideTheString() {
    for (Object index : new Object[] {
      5,
      -6,
      Integer.MAX_VALUE,
      Integer.MIN_VALUE,
      Long.MAX_VALUE,
      Long.MIN_VALUE,
      "999999999999999999999999999999",
      "-999999999999999999999999999999",
    }) {
      assertThat(resolve("stuff", index)).isNull();
    }
    assertThat(resolve("", 0)).isNull();
    assertThat(resolve("", -1)).isNull();
  }

  @Test
  public void itUsesTheSameCharacterBoundariesAsStringSlices() {
    String value = "ä\uD83D\uDE00z";
    for (int i = 0; i < value.length(); i++) {
      assertThat(resolve(value, i)).isEqualTo(value.substring(i, i + 1));
      assertThat(resolve(value, i - value.length())).isEqualTo(value.substring(i, i + 1));
    }
  }

  @Test
  public void itLeavesNonIndexPropertiesUnresolved() {
    for (Object property : new Object[] {
      null,
      "",
      "-",
      "--1",
      "1-2",
      "+1",
      "1.5",
      1.5,
      true,
      "length",
      "empty",
    }) {
      assertUnresolved("stuff", property);
    }
  }

  @Test
  public void itLeavesOtherTypesUnresolved() {
    for (Object base : new Object[] {
      null,
      123,
      ImmutableList.of("s"),
      ImmutableMap.of("0", "s"),
      new String[] { "s" },
      new StringBuilder("stuff"),
      new SafeString("stuff"),
    }) {
      assertUnresolved(base, "0");
    }
  }

  @Test
  public void itKeepsStringsReadOnlyInBothDefaultResolvers() {
    for (ELResolver defaultResolver : new ELResolver[] {
      JinjavaInterpreterResolver.DEFAULT_RESOLVER_READ_ONLY,
      JinjavaInterpreterResolver.DEFAULT_RESOLVER_READ_WRITE,
    }) {
      ELContext context = new SimpleContext();
      assertThat(defaultResolver.getValue(context, "stuff", "0")).isEqualTo("s");
      assertThat(context.isPropertyResolved()).isTrue();
      context.setPropertyResolved(false);
      assertThat(defaultResolver.getType(context, "stuff", "0")).isEqualTo(String.class);
      assertThat(context.isPropertyResolved()).isTrue();
      context.setPropertyResolved(false);
      assertThat(defaultResolver.isReadOnly(context, "stuff", "0")).isTrue();
      assertThat(context.isPropertyResolved()).isTrue();
      context.setPropertyResolved(false);
      assertThatThrownBy(() -> defaultResolver.setValue(context, "stuff", "0", "x"))
        .isInstanceOf(PropertyNotWritableException.class);
      assertThat(context.isPropertyResolved()).isTrue();
    }
  }

  private Object resolve(String base, Object property) {
    ELContext context = new SimpleContext();
    Object result = resolver.getValue(context, base, property);
    assertThat(context.isPropertyResolved()).isTrue();
    return result;
  }

  private void assertUnresolved(Object base, Object property) {
    ELContext context = new SimpleContext();
    assertThat(resolver.getValue(context, base, property)).isNull();
    assertThat(context.isPropertyResolved()).isFalse();
    assertThat(resolver.getType(context, base, property)).isNull();
    assertThat(context.isPropertyResolved()).isFalse();
    assertThat(resolver.isReadOnly(context, base, property)).isFalse();
    assertThat(context.isPropertyResolved()).isFalse();
    resolver.setValue(context, base, property, "x");
    assertThat(context.isPropertyResolved()).isFalse();
  }
}
