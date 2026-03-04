package com.hubspot.jinjava.objects.collections;

import com.hubspot.jinjava.interpret.CollectionTooBigException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.objects.PyWrapper;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;

public class SizeLimitingPySet extends PySet implements PyWrapper {

  private int maxSize;
  private boolean hasWarned;

  public SizeLimitingPySet(Set<Object> set, int maxSize) {
    super(set);
    if (set == null) {
      throw new IllegalArgumentException("set is null");
    }
    if (maxSize <= 0) {
      throw new IllegalArgumentException("maxSize must be >= 1");
    }

    this.maxSize = maxSize;
    if (set.size() > maxSize) {
      throw new CollectionTooBigException(set.size(), maxSize);
    }
  }

  @Override
  public boolean add(Object element) {
    checkSize(size() + 1);
    return super.add(element);
  }

  @Override
  public boolean addAll(@Nonnull Collection<?> elements) {
    if (elements == null || elements.isEmpty()) {
      return false;
    }
    checkSize(size() + elements.size());
    return super.addAll(elements);
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
            String.format("Set is at 90%% of max size (%d of %d)", newSize, maxSize),
            null,
            -1,
            -1,
            new CollectionTooBigException(newSize, maxSize)
          )
        );
    }
  }
}
