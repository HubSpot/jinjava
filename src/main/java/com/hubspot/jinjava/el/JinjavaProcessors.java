package com.hubspot.jinjava.el;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.tree.Node;
import java.util.function.BiConsumer;

public class JinjavaProcessors {
  private final BiConsumer<Node, JinjavaInterpreter> nodePreProcessor;
  private final BiConsumer<Node, JinjavaInterpreter> nodePostProcessor;

  private final BiConsumer<Filter, JinjavaInterpreter> filterPreProcessor;
  private final BiConsumer<Filter, JinjavaInterpreter> filterPostProcessor;

  private JinjavaProcessors(Builder builder) {
    nodePreProcessor = builder.nodePreProcessor;
    nodePostProcessor = builder.nodePostProcessor;
    filterPreProcessor = builder.filterPreProcessor;
    filterPostProcessor = builder.filterPostProcessor;
  }

  public BiConsumer<Node, JinjavaInterpreter> getNodePreProcessor() {
    return nodePreProcessor;
  }

  public BiConsumer<Node, JinjavaInterpreter> getNodePostProcessor() {
    return nodePostProcessor;
  }

  public BiConsumer<Filter, JinjavaInterpreter> getFilterPreProcessor() {
    return filterPreProcessor;
  }

  public BiConsumer<Filter, JinjavaInterpreter> getFilterPostProcessor() {
    return filterPostProcessor;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private BiConsumer<Node, JinjavaInterpreter> nodePreProcessor = (n, i) -> {};
    private BiConsumer<Node, JinjavaInterpreter> nodePostProcessor = (n, i) -> {};

    private BiConsumer<Filter, JinjavaInterpreter> filterPreProcessor = (n, i) -> {};
    private BiConsumer<Filter, JinjavaInterpreter> filterPostProcessor = (n, i) -> {};

    private Builder() {}

    public Builder withNodePreProcessor(BiConsumer<Node, JinjavaInterpreter> processor) {
      this.nodePreProcessor = processor;
      return this;
    }

    public Builder withNodePostProcessor(BiConsumer<Node, JinjavaInterpreter> processor) {
      this.nodePostProcessor = processor;
      return this;
    }

    public Builder withFilterPreProcessor(
      BiConsumer<Filter, JinjavaInterpreter> processor
    ) {
      this.filterPreProcessor = processor;
      return this;
    }

    public Builder withFilterPostProcessor(
      BiConsumer<Filter, JinjavaInterpreter> processor
    ) {
      this.filterPostProcessor = processor;
      return this;
    }

    public JinjavaProcessors build() {
      return new JinjavaProcessors(this);
    }
  }
}
