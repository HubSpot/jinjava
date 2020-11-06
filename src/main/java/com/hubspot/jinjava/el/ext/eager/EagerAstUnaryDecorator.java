package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.util.ChunkResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstNode;
import de.odysseus.el.tree.impl.ast.AstUnary;
import javax.el.ELContext;

public class EagerAstUnaryDecorator extends AstUnary implements EvalResultHolder {
  private Object evalResult;
  protected final EvalResultHolder child;
  protected final Operator operator;

  public EagerAstUnaryDecorator(AstNode child, Operator operator) {
    this(EagerAstNodeDecorator.getAsEvalResultHolder(child), operator);
  }

  private EagerAstUnaryDecorator(EvalResultHolder child, Operator operator) {
    super((AstNode) child, operator);
    this.child = child;
    this.operator = operator;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      StringBuilder sb = new StringBuilder();
      sb.append(operator.toString());
      if (child.hasEvalResult()) {
        sb.append(
          ChunkResolver.getValueAsJinjavaStringSafe(child.getAndClearEvalResult())
        );
      } else {
        sb.append(e.getDeferredEvalResult());
      }
      throw new DeferredParsingException(sb.toString());
    } finally {
      child.getAndClearEvalResult();
    }
  }

  @Override
  public Object getAndClearEvalResult() {
    Object temp = evalResult;
    evalResult = null;
    return temp;
  }

  @Override
  public boolean hasEvalResult() {
    return evalResult != null;
  }
}
