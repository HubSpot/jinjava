package com.hubspot.jinjava.objects.collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.interpret.CollectionTooBigException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class SizeLimitingPyListTest extends BaseInterpretingTest {

  @Test
  public void itDoesntLimitByDefault() {
    SizeLimitingPyList objects = new SizeLimitingPyList(
      createList(10),
      Integer.MAX_VALUE
    );
    assertThat(objects.size()).isEqualTo(10);
  }

  @Test(expected = CollectionTooBigException.class)
  public void itLimitsOnCreate() {
    new SizeLimitingPyList(createList(4), 2);
  }

  @Test
  public void itWarnsOnAdd() {
    JinjavaInterpreter interpreter = JinjavaInterpreter.getCurrent();
    SizeLimitingPyList objects = new SizeLimitingPyList(new ArrayList<>(), 10);
    int i;
    for (i = 1; i < 9; i++) {
      objects.add(i);
      assertThat(interpreter.getErrors()).isEmpty();
    }
    objects.add(i++);
    assertThat(interpreter.getErrors().size()).isEqualTo(1);
    assertThat(interpreter.getErrors().get(0).getException())
      .isInstanceOf(CollectionTooBigException.class);
    objects.add(i++);

    assertThatThrownBy(() -> objects.add(10))
      .isInstanceOf(CollectionTooBigException.class);
  }

  @Test
  public void itLimitsOnAdd() {
    List<Object> list = createList(3);
    SizeLimitingPyList objects = new SizeLimitingPyList(new ArrayList<>(), 2);
    objects.add(list.get(0));
    objects.add(list.get(1));
    assertThatThrownBy(() -> objects.add(list.get(2)))
      .isInstanceOf(CollectionTooBigException.class);
  }

  @Test
  public void itLimitsOnAppend() {
    List<Object> list = createList(3);
    SizeLimitingPyList objects = new SizeLimitingPyList(new ArrayList<>(), 2);
    objects.append(list.get(0));
    objects.append(list.get(1));
    assertThatThrownBy(() -> objects.append(list.get(2)))
      .isInstanceOf(CollectionTooBigException.class);
  }

  @Test
  public void itLimitsOnInsert() {
    List<Object> list = createList(3);
    SizeLimitingPyList objects = new SizeLimitingPyList(new ArrayList<>(), 2);
    objects.add(list.get(0));
    objects.add(list.get(1));
    assertThatThrownBy(() -> objects.insert(1, list.get(2)))
      .isInstanceOf(CollectionTooBigException.class);
  }

  @Test
  public void itLimitsOnAddAll() {
    List<Object> list = createList(3);
    SizeLimitingPyList objects = new SizeLimitingPyList(new ArrayList<>(), 2);
    assertThatThrownBy(() -> objects.addAll(list))
      .isInstanceOf(CollectionTooBigException.class);
  }

  public static List<Object> createList(int size) {
    List<Object> result = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      result.add(i + 1);
    }
    return result;
  }
}
