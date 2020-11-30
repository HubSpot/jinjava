package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.lib.tag.DoTag;
import com.hubspot.jinjava.lib.tag.PrintTag;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.lib.tag.Tag;
import java.util.Map;
import java.util.Optional;

public class EagerTagFactory {
  public static final Map<Class<? extends Tag>, Class<? extends EagerTagDecorator<? extends Tag>>> EAGER_TAG_OVERRIDES = ImmutableMap
    .<Class<? extends Tag>, Class<? extends EagerTagDecorator<?>>>builder()
    .put(SetTag.class, EagerSetTag.class)
    .put(DoTag.class, EagerDoTag.class)
    .put(PrintTag.class, EagerPrintTag.class)
    .build();

  @SuppressWarnings("unchecked")
  public static <T extends Tag> Optional<EagerTagDecorator<T>> getEagerTagDecorator(
    Class<T> clazz
  ) {
    try {
      if (EAGER_TAG_OVERRIDES.containsKey(clazz)) {
        EagerTagDecorator<?> decorator = EAGER_TAG_OVERRIDES
          .get(clazz)
          .getDeclaredConstructor()
          .newInstance();
        if (decorator.getTag().getClass() == clazz) {
          return Optional.of((EagerTagDecorator<T>) decorator);
        }
      }
      T tag = clazz.getDeclaredConstructor().newInstance();
      return Optional.of(new EagerGenericTag<>(tag));
    } catch (NoSuchMethodException e) {
      return Optional.empty();
    } catch (Exception e) {
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
    }
  }
}
