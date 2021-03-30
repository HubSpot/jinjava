package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.util.ChunkResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstIdentifier;
import de.odysseus.el.tree.impl.ast.AstMethod;
import de.odysseus.el.tree.impl.ast.AstNode;
import de.odysseus.el.tree.impl.ast.AstParameters;
import de.odysseus.el.tree.impl.ast.AstProperty;
import java.util.Optional;
import javax.el.ELContext;

public class EagerAstMethod extends AstMethod implements EvalResultHolder {
  private Object evalResult;
  // instanceof AstProperty
  protected final EvalResultHolder property;
  // instanceof AstParameters
  protected final EvalResultHolder params;

  public EagerAstMethod(AstProperty property, AstParameters params) {
    this(
      EagerAstNode.getAsEvalResultHolder(property),
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
      //      if (e.getDeferredEvalResult().contains(ExtendedParser.INTERPRETER)) {
      //        throw new DeferredValueException("Cannot partially resolve");
      //      }
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
        Optional<String> maybePartiallyResolved = getPartiallyResolved(
          bindings,
          context,
          e.getDeferredEvalResult()
        );
        if (!maybePartiallyResolved.isPresent()) {
          throw new DeferredValueException("Cannot resolve method");
        }
        throw new DeferredParsingException(AstMethod.class, maybePartiallyResolved.get());
      }
      sb.append(String.format("(%s)", paramString));
      throw new DeferredParsingException(AstMethod.class, sb.toString());
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

  private Optional<String> getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    String deferredValueResult
  ) {
    boolean usedDeferredValueResult = false;
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
      return Optional.empty();
    }

    if (((EvalResultHolder) prefix).hasEvalResult()) {
      if (prefix instanceof AstIdentifier) {
        ((EvalResultHolder) prefix).getAndClearEvalResult(); // clear unused result
        stringPrefix = ((AstIdentifier) prefix).getName();
      } else {
        stringPrefix =
          ChunkResolver.getValueAsJinjavaStringSafe(
            ((EvalResultHolder) prefix).getAndClearEvalResult()
          );
      }
    } else {
      stringPrefix = deferredValueResult;
      usedDeferredValueResult = true;
    }
    if (methodOrRangeMin instanceof EvalResultHolder) {
      if (((EvalResultHolder) methodOrRangeMin).hasEvalResult()) {
        stringMethod =
          ChunkResolver.getValueAsJinjavaStringSafe(
            ((EvalResultHolder) methodOrRangeMin).getAndClearEvalResult()
          );
      } else if (usedDeferredValueResult) {
        stringMethod = getResultFromAstNode(bindings, context, methodOrRangeMin);
      } else {
        stringMethod = deferredValueResult;
        usedDeferredValueResult = true;
      }
    }

    if (rangeMax instanceof EvalResultHolder) {
      if (((EvalResultHolder) rangeMax).hasEvalResult()) {
        stringRangeMax =
          ChunkResolver.getValueAsJinjavaStringSafe(
            ((EvalResultHolder) rangeMax).getAndClearEvalResult()
          );
      } else if (usedDeferredValueResult) {
        stringRangeMax = getResultFromAstNode(bindings, context, rangeMax);
      } else {
        stringRangeMax = deferredValueResult;
        usedDeferredValueResult = true;
      }
    }
    String paramString;
    if (usedDeferredValueResult) {
      try {
        paramString =
          ChunkResolver.getValueAsJinjavaStringSafe(params.eval(bindings, context));
        // replace brackets with parens
        paramString = paramString.substring(1, paramString.length() - 1);
      } catch (DeferredParsingException e) {
        paramString = e.getDeferredEvalResult();
      }
    } else {
      paramString = deferredValueResult;
    }

    return Optional.of(
      String.format(formatString, stringPrefix, stringMethod, stringRangeMax) +
      String.format("(%s)", paramString)
    );
  }

  private String getResultFromAstNode(
    Bindings bindings,
    ELContext context,
    AstNode methodOrRangeMin
  ) {
    String stringMethod;
    try {
      stringMethod =
        ChunkResolver.getValueAsJinjavaStringSafe(
          methodOrRangeMin.eval(bindings, context)
        );
    } catch (DeferredParsingException e) {
      stringMethod = e.getDeferredEvalResult();
    } finally {
      ((EvalResultHolder) methodOrRangeMin).getAndClearEvalResult();
    }
    return stringMethod;
  }
}
