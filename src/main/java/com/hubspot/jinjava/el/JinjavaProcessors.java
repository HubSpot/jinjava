package com.hubspot.jinjava.el;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.Node;
import java.util.function.BiConsumer;

public class JinjavaProcessors {

  private final BiConsumer<Node, JinjavaInterpreter> nodePreProcessor;
  private final BiConsumer<Node, JinjavaInterpreter> nodePostProcessor;

  private JinjavaProcessors(Builder builder) {
    nodePreProcessor = builder.nodePreProcessor;
    nodePostProcessor = builder.nodePostProcessor;
  }

  public BiConsumer<Node, JinjavaInterpreter> getNodePreProcessor() {
    return nodePreProcessor;
  }

  public BiConsumer<Node, JinjavaInterpreter> getNodePostProcessor() {
    return nodePostProcessor;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(JinjavaProcessors processors) {
    return new Builder(processors);
  }

  public static class Builder {

    private BiConsumer<Node, JinjavaInterpreter> nodePreProcessor = (n, i) -> {};
    private BiConsumer<Node, JinjavaInterpreter> nodePostProcessor = (n, i) -> {};

    private Builder() {}

    private Builder(JinjavaProcessors processors) {
      this.nodePreProcessor = processors.nodePreProcessor;
      this.nodePostProcessor = processors.nodePostProcessor;
    }

    public Builder withNodePreProcessor(BiConsumer<Node, JinjavaInterpreter> processor) {
      this.nodePreProcessor = processor;
      return this;
    }

    public Builder withNodePostProcessor(BiConsumer<Node, JinjavaInterpreter> processor) {
      this.nodePostProcessor = processor;
      return this;
    }

    public JinjavaProcessors build() {
      return new JinjavaProcessors(this);
    }
  }
}
