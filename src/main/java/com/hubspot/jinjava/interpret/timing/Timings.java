package com.hubspot.jinjava.interpret.timing;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Timings {
  private final Stack<TimingBlock> blockStack = new Stack<>();
  private final List<TimingBlock> blocks = new ArrayList<>();

  private final TimingLevel maxLevel;

  public Timings(TimingLevel maxLevel) {
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

  public String toString(TimingLevel maxLevel, Duration minDuration) {
    return getBlocks()
      .stream()
      .map(b -> b.toString(maxLevel, minDuration))
      .collect(Collectors.joining("\n"));
  }

  public void clear() {
    this.blockStack.clear();
    this.blocks.clear();
  }
}
