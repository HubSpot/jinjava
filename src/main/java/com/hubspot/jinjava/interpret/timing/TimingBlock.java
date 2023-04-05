package com.hubspot.jinjava.interpret.timing;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class TimingBlock {
  final String name;
  private final String fileName;
  private final int lineNumber;
  private final TimingLevel timingLevel;
  private final int position;
  private Instant start;
  private Instant end;

  private Map<String, Object> data;
  final LinkedList<TimingBlock> children = new LinkedList<>();

  public TimingBlock(
    String name,
    String fileName,
    int lineNumber,
    int position,
    TimingLevel timingLevel
  ) {
    this.name = name == null ? null : name.substring(0, Math.min(name.length(), 20));
    this.fileName = fileName;
    this.lineNumber = lineNumber;
    this.position = position;
    this.timingLevel = timingLevel;
  }

  public String getName() {
    return name;
  }

  public String getFileName() {
    return fileName;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public int getPosition() {
    return position;
  }

  public Instant getStart() {
    return start;
  }

  public Instant getEnd() {
    return end;
  }

  public TimingLevel getLevel() {
    return timingLevel;
  }

  public TimingBlock start() {
    if (start == null) {
      this.start = Instant.now();
    }
    return this;
  }

  void end() {
    if (end == null) {
      this.end = Instant.now();
    }
  }

  /**
   * Override the duration. Will not be overwritten by any subsequent calls to {@link #end()}
   */
  void end(Duration duration) {
    end = start.plus(duration);
  }

  public Duration getDuration() {
    return Duration.between(start, end);
  }

  public List<TimingBlock> getChildren() {
    return children;
  }

  public TimingBlock putData(String key, Object value) {
    if (data == null) {
      data = new HashMap<>();
      data.put(key, value);
    }
    return this;
  }

  public Map<String, Object> getData() {
    if (data == null) {
      return Collections.emptyMap();
    }
    return data;
  }

  TimingBlock startChild(TimingBlock block) {
    this.children.add(block.start());
    return block;
  }

  public String toString(TimingLevel maxLevel, Duration minDuration) {
    if (timingLevel.getValue() > maxLevel.getValue()) {
      return "";
    }
    if (getDuration().toNanos() < minDuration.toNanos()) {
      return "";
    }

    StringBuilder s = new StringBuilder(name)
      .append(": ")
      .append(getDuration())
      .append(" ms.");

    if (data != null && !data.isEmpty()) {
      s.append(" [");
      s.append(
        data
          .entrySet()
          .stream()
          .map(d -> d.getKey() + " = " + d.getValue().toString())
          .collect(Collectors.joining(","))
      );
      s.append("] ");
    }

    if (children.size() > 0) {
      StringBuilder childrenStringBuilder = new StringBuilder();
      for (TimingBlock b : children) {
        for (String line : b.toString(maxLevel, minDuration).split("\n")) {
          if (!line.isEmpty()) {
            childrenStringBuilder.append('\t').append(line).append('\n');
          }
        }
      }
      if (childrenStringBuilder.length() > 0) {
        s.append(" {\n");
        s.append(childrenStringBuilder);
        s.append("}");
      }
    }

    return s.toString();
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", TimingBlock.class.getSimpleName() + "[", "]")
      .add("name='" + name + "'")
      .add("fileName='" + fileName + "'")
      .add("lineNumber=" + lineNumber)
      .add("timingLevel=" + timingLevel)
      .add("position=" + position)
      .add("start=" + start)
      .add("end=" + end)
      .add("data=" + data)
      .add("children=" + children)
      .toString();
  }
}
