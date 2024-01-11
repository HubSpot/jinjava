package com.hubspot.jinjava.tree.output;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.tree.parse.TokenScannerSymbols;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;
import java.util.LinkedList;
import java.util.List;

public class OutputList {

  public static final String PREVENT_ACCIDENTAL_EXPRESSIONS =
    "PREVENT_ACCIDENTAL_EXPRESSIONS";
  private final List<OutputNode> nodes = new LinkedList<>();
  private final List<BlockPlaceholderOutputNode> blocks = new LinkedList<>();
  private final long maxOutputSize;
  private long currentSize;

  public OutputList(long maxOutputSize) {
    this.maxOutputSize = maxOutputSize;
  }

  public void addNode(OutputNode node) {
    if (maxOutputSize > 0 && currentSize + node.getSize() > maxOutputSize) {
      throw new OutputTooBigException(maxOutputSize, currentSize + node.getSize());
    }

    currentSize += node.getSize();
    nodes.add(node);

    if (node instanceof BlockPlaceholderOutputNode) {
      BlockPlaceholderOutputNode blockNode = (BlockPlaceholderOutputNode) node;

      if (maxOutputSize > 0 && currentSize + blockNode.getSize() > maxOutputSize) {
        throw new OutputTooBigException(maxOutputSize, currentSize + blockNode.getSize());
      }

      currentSize += blockNode.getSize();
      blocks.add(blockNode);
    }
  }

  public List<OutputNode> getNodes() {
    return nodes;
  }

  public List<BlockPlaceholderOutputNode> getBlocks() {
    return blocks;
  }

  public String getValue() {
    LengthLimitingStringBuilder val = new LengthLimitingStringBuilder(maxOutputSize);

    return JinjavaInterpreter
      .getCurrentMaybe()
      .map(JinjavaInterpreter::getConfig)
      .filter(config ->
        config
          .getFeatures()
          .getActivationStrategy(PREVENT_ACCIDENTAL_EXPRESSIONS)
          .isActive(null)
      )
      .map(config ->
        joinNodesWithoutAddingExpressions(val, config.getTokenScannerSymbols())
      )
      .orElseGet(() -> joinNodes(val));
  }

  private String joinNodesWithoutAddingExpressions(
    LengthLimitingStringBuilder val,
    TokenScannerSymbols tokenScannerSymbols
  ) {
    String separator = getWhitespaceSeparator(tokenScannerSymbols);
    String prev = null;
    String cur;
    for (OutputNode node : nodes) {
      try {
        cur = node.getValue();
        if (
          prev != null &&
          prev.length() > 0 &&
          prev.charAt(prev.length() - 1) == tokenScannerSymbols.getExprStartChar()
        ) {
          if (
            cur.length() > 0 &&
            TokenScannerSymbols.isNoteTagOrExprChar(tokenScannerSymbols, cur.charAt(0))
          ) {
            val.append(separator);
          }
        }
        prev = cur;
        val.append(node.getValue());
      } catch (OutputTooBigException e) {
        JinjavaInterpreter
          .getCurrent()
          .addError(TemplateError.fromOutputTooBigException(e));
        return val.toString();
      }
    }

    return val.toString();
  }

  private static String getWhitespaceSeparator(TokenScannerSymbols tokenScannerSymbols) {
    @SuppressWarnings("StringBufferReplaceableByString")
    String separator = new StringBuilder()
      .append('\n')
      .append(tokenScannerSymbols.getPrefixChar())
      .append(tokenScannerSymbols.getNoteChar())
      .append(tokenScannerSymbols.getTrimChar())
      .append(' ')
      .append(tokenScannerSymbols.getNoteChar())
      .append(tokenScannerSymbols.getExprEndChar())
      .toString();
    return separator;
  }

  private String joinNodes(LengthLimitingStringBuilder val) {
    for (OutputNode node : nodes) {
      try {
        val.append(node.getValue());
      } catch (OutputTooBigException e) {
        JinjavaInterpreter
          .getCurrent()
          .addError(TemplateError.fromOutputTooBigException(e));
        return val.toString();
      }
    }

    return val.toString();
  }

  @Override
  public String toString() {
    return getValue();
  }
}
