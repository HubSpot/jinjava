package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstTuple;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.util.ChunkResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstParameters;
import java.util.StringJoiner;
import javax.el.ELContext;

public class EagerAstTuple extends AstTuple implements EvalResultHolder {
  private Object evalResult;

  public EagerAstTuple(AstParameters elements) {
    super(elements);
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      StringJoiner joiner = new StringJoiner(",");
      for (int i = 0; i < elements.getCardinality(); i++) {
        EvalResultHolder node = (EvalResultHolder) elements.getChild(i);
        try {
          Object result;
          if ((node).hasEvalResult()) {
            result = node.getAndClearEvalResult();
          } else {
            result = node.eval(bindings, context);
          }
          joiner.add(ChunkResolver.getValueAsJinjavaStringSafe(result));
        } catch (DeferredParsingException e1) {
          joiner.add(e1.getDeferredEvalResult());
        }
      }
      throw new DeferredParsingException(String.format("(%s)", joiner.toString()));
    } finally {
      ((EvalResultHolder) elements).getAndClearEvalResult();
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
