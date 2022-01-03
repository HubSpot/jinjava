package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.tree.Bindings;
import com.hubspot.jinjava.el.tree.impl.ast.AstNode;
import com.hubspot.jinjava.el.tree.impl.ast.AstParameters;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import jakarta.el.ELContext;

public class EagerAstParameters extends AstParameters implements EvalResultHolder {
  protected Object[] evalResult;
  protected boolean hasEvalResult;
  protected final List<AstNode> nodes;

  public EagerAstParameters(List<AstNode> nodes) {
    this( // to avoid converting nodes twice, call separate constructor
      nodes
        .stream()
        .map(EagerAstNodeDecorator::getAsEvalResultHolder)
        .map(e -> (AstNode) e)
        .collect(Collectors.toList()),
      true
    );
  }

  private EagerAstParameters(List<AstNode> nodes, boolean convertedToEvalResultHolder) {
    super(nodes);
    this.nodes = nodes;
  }

  @Override
  public Object[] eval(Bindings bindings, ELContext context) {
    try {
      evalResult = super.eval(bindings, context);
      hasEvalResult = true;
      return evalResult;
    } catch (DeferredParsingException e) {
      StringJoiner joiner = new StringJoiner(", ");
      nodes
        .stream()
        .map(node -> (EvalResultHolder) node)
        .forEach(
          node ->
            joiner.add(
              EvalResultHolder.reconstructNode(bindings, context, node, e, false)
            )
        );
      throw new DeferredParsingException(this, joiner.toString());
    } finally {
      nodes.forEach(node -> ((EvalResultHolder) node).getAndClearEvalResult());
    }
  }

  @Override
  public Object[] getAndClearEvalResult() {
    Object[] temp = evalResult;
    evalResult = null;
    hasEvalResult = false;
    return temp;
  }

  @Override
  public boolean hasEvalResult() {
    return hasEvalResult;
  }
}
