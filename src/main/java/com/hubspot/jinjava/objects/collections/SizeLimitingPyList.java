package com.hubspot.jinjava.objects.collections;

import com.hubspot.jinjava.interpret.CollectionTooBigException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.objects.PyWrapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;

public class SizeLimitingPyList extends PyList implements PyWrapper {
  private int maxSize;
  private boolean hasWarned;

  private SizeLimitingPyList(List<Object> list) {
    super(list);
  }

  public SizeLimitingPyList(List<Object> list, int maxSize) {
    super(list);
    if (list == null) {
      throw new IllegalArgumentException("list is null");
    }
    if (maxSize <= 0) {
      throw new IllegalArgumentException("maxSize must be >= 1");
    }

    this.maxSize = maxSize;
    if (list.size() > maxSize) {
      throw new CollectionTooBigException(list.size(), maxSize);
    }
  }

  @Override
  public boolean add(Object element) {
    checkSize(size() + 1);
    return super.add(element);
  }

  @Override
  public void add(int index, Object element) {
    checkSize(size() + 1);
    super.add(index, element);
  }

  @Override
  public boolean addAll(int index, @Nonnull Collection<?> elements) {
    if (elements == null || elements.isEmpty()) {
      return false;
    }
    checkSize(size() + elements.size());
    return super.addAll(index, elements);
  }

  @Override
  public boolean addAll(@Nonnull Collection<?> elements) {
    if (elements == null || elements.isEmpty()) {
      return false;
    }
    checkSize(size() + elements.size());
    return super.addAll(elements);
  }

  @Override
  public PyList copy() {
    return new SizeLimitingPyList(new ArrayList<>(delegate()));
  }

  private void checkSize(int newSize) {
    if (newSize > maxSize) {
      throw new CollectionTooBigException(newSize, maxSize);
    } else if (!hasWarned && newSize >= maxSize * 0.9) {
      hasWarned = true;
      JinjavaInterpreter
        .getCurrent()
        .addError(
          new TemplateError(
            ErrorType.WARNING,
            ErrorReason.COLLECTION_TOO_BIG,
            String.format("List is at 90%% of max size (%d of %d)", newSize, maxSize),
            null,
            -1,
            -1,
            new CollectionTooBigException(newSize, maxSize)
          )
        );
    }
  }
}
