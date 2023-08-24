package com.hubspot.jinjava.objects.collections;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.interpret.IndexOutOfRangeException;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.Test;

public class PyListTest extends BaseJinjavaTest {

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
  public void itSupportsExtendOperationWithNullList() {
    assertThat(
        jinjava.render(
          "{% set test = [1, 2, 3] %}" + "{% do test.extend(null) %}" + "{{ test }}",
          Collections.emptyMap()
        )
      )
      .isEqualTo("[1, 2, 3]");
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
  public void itDisallowsInsertingSelf() {
    assertThat(
        jinjava.render(
          "{% set test = [1,2] %}" + "{% do test.insert(0, test) %}" + "{{ test }}",
          Collections.emptyMap()
        )
      )
      .isEqualTo("[1, 2]");
  }

  @Test
  public void itDisallowsAppendingSelf() {
    assertThat(
        jinjava.render(
          "{% set test = [1, 2] %}" + "{% do test.append(test) %}" + "{{ test }}",
          Collections.emptyMap()
        )
      )
      .isEqualTo("[1, 2]");
  }

  @Test
  public void itComputesHashCodeWhenListContainsItself() {
    PyList list1 = new PyList(new ArrayList<>());
    PyList list2 = new PyList(new ArrayList<>());
    list1.add(list2);
    int initialHashCode = list1.hashCode();
    list2.add(list1);
    int hashCodeWithInfiniteRecursion = list1.hashCode();
    assertThat(initialHashCode).isNotEqualTo(hashCodeWithInfiniteRecursion);
    assertThat(list1.hashCode())
      .isEqualTo(hashCodeWithInfiniteRecursion)
      .describedAs("Hash code should be consistent on multiple calls");
    assertThat(list2.hashCode())
      .isEqualTo(list1.hashCode())
      .describedAs(
        "The two lists are currently the same as they are both a list1 of a single infinitely recurring list"
      );
    list1.add(123456);
    assertThat(list2.hashCode())
      .isNotEqualTo(list1.hashCode())
      .describedAs(
        "The two lists are no longer the same as list1 has 2 elements while list2 has one"
      );
    PyList copy = list1.copy();
    assertThat(copy.hashCode())
      .isNotEqualTo(list1.hashCode())
      .describedAs(
        "copy is not the same as list1 because it is a list of a list of recursion, whereas list1 is a list of recursion"
      );
    assertThat(list1.copy().hashCode())
      .isEqualTo(copy.hashCode())
      .describedAs("All copies should have the same hash code");
  }
}
