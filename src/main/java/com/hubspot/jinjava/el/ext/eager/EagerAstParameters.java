package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstNode;
import de.odysseus.el.tree.impl.ast.AstParameters;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javax.el.ELContext;

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
      throw new DeferredParsingException(
        this,
        getPartiallyResolved(bindings, context, e, false)
      );
    }
  }

  @Override
  public String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException deferredParsingException,
    boolean preserveIdentifier
  ) {
    StringJoiner joiner = new StringJoiner(", ");
    nodes
      .stream()
      .map(node -> (EvalResultHolder) node)
      .forEach(
        node ->
          joiner.add(
            EvalResultHolder.reconstructNode(
              bindings,
              context,
              node,
              deferredParsingException,
              false
            )
          )
      );
    return joiner.toString();
  }

  @Override
  public Object[] getEvalResult() {
    return evalResult;
  }

  @Override
  public void clearEvalResult() {
    evalResult = null;
    hasEvalResult = false;
    nodes.forEach(node -> ((EvalResultHolder) node).clearEvalResult());
  }

  @Override
  public boolean hasEvalResult() {
    return hasEvalResult;
  }
}
