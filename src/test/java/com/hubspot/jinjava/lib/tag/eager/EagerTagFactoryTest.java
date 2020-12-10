package com.hubspot.jinjava.lib.tag.eager;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.lib.tag.IncludeTag;
import com.hubspot.jinjava.lib.tag.RawTag;
import com.hubspot.jinjava.lib.tag.Tag;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

public class EagerTagFactoryTest {

  @Test
  public void itGetsEagerTagDecoratorForOverrides() {
    Set<EagerTagDecorator<?>> eagerTagDecoratorSet = EagerTagFactory
      .EAGER_TAG_OVERRIDES.keySet()
      .stream()
      .map(EagerTagFactory::getEagerTagDecorator)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toSet());
    assertThat(eagerTagDecoratorSet.size())
      .isEqualTo(EagerTagFactory.EAGER_TAG_OVERRIDES.keySet().size());
    assertThat(
        eagerTagDecoratorSet
          .stream()
          .map(e -> e.getTag().getClass())
          .collect(Collectors.toSet())
      )
      .isEqualTo(EagerTagFactory.EAGER_TAG_OVERRIDES.keySet());
  }

  @Test
  public void itGetsEagerTagDecoratorForNonOverride() {
    Class<? extends Tag> clazz = IncludeTag.class;
    Optional<? extends EagerTagDecorator<? extends Tag>> maybeEagerGenericTag = EagerTagFactory.getEagerTagDecorator(
      clazz
    );
    assertThat(maybeEagerGenericTag).isPresent();
    assertThat(maybeEagerGenericTag.get()).isInstanceOf(EagerGenericTag.class);
    assertThat(maybeEagerGenericTag.get().getTag()).isInstanceOf(clazz);
  }
}
