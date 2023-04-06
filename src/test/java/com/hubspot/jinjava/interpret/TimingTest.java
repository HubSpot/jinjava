package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.interpret.timing.TimingBlock;
import com.hubspot.jinjava.interpret.timing.TimingLevel;
import com.hubspot.jinjava.interpret.timing.Timings;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class TimingTest {
  private Timings timings;

  @Before
  public void setUp() {
    timings = new Timings(TimingLevel.ALL);
  }

  private void sleep(long t) {
    try {
      Thread.sleep(t);
    } catch (InterruptedException ignored) {}
  }

  @Test
  public void itAddsTimings() {
    TimingBlock timingBlock = timings.start(
      new TimingBlock("foo1", "bar", 1, 1, TimingLevel.LOW)
    );
    sleep(10);
    timings.end(timingBlock);

    timingBlock = timings.start(new TimingBlock("foo2", "bar", 2, 1, TimingLevel.LOW));
    sleep(10);
    timingBlock.putData("new", "zealand");
    timings.end(timingBlock);

    assertThat(timings.getBlocks()).hasSize(2);
    List<TimingBlock> children = timings.getBlocks();
    assertThat(children.get(0).getName()).isEqualTo("foo1");
    assertThat(children.get(0).getDuration())
      .isGreaterThan(Duration.of(9, ChronoUnit.MILLIS));
    assertThat(children.get(1).getName()).isEqualTo("foo2");
    assertThat(children.get(1).getData()).isEqualTo(ImmutableMap.of("new", "zealand"));
  }

  @Test
  public void itAddsNestedTimings() {
    TimingBlock parent = timings.start(
      new TimingBlock("parent", "bar", 1, 1, TimingLevel.LOW)
    );
    sleep(10);
    TimingBlock child1 = timings.start(
      new TimingBlock("child1", "bar", 1, 1, TimingLevel.LOW)
    );
    sleep(10);
    TimingBlock grandChild = timings.start(
      new TimingBlock("grandChild1", "bar", 1, 1, TimingLevel.LOW)
    );
    sleep(20);
    timings.end(grandChild);
    timings.end(child1);

    TimingBlock child2 = timings.start(
      new TimingBlock("child2", "bar", 1, 1, TimingLevel.LOW)
    );
    sleep(10);
    timings.end(child2);

    timings.end(parent);

    assertThat(timings.getBlocks()).hasSize(1);
    assertThat(timings.getBlocks().get(0)).isEqualTo(parent);
    assertThat(timings.getBlocks().get(0).getChildren()).containsExactly(child1, child2);
    assertThat(timings.getBlocks().get(0).getChildren().get(0).getChildren())
      .containsExactly(grandChild);
  }

  @Test
  public void itFiltersToStringByLevel() {
    TimingBlock parent = timings.start(
      new TimingBlock("parent", "bar", 1, 1, TimingLevel.LOW)
    );
    TimingBlock child1 = timings.start(
      new TimingBlock("child1", "bar", 1, 1, TimingLevel.LOW)
    );
    timings.end(child1);
    TimingBlock child2 = timings.start(
      new TimingBlock("child2", "bar", 1, 1, TimingLevel.HIGH)
    );
    timings.end(child2);
    timings.end(parent);

    assertThat(timings.toString(TimingLevel.ALL, Duration.ZERO))
      .contains("child1")
      .contains("child2");

    assertThat(timings.toString(TimingLevel.LOW, Duration.ZERO))
      .contains("child1")
      .doesNotContain("child2");
  }

  @Test
  public void itFiltersToStringByDuration() {
    TimingBlock parent = timings.start(
      new TimingBlock("parent", "bar", 1, 1, TimingLevel.LOW)
    );
    TimingBlock child1 = timings.start(
      new TimingBlock("slowKid", "bar", 1, 1, TimingLevel.LOW)
    );
    sleep(200);
    timings.end(child1);
    TimingBlock child2 = timings.start(
      new TimingBlock("fastKid", "bar", 1, 1, TimingLevel.HIGH)
    );
    sleep(1);
    timings.end(child2);
    timings.end(parent);

    assertThat(timings.toString(TimingLevel.ALL, Duration.ZERO))
      .contains("slowKid")
      .contains("fastKid");

    assertThat(timings.toString(TimingLevel.ALL, Duration.of(100, ChronoUnit.MILLIS)))
      .contains("slowKid")
      .doesNotContain("fastKid");
  }

  @Test
  public void itIncludesShortDurationsWithAlways() {
    timings.end(timings.start(new TimingBlock("always", "", 1, 1, TimingLevel.ALWAYS)));
    timings.end(timings.start(new TimingBlock("low", "", 1, 1, TimingLevel.LOW)));

    assertThat(timings.toString(TimingLevel.LOW, Duration.of(50, ChronoUnit.MILLIS)))
      .contains("always")
      .doesNotContain("low");
  }

  @Test
  public void itIgnoresTimingsAboveMaxLevel() {
    timings = new Timings(TimingLevel.LOW);
    timings.end(timings.start(new TimingBlock("parent", "", 1, 1, TimingLevel.HIGH)));

    assertThat(timings.getBlocks()).isEmpty();
  }
}
