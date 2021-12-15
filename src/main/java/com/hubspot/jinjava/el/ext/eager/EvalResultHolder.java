package com.hubspot.jinjava.el.ext.eager;

import com.google.common.primitives.Primitives;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstIdentifier;
import javax.el.ELContext;

public interface EvalResultHolder {
  Object getEvalResult();

  void clearEvalResult();

  boolean hasEvalResult();

  Object eval(Bindings bindings, ELContext elContext);

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
    String partiallyResolvedImage;
    Object evalResult = astNode.getEvalResult();
    if (astNode.hasEvalResult() && (!preserveIdentifier || isPrimitive(evalResult))) {
      partiallyResolvedImage =
        EagerExpressionResolver.getValueAsJinjavaStringSafe(
          evalResult // this used to clear result too
        );
    } else if (
      preserveIdentifier ||
      (
        astNode instanceof AstIdentifier &&
        ExtendedParser.INTERPRETER.equals(((AstIdentifier) astNode).getName())
      )
    ) {
      partiallyResolvedImage =
        astNode.getPartiallyResolved(bindings, context, exception, true);
    } else if (exception != null && exception.getSourceNode() == astNode) {
      partiallyResolvedImage = exception.getDeferredEvalResult();
    } else {
      try {
        partiallyResolvedImage =
          EagerExpressionResolver.getValueAsJinjavaStringSafe(
            astNode.eval(bindings, context)
          );
      } catch (DeferredParsingException e) {
        partiallyResolvedImage = e.getDeferredEvalResult();
      } finally {
        //        astNode.getAndClearEvalResult();
      }
    }
    return partiallyResolvedImage;
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
