package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.hubspot.jinjava.lib.tag.Tag;
import java.util.Optional;
import java.util.Set;

public class EagerTagFactory {
  private Set<Class<? extends Tag>> skippedTagClasses;

  public EagerTagFactory() {}

  public EagerTagFactory(Class<? extends Tag>... skippedTagClass) {
    this.skippedTagClasses = Sets.newHashSet(skippedTagClass);
  }

  public EagerTagFactory(Set<Class<? extends Tag>> skippedTagClasses) {
    this.skippedTagClasses = skippedTagClasses;
  }

  public <T extends Tag> Optional<EagerTagDecorator<T>> getEagerTagDecorator(
    Class<T> clazz
  ) {
    try {
      if (skippedTagClasses.contains(clazz)) {
        return Optional.empty();
      }
      T tag = clazz.getDeclaredConstructor().newInstance();
      return Optional.of(new EagerGenericTagDecorator<>(tag));
    } catch (Exception e) {
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
    }
  }
}
