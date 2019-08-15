package com.hubspot.jinjava.tree.output;

import java.util.List;
import java.util.Optional;

import com.hubspot.jinjava.tree.Node;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BlockInfo {

  private final List<? extends Node> nodes;

  private final Optional<String> parentPath;

  public BlockInfo(List<? extends Node> nodes, Optional<String> parentPath) {
    this.nodes = nodes;
    this.parentPath = parentPath;
  }

  public List<? extends Node> getNodes() {
    return nodes;
  }

  public Optional<String> getParentPath() {
    return parentPath;
  }
}
