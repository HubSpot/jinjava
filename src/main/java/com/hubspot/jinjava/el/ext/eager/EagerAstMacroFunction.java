package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstMacroFunction;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.interpret.DeferredValueException;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstParameters;
import java.util.StringJoiner;
import javax.el.ELContext;
import javax.el.ELException;

public class EagerAstMacroFunction extends AstMacroFunction implements EvalResultHolder {
  protected Object evalResult;
  protected boolean hasEvalResult;
  // instanceof AstParameters
  protected EvalResultHolder params;

  public EagerAstMacroFunction(
    String name,
    int index,
    AstParameters params,
    boolean varargs
  ) {
    this(name, index, EagerAstNodeDecorator.getAsEvalResultHolder(params), varargs);
  }

  private EagerAstMacroFunction(
    String name,
    int index,
    EvalResultHolder params,
    boolean varargs
  ) {
    super(name, index, (AstParameters) params, varargs);
    this.params = params;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      evalResult = super.eval(bindings, context);
      hasEvalResult = true;
      return evalResult;
    } catch (DeferredValueException | ELException e) {
      DeferredValueException e1;
      if (!(e instanceof DeferredValueException)) {
        if (e.getCause() instanceof DeferredValueException) {
          e1 = (DeferredValueException) e.getCause();
        } else {
          throw e;
        }
      } else {
        e1 = (DeferredValueException) e;
      }
      StringBuilder sb = new StringBuilder();
      sb.append(getName());
      try {
        StringJoiner paramString = new StringJoiner(", ");
        for (int i = 0; i < ((AstParameters) params).getCardinality(); i++) {
          paramString.add(
            EvalResultHolder.reconstructNode(
              bindings,
              context,
              (EvalResultHolder) ((AstParameters) params).getChild(i),
              e1 instanceof DeferredParsingException
                ? (DeferredParsingException) e1
                : null,
              false
            )
          );
        }
        sb.append(String.format("(%s)", paramString));
      } catch (DeferredParsingException dpe) {
        sb.append(String.format("(%s)", dpe.getDeferredEvalResult()));
      }
      throw new DeferredParsingException(this, sb.toString());
    } finally {
      params.getAndClearEvalResult();
      for (int i = 0; i < ((AstParameters) params).getCardinality(); i++) {
        ((EvalResultHolder) ((AstParameters) params).getChild(i)).getAndClearEvalResult();
      }
    }
  }

  @Override
  public Object getAndClearEvalResult() {
    Object temp = evalResult;
    evalResult = null;
    hasEvalResult = false;
    return temp;
  }

  @Override
  public boolean hasEvalResult() {
    return hasEvalResult;
  }
}
