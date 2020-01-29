package com.hubspot.jinjava.tree.output;

import java.util.List;
import java.util.Optional;

import com.hubspot.jinjava.tree.Node;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BlockInfo {

  private final List<? extends Node> nodes;

  private final Optional<String> parentPath;

  private final int parentLineNo;

  private final int parentPosition;

  public BlockInfo(List<? extends Node> nodes, Optional<String> parentPath, int parentLineNo, int parentPosition) {
    this.nodes = nodes;
    this.parentPath = parentPath;
    this.parentLineNo = parentLineNo;
    this.parentPosition = parentPosition;
  }

  public List<? extends Node> getNodes() {
    return nodes;
  }

  public Optional<String> getParentPath() {
    return parentPath;
  }

  public int getParentLineNo() {
    return parentLineNo;
  }

  public int getParentPosition() {
    return parentPosition;
  }
}
