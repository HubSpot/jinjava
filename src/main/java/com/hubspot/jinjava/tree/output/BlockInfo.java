package com.hubspot.jinjava.tree.output;

import java.util.List;

import com.hubspot.jinjava.tree.Node;

public class BlockInfo {

  private final List<? extends Node> nodes;

  private final String parentPath;

  public BlockInfo(List<? extends Node> nodes, String parentPath) {
    this.nodes = nodes;
    this.parentPath = parentPath;
  }

  public List<? extends Node> getNodes() {
    return nodes;
  }

  public String getParentPath() {
    return parentPath;
  }
}
