package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.el.ext.IdentifierPreservationStrategy;
import com.hubspot.jinjava.interpret.Context.TemporaryValueClosable;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstNode;
import de.odysseus.el.tree.impl.ast.AstParameters;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javax.el.ELContext;
import javax.el.ELException;

public class EagerAstParameters extends AstParameters implements EvalResultHolder {
  protected Object evalResult;
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
    try (
      TemporaryValueClosable<Boolean> c = (
        (JinjavaInterpreter) context
          .getELResolver()
          .getValue(context, null, ExtendedParser.INTERPRETER)
      ).getContext()
        .withPartialMacroEvaluation(false)
    ) {
      try {
        setEvalResult(super.eval(bindings, context));
        return (Object[]) checkEvalResultSize(context);
      } catch (DeferredValueException | ELException originalException) {
        DeferredParsingException e = EvalResultHolder.convertToDeferredParsingException(
          originalException
        );
        throw new DeferredParsingException(
          this,
          getPartiallyResolved(
            bindings,
            context,
            e,
            IdentifierPreservationStrategy.PRESERVING
          ), // Need this to always be true because a function may modify the identifier
          IdentifierPreservationStrategy.PRESERVING
        );
      }
    }
  }

  @Override
  public String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException deferredParsingException,
    IdentifierPreservationStrategy identifierPreservationStrategy
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
              identifierPreservationStrategy
            )
          )
      );
    return joiner.toString();
  }

  @Override
  public Object getEvalResult() {
    return evalResult;
  }

  @Override
  public void setEvalResult(Object evalResult) {
    this.evalResult = evalResult;
    hasEvalResult = true;
  }

  @Override
  public boolean hasEvalResult() {
    return hasEvalResult;
  }
}
