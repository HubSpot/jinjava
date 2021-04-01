package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.util.ChunkResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstMethod;
import de.odysseus.el.tree.impl.ast.AstNode;
import de.odysseus.el.tree.impl.ast.AstParameters;
import de.odysseus.el.tree.impl.ast.AstProperty;
import javax.el.ELContext;

public class EagerAstMethod extends AstMethod implements EvalResultHolder {
  private Object evalResult;
  // instanceof AstProperty
  protected final EvalResultHolder property;
  // instanceof AstParameters
  protected final EvalResultHolder params;

  public EagerAstMethod(AstProperty property, AstParameters params) {
    this(
      EagerAstNodeDecorator.getAsEvalResultHolder(property),
      EagerAstParameters.getAsEvalResultHolder(params)
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
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      StringBuilder sb = new StringBuilder();
      String paramString;
      if (property.hasEvalResult()) {
        sb.append(
          ChunkResolver.getValueAsJinjavaStringSafe(property.getAndClearEvalResult())
        );
        paramString = e.getDeferredEvalResult();
      } else if (params.hasEvalResult()) {
        paramString =
          ChunkResolver.getValueAsJinjavaStringSafe(params.getAndClearEvalResult());
        if (property instanceof EagerAstDot) {
          sb.append(
            String.format(
              "%s.%s",
              e.getDeferredEvalResult(),
              ((EagerAstDot) property).getProperty()
            )
          );
        } else {
          sb.append(e.getDeferredEvalResult());
        }
      } else {
        throw new DeferredParsingException(
          this,
          getPartiallyResolved(bindings, context, e)
        );
      }
      sb.append(String.format("(%s)", paramString));
      throw new DeferredParsingException(this, sb.toString());
    } finally {
      property.getAndClearEvalResult();
      params.getAndClearEvalResult();
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

  /**
   * This method is used when we need to reconstruct the method property and params manually.
   * Neither the property or params could be evaluated so we dive into the property and figure out
   * where the DeferredParsingException came from.
   */
  private String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException deferredParsingException
  ) {
    String stringPrefix;
    String stringMethod = "";
    String stringRangeMax = "";
    AstNode prefix;
    AstNode methodOrRangeMin = null;
    AstNode rangeMax = null;
    String formatString;
    if (property instanceof EagerAstDot) {
      formatString = "%s.%s";
      prefix = ((EagerAstDot) property).getPrefix();
      stringMethod = ((EagerAstDot) property).getProperty();
    } else if (property instanceof EagerAstBracket) {
      formatString = "%s[%s]";
      prefix = ((EagerAstBracket) property).getPrefix();
      methodOrRangeMin = ((EagerAstBracket) property).getMethod();
    } else if (property instanceof EagerAstRangeBracket) {
      formatString = "%s[%s:%s]";
      prefix = ((EagerAstRangeBracket) property).getPrefix();
      methodOrRangeMin = ((EagerAstRangeBracket) property).getRangeMin();
      rangeMax = ((EagerAstRangeBracket) property).getRangeMax();
    } else {
      throw new DeferredValueException("Cannot resolve property in EagerAstMethod");
    }

    // If prefix is an identifier, then preserve it in case the method should modify it.
    stringPrefix =
      EvalResultHolder.reconstructNode(
        bindings,
        context,
        (EvalResultHolder) prefix,
        deferredParsingException,
        true
      );

    if (methodOrRangeMin instanceof EvalResultHolder) {
      stringMethod =
        EvalResultHolder.reconstructNode(
          bindings,
          context,
          (EvalResultHolder) methodOrRangeMin,
          deferredParsingException,
          false
        );
    }

    if (rangeMax instanceof EvalResultHolder) {
      stringRangeMax =
        EvalResultHolder.reconstructNode(
          bindings,
          context,
          (EvalResultHolder) rangeMax,
          deferredParsingException,
          false
        );
    }
    String paramString;
    if (deferredParsingException.getSourceNode() == params) {
      paramString = deferredParsingException.getDeferredEvalResult();
    } else {
      try {
        paramString =
          ChunkResolver.getValueAsJinjavaStringSafe(params.eval(bindings, context));
        // remove brackets so they can get replaced with parentheses
        paramString = paramString.substring(1, paramString.length() - 1);
      } catch (DeferredParsingException e) {
        paramString = e.getDeferredEvalResult();
      }
    }

    return (
      String.format(formatString, stringPrefix, stringMethod, stringRangeMax) +
      String.format("(%s)", paramString)
    );
  }
}
