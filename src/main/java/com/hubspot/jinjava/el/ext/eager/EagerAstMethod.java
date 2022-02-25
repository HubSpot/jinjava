package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstMethod;
import de.odysseus.el.tree.impl.ast.AstNode;
import de.odysseus.el.tree.impl.ast.AstParameters;
import de.odysseus.el.tree.impl.ast.AstProperty;
import javax.el.ELContext;
import javax.el.ELException;

public class EagerAstMethod extends AstMethod implements EvalResultHolder {
  protected Object evalResult;
  protected boolean hasEvalResult;
  // instanceof AstProperty
  protected final EvalResultHolder property;
  // instanceof AstParameters
  protected final EvalResultHolder params;

  public EagerAstMethod(AstProperty property, AstParameters params) {
    this(
      EagerAstNodeDecorator.getAsEvalResultHolder(property),
      EagerAstNodeDecorator.getAsEvalResultHolder(params)
    );
  }

  private EagerAstMethod(EvalResultHolder property, EvalResultHolder params) {
    super((AstProperty) property, (AstParameters) params);
    this.property = property;
    this.params = params;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      setEvalResult(super.eval(bindings, context));
      return evalResult;
    } catch (DeferredValueException | ELException originalException) {
      DeferredParsingException e = EvalResultHolder.convertToDeferredParsingException(
        originalException
      );
      throw new DeferredParsingException(
        this,
        getPartiallyResolved(bindings, context, e, true) // Need this to always be true because the method may modify the identifier
      );
    }
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

  /**
   * This method is used when we need to reconstruct the method property and params manually.
   * Neither the property or params could be evaluated so we dive into the property and figure out
   * where the DeferredParsingException came from.
   */
  public String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException deferredParsingException,
    boolean preserveIdentifier
  ) {
    String propertyResult;
    propertyResult =
      (property).getPartiallyResolved(
          bindings,
          context,
          deferredParsingException,
          preserveIdentifier
        );
    String paramString;
    if (
      deferredParsingException != null &&
      deferredParsingException.getSourceNode() == params
    ) {
      paramString = deferredParsingException.getDeferredEvalResult();
    } else {
      try {
        paramString =
          EagerExpressionResolver.getValueAsJinjavaStringSafe(
            ((AstNode) params).eval(bindings, context)
          );
        // remove brackets so they can get replaced with parentheses
        paramString = paramString.substring(1, paramString.length() - 1);
      } catch (DeferredParsingException e) {
        paramString = e.getDeferredEvalResult();
      }
    }

    return (propertyResult + String.format("(%s)", paramString));
  }
}
