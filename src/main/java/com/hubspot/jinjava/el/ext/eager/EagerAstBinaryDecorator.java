package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.util.ChunkResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstBinary;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELContext;

public class EagerAstBinaryDecorator extends AstBinary implements EvalResultHolder {
  protected Object evalResult;
  protected final EvalResultHolder left;
  protected final EvalResultHolder right;
  protected final Operator operator;

  public EagerAstBinaryDecorator(AstNode left, AstNode right, Operator operator) {
    this(
      EagerAstNodeDecorator.getAsEvalResultHolder(left),
      EagerAstNodeDecorator.getAsEvalResultHolder(right),
      operator
    );
  }

  private EagerAstBinaryDecorator(
    EvalResultHolder left,
    EvalResultHolder right,
    Operator operator
  ) {
    super((AstNode) left, (AstNode) right, operator);
    this.left = left;
    this.right = right;
    this.operator = operator;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      StringBuilder sb = new StringBuilder();
      if (left.hasEvalResult()) {
        sb.append(
          ChunkResolver.getValueAsJinjavaStringSafe(left.getAndClearEvalResult())
        );
        sb.append(String.format(" %s ", operator.toString()));
        sb.append(e.getDeferredEvalResult());
      } else {
        sb.append(e.getDeferredEvalResult());
        sb.append(String.format(" %s ", operator.toString()));
        try {
          sb.append(
            ChunkResolver.getValueAsJinjavaStringSafe(
              ((AstNode) right).eval(bindings, context)
            )
          );
        } catch (DeferredParsingException e1) {
          sb.append(e1.getDeferredEvalResult());
        }
      }
      throw new DeferredParsingException(sb.toString());
    } finally {
      left.getAndClearEvalResult();
      right.getAndClearEvalResult();
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
