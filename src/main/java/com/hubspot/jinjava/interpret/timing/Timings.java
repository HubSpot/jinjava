package com.hubspot.jinjava.interpret.timing;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Timings {
  private final Stack<TimingBlock> blockStack = new Stack<>();
  private final List<TimingBlock> blocks = new ArrayList<>();
  private final long start;

  private final TimingLevel maxLevel;

  public Timings(TimingLevel maxLevel) {
    this.start = System.nanoTime();
    this.maxLevel = maxLevel;
  }

  public TimingBlock start(TimingBlock block) {
    if (block.getLevel().getValue() < maxLevel.getValue()) {
      if (blockStack.isEmpty()) {
        blockStack.push(block.start());
        blocks.add(block);
      } else {
        blockStack.peek().startChild(block);
        blockStack.push(block);
      }
    }
    return block;
  }

  public void end(TimingBlock block) {
    block.end();
    if (!blockStack.isEmpty() && blockStack.peek() == block) {
      blockStack.pop();
    }
  }

  public long getStart() {
    return start;
  }

  public List<TimingBlock> getBlocks() {
    return blocks;
  }

  public <I> I record(TimingBlock timingBlock, Supplier<I> func) {
    timingBlock.start();
    try {
      return func.get();
    } finally {
      timingBlock.end();
    }
  }

  public void record(TimingBlock timingBlock, Runnable func) {
    timingBlock.start();
    try {
      func.run();
    } finally {
      timingBlock.end();
    }
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Timings.class.getSimpleName() + "[", "]")
      .add("blockStack=" + blockStack)
      .add("start=" + start)
      .toString();
  }

  public String toString(TimingLevel maxLevel, int minMillis) {
    return getBlocks()
      .stream()
      .map(b -> b.toString(maxLevel, minMillis))
      .collect(Collectors.joining("\n"));
  }

  public void clear() {
    this.blockStack.clear();
    blockStack.add(new TimingBlock("root", "", 0, 0, TimingLevel.LOW));
  }
}
