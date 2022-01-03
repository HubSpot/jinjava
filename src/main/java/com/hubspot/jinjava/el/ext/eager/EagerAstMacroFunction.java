package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstMacroFunction;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.tree.Bindings;
import com.hubspot.jinjava.el.tree.impl.ast.AstParameters;
import java.util.StringJoiner;
import jakarta.el.ELContext;

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
    return EvalResultHolder.super.eval(
      () -> super.eval(bindings, context),
      bindings,
      context
    );
  }

  @Override
  public String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException deferredParsingException,
    boolean preserveIdentifier
  ) {
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
            deferredParsingException,
            false
          )
        );
      }
      sb.append(String.format("(%s)", paramString));
    } catch (DeferredParsingException dpe) {
      sb.append(String.format("(%s)", dpe.getDeferredEvalResult()));
    }
    return sb.toString();
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
  public void clearEvalResult() {
    evalResult = null;
    hasEvalResult = false;
    params.clearEvalResult();
    for (int i = 0; i < ((AstParameters) params).getCardinality(); i++) {
      ((EvalResultHolder) ((AstParameters) params).getChild(i)).clearEvalResult();
    }
  }

  @Override
  public boolean hasEvalResult() {
    return hasEvalResult;
  }
}
