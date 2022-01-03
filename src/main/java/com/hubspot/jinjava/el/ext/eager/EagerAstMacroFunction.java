package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstMacroFunction;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.el.tree.Bindings;
import com.hubspot.jinjava.el.tree.impl.ast.AstParameters;
import java.util.StringJoiner;
import jakarta.el.ELContext;
import jakarta.el.ELException;

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
    } catch (DeferredValueException | ELException originalException) {
      DeferredParsingException e = EvalResultHolder.convertToDeferredParsingException(
        originalException
      );
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
              e,
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
