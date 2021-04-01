package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.util.ChunkResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstIdentifier;
import de.odysseus.el.tree.impl.ast.AstNode;
import de.odysseus.el.tree.impl.ast.AstParameters;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javax.el.ELContext;

public class EagerAstParameters extends AstParameters implements EvalResultHolder {
  private Object[] evalResult;
  private final List<AstNode> nodes;

  public EagerAstParameters(List<AstNode> nodes) {
    this(
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

  public static EvalResultHolder getAsEvalResultHolder(AstParameters astParameters) {
    if (astParameters instanceof EvalResultHolder) {
      return (EvalResultHolder) astParameters;
    }
    List<AstNode> nodes = new ArrayList<>();
    for (int i = 0; i < astParameters.getCardinality(); i++) {
      nodes.add(
        (AstNode) EagerAstNodeDecorator.getAsEvalResultHolder(astParameters.getChild(i))
      );
    }
    return new EagerAstParameters(nodes, true);
  }

  @Override
  public Object[] eval(Bindings bindings, ELContext context) {
    try {
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      StringJoiner joiner = new StringJoiner(", ");
      nodes
        .stream()
        .map(node -> (EvalResultHolder) node)
        .forEach(
          node -> {
            if (
              node instanceof AstIdentifier &&
              ExtendedParser.INTERPRETER.equals(((AstIdentifier) node).getName())
            ) {
              joiner.add(ExtendedParser.INTERPRETER);
            } else if (node.hasEvalResult()) {
              joiner.add(
                ChunkResolver.getValueAsJinjavaStringSafe(node.getAndClearEvalResult())
              );
            } else {
              try {
                joiner.add(
                  ChunkResolver.getValueAsJinjavaStringSafe(
                    ((AstNode) node).eval(bindings, context)
                  )
                );
              } catch (DeferredParsingException e1) {
                joiner.add(e1.getDeferredEvalResult());
              }
            }
          }
        );
      throw new DeferredParsingException(AstParameters.class, joiner.toString());
    } finally {
      nodes.forEach(node -> ((EvalResultHolder) node).getAndClearEvalResult());
    }
  }

  @Override
  public Object[] getAndClearEvalResult() {
    Object[] temp = evalResult;
    evalResult = null;
    return temp;
  }

  @Override
  public boolean hasEvalResult() {
    return evalResult != null;
  }
}
