package com.hubspot.jinjava.lib.tag.eager;

import com.google.common.annotations.Beta;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.lib.tag.BlockTag;
import com.hubspot.jinjava.lib.tag.CallTag;
import com.hubspot.jinjava.lib.tag.CycleTag;
import com.hubspot.jinjava.lib.tag.DoTag;
import com.hubspot.jinjava.lib.tag.ElseIfTag;
import com.hubspot.jinjava.lib.tag.ElseTag;
import com.hubspot.jinjava.lib.tag.EndTag;
import com.hubspot.jinjava.lib.tag.ExtendsTag;
import com.hubspot.jinjava.lib.tag.ForTag;
import com.hubspot.jinjava.lib.tag.FromTag;
import com.hubspot.jinjava.lib.tag.IfTag;
import com.hubspot.jinjava.lib.tag.ImportTag;
import com.hubspot.jinjava.lib.tag.IncludeTag;
import com.hubspot.jinjava.lib.tag.PrintTag;
import com.hubspot.jinjava.lib.tag.RawTag;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.lib.tag.UnlessTag;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Beta
public class EagerTagFactory {
  public static final Map<Class<? extends Tag>, Class<? extends EagerTagDecorator<? extends Tag>>> EAGER_TAG_OVERRIDES = ImmutableMap
    .<Class<? extends Tag>, Class<? extends EagerTagDecorator<?>>>builder()
    .put(SetTag.class, EagerSetTag.class)
    .put(DoTag.class, EagerDoTag.class)
    .put(PrintTag.class, EagerPrintTag.class)
    .put(FromTag.class, EagerFromTag.class)
    .put(ImportTag.class, EagerImportTag.class)
    .put(IncludeTag.class, EagerIncludeTag.class)
    .put(ForTag.class, EagerForTag.class)
    .put(CycleTag.class, EagerCycleTag.class)
    .put(IfTag.class, EagerIfTag.class)
    .put(UnlessTag.class, EagerUnlessTag.class)
    .put(CallTag.class, EagerCallTag.class)
    .build();
  // These classes don't need an eager decorator.
  public static final Set<Class<? extends Tag>> TAG_CLASSES_TO_SKIP = ImmutableSet
    .<Class<? extends Tag>>builder()
    .add(BlockTag.class)
    .add(EndTag.class)
    .add(ElseIfTag.class)
    .add(ElseTag.class)
    .add(RawTag.class)
    .add(ExtendsTag.class) // TODO support reconstructing extends tags
    .build();

  @SuppressWarnings("unchecked")
  public static <T extends Tag> Optional<EagerTagDecorator<T>> getEagerTagDecorator(
    T tag
  ) {
    Class<? extends Tag> clazz = tag.getClass();
    try {
      if (TAG_CLASSES_TO_SKIP.contains(clazz)) {
        return Optional.empty();
      }
      if (EAGER_TAG_OVERRIDES.containsKey(clazz)) {
        EagerTagDecorator<?> decorator = EAGER_TAG_OVERRIDES
          .get(clazz)
          .getDeclaredConstructor(clazz)
          .newInstance(tag);
        if (decorator.getTag().getClass() == clazz) {
          return Optional.of((EagerTagDecorator<T>) decorator);
        }
      }
      return Optional.of(new EagerGenericTag<>(tag));
    } catch (NoSuchMethodException e) {
      return Optional.empty();
    } catch (Exception e) {
      Throwables.throwIfUnchecked(e);
      throw new RuntimeException(e);
    }
  }
}
