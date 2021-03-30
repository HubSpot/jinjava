package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstRangeBracket;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.util.ChunkResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELContext;

public class EagerAstRangeBracket extends AstRangeBracket implements EvalResultHolder {
  protected Object evalResult;

  public EagerAstRangeBracket(
    AstNode base,
    AstNode rangeStart,
    AstNode rangeMax,
    boolean lvalue,
    boolean strict,
    boolean ignoreReturnType
  ) {
    super(
      (AstNode) EagerAstNode.getAsEvalResultHolder(base),
      (AstNode) EagerAstNode.getAsEvalResultHolder(rangeStart),
      (AstNode) EagerAstNode.getAsEvalResultHolder(rangeMax),
      lvalue,
      strict,
      ignoreReturnType
    );
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      StringBuilder sb = new StringBuilder();
      if (((EvalResultHolder) prefix).hasEvalResult()) {
        sb.append(
          ChunkResolver.getValueAsJinjavaStringSafe(
            ((EvalResultHolder) prefix).getAndClearEvalResult()
          )
        );
      } else {
        sb.append(e.getDeferredEvalResult());
        e = null;
      }
      sb.append("[");
      if (((EvalResultHolder) property).hasEvalResult()) {
        sb.append(
          ChunkResolver.getValueAsJinjavaStringSafe(
            ((EvalResultHolder) property).getAndClearEvalResult()
          )
        );
      } else if (e != null) {
        sb.append(e.getDeferredEvalResult());
        e = null;
      } else {
        try {
          sb.append(
            ChunkResolver.getValueAsJinjavaStringSafe(property.eval(bindings, context))
          );
        } catch (DeferredParsingException e1) {
          sb.append(e1.getDeferredEvalResult());
        }
      }
      sb.append(":");
      if (((EvalResultHolder) rangeMax).hasEvalResult()) {
        sb.append(
          ChunkResolver.getValueAsJinjavaStringSafe(
            ((EvalResultHolder) rangeMax).getAndClearEvalResult()
          )
        );
      } else if (e != null) {
        sb.append(e.getDeferredEvalResult());
      } else {
        try {
          sb.append(
            ChunkResolver.getValueAsJinjavaStringSafe(rangeMax.eval(bindings, context))
          );
        } catch (DeferredParsingException e1) {
          sb.append(e1.getDeferredEvalResult());
        }
      }
      sb.append("]");
      throw new DeferredParsingException(AstRangeBracket.class, sb.toString());
    } finally {
      if (prefix != null) {
        ((EvalResultHolder) prefix).getAndClearEvalResult();
      }
      if (property != null) {
        ((EvalResultHolder) property).getAndClearEvalResult();
      }
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

  public AstNode getRangeMax() {
    return rangeMax;
  }

  public AstNode getRangeMin() {
    return property;
  }

  public AstNode getPrefix() {
    return prefix;
  }
}
