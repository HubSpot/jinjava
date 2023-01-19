package com.hubspot.jinjava.objects.serialization;

import java.util.concurrent.atomic.AtomicInteger;

public class DepthAndWidthTracker {
  private final AtomicInteger depth;
  private final AtomicInteger width;

  public DepthAndWidthTracker(int depth, int width) {
    this.depth = new AtomicInteger(depth);
    this.width = new AtomicInteger(width);
  }

  public boolean acquire() {
    return depth.decrementAndGet() >= 0 && width.decrementAndGet() >= 0;
  }

  public void release() {
    depth.incrementAndGet();
  }

  public int getRemainingDepth() {
    return depth.get();
  }

  public int getRemainingWidth() {
    return width.get();
  }
}
