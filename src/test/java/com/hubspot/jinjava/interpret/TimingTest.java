package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.interpret.timing.TimingBlock;
import com.hubspot.jinjava.interpret.timing.TimingLevel;
import com.hubspot.jinjava.interpret.timing.Timings;
import java.util.LinkedList;
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

    assertThat(timings.getBlocks()).hasSize(1);
    LinkedList<TimingBlock> children = timings.getBlocks().get(0).getChildren();
    assertThat(children).hasSize(2);
    assertThat(children.get(0).getName()).isEqualTo("foo1");
    assertThat(children.get(0).getDuration()).isGreaterThan(9);
    assertThat(children.get(1).getName()).isEqualTo("foo2");
    assertThat(children.get(1).getData()).isEqualTo(ImmutableMap.of("new", "zealand"));
  }

  @Test
  public void itAddsNestedTimings() {
    TimingBlock parent = timings.start(
      new TimingBlock("parent", "bar", 1, 1, TimingLevel.LOW)
    );
    sleep(10);
    TimingBlock child = timings.start(
      new TimingBlock("child", "bar", 1, 1, TimingLevel.LOW)
    );
    sleep(10);
    timings.end(child);
    timings.end(parent);

    LinkedList<TimingBlock> children = timings.getBlocks().get(0).getChildren();
    assertThat(timings.getBlocks()).hasSize(1);
    assertThat(children).hasSize(1);
    assertThat(children.get(0)).isEqualTo(parent);
    assertThat(children.get(0).getChildren()).hasSize(1);
    assertThat(children.get(0).getChildren().get(0)).isEqualTo(child);
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

    assertThat(timings.toString(TimingLevel.ALL)).contains("child1").contains("child2");

    assertThat(timings.toString(TimingLevel.LOW))
      .contains("child1")
      .doesNotContain("child2");
  }

  @Test
  public void itIgnoresTimingsAboveMaxLevel() {
    timings = new Timings(TimingLevel.NONE);
    TimingBlock parent = timings.start(
      new TimingBlock("parent", "bar", 1, 1, TimingLevel.LOW)
    );
    timings.end(parent);

    LinkedList<TimingBlock> children = timings.getBlocks().get(0).getChildren();
    assertThat(timings.getBlocks()).hasSize(1);
    assertThat(children).hasSize(0);
  }
}
