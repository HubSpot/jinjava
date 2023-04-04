package com.hubspot.jinjava.interpret.timing;

import java.util.Stack;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Timings {
  private final Stack<TimingBlock> blockStack = new Stack<>();
  private final long start;

  private final TimingLevel maxLevel;

  public Timings(TimingLevel maxLevel) {
    this.start = System.nanoTime();
    this.maxLevel = maxLevel;
    blockStack.add(new TimingBlock("root", "", 0, 0, TimingLevel.LOW));
  }

  public TimingBlock start(TimingBlock block) {
    if (block.getLevel().getValue() < maxLevel.getValue()) {
      blockStack.peek().startChild(block);
      blockStack.push(block);
    }
    return block;
  }

  public void end(TimingBlock block) {
    block.end();
    if (blockStack.peek() == block) {
      blockStack.pop();
    }
  }

  public long getStart() {
    return start;
  }

  public Stack<TimingBlock> getBlocks() {
    return blockStack;
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

  public String toString(TimingLevel maxLevel) {
    return getBlocks()
      .stream()
      .map(b -> b.toString(maxLevel))
      .collect(Collectors.joining(","));
  }

  public void clear() {
    this.blockStack.clear();
  }
}
