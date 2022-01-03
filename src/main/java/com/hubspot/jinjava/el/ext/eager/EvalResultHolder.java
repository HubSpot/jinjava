package com.hubspot.jinjava.el.ext.eager;

import com.google.common.primitives.Primitives;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.el.tree.impl.ast.AstNode;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import com.hubspot.jinjava.el.tree.Bindings;
import com.hubspot.jinjava.el.tree.impl.ast.AstIdentifier;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import java.util.function.Supplier;

public interface EvalResultHolder {
  Object getEvalResult();

  void setEvalResult(Object evalResult);

  void clearEvalResult();

  boolean hasEvalResult();

  default Object eval(
    Supplier<Object> evalSupplier,
    Bindings bindings,
    ELContext context
  ) {
    try {
      setEvalResult(evalSupplier.get());
      return getEvalResult();
    } catch (DeferredValueException | ELException originalException) {
      DeferredParsingException e = EvalResultHolder.convertToDeferredParsingException(
        originalException
      );
      throw new DeferredParsingException(
        this,
        getPartiallyResolved(bindings, context, e, false)
      );
    }
  }

  String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException deferredParsingException,
    boolean preserveIdentifier
  );

  static String reconstructNode(
    Bindings bindings,
    ELContext context,
    EvalResultHolder astNode,
    DeferredParsingException exception,
    boolean preserveIdentifier
  ) {
    Object evalResult = astNode.getEvalResult();
    if (astNode.hasEvalResult() && (!preserveIdentifier || isPrimitive(evalResult))) {
      try {
        return EagerExpressionResolver.getValueAsJinjavaStringSafe(evalResult);
      } catch (DeferredValueException e) {
        preserveIdentifier = true;
      }
    }
    if (
      preserveIdentifier ||
      (
        astNode instanceof AstIdentifier &&
        ExtendedParser.INTERPRETER.equals(((AstIdentifier) astNode).getName())
      )
    ) {
      return astNode.getPartiallyResolved(bindings, context, exception, true);
    }
    if (exception != null && exception.getSourceNode() == astNode) {
      return exception.getDeferredEvalResult();
    }
    try {
      return EagerExpressionResolver.getValueAsJinjavaStringSafe(
        ((AstNode) astNode).eval(bindings, context)
      );
    } catch (DeferredParsingException e) {
      return e.getDeferredEvalResult();
    }
  }

  static DeferredParsingException convertToDeferredParsingException(
    RuntimeException original
  ) {
    DeferredValueException deferredValueException;
    if (!(original instanceof DeferredValueException)) {
      if (original.getCause() instanceof DeferredValueException) {
        deferredValueException = (DeferredValueException) original.getCause();
      } else {
        throw original;
      }
    } else {
      deferredValueException = (DeferredValueException) original;
    }
    if (deferredValueException instanceof DeferredParsingException) {
      return (DeferredParsingException) deferredValueException;
    }
    return null;
  }

  static boolean isPrimitive(Object evalResult) {
    return (
      evalResult == null ||
      Primitives.isWrapperType(evalResult.getClass()) ||
      evalResult instanceof String
    );
  }
}
