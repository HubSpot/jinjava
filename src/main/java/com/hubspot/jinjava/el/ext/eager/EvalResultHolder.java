package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.el.ext.IdentifierPreservationStrategy;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstIdentifier;
import de.odysseus.el.tree.impl.ast.AstNode;
import java.util.Collection;
import java.util.function.Supplier;
import javax.el.ELContext;
import javax.el.ELException;

public interface EvalResultHolder {
  Object getEvalResult();

  void setEvalResult(Object evalResult);

  boolean hasEvalResult();

  default Object eval(
    Supplier<Object> evalSupplier,
    Bindings bindings,
    ELContext context
  ) {
    try {
      setEvalResult(evalSupplier.get());
      return checkEvalResultSize(context);
    } catch (DeferredValueException | ELException originalException) {
      DeferredParsingException e = EvalResultHolder.convertToDeferredParsingException(
        originalException
      );
      throw new DeferredParsingException(
        this,
        getPartiallyResolved(
          bindings,
          context,
          e,
          IdentifierPreservationStrategy.RESOLVING
        )
      );
    }
  }

  default Object checkEvalResultSize(ELContext context) {
    Object evalResult = getEvalResult();
    if (
      evalResult instanceof Collection &&
      ((Collection<?>) evalResult).size() > 100 && // TODO make size configurable
      (
        (JinjavaInterpreter) context
          .getELResolver()
          .getValue(context, null, ExtendedParser.INTERPRETER)
      ).getContext()
        .isDeferLargeObjects()
    ) {
      throw new DeferredValueException("Collection too big");
    }
    return evalResult;
  }

  String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException deferredParsingException,
    IdentifierPreservationStrategy identifierPreservationStrategy
  );

  static String reconstructNode(
    Bindings bindings,
    ELContext context,
    EvalResultHolder astNode,
    DeferredParsingException exception,
    IdentifierPreservationStrategy preserveIdentifier
  ) {
    if (astNode == null) {
      return "";
    }
    preserveIdentifier =
      IdentifierPreservationStrategy.preserving(
        preserveIdentifier.isPreserving() ||
        astNode instanceof AstIdentifier &&
        ExtendedParser.INTERPRETER.equals(((AstIdentifier) astNode).getName())
      );
    if (
      preserveIdentifier.isPreserving() &&
      !astNode.hasEvalResult() &&
      !(exceptionMatchesNode(exception, astNode))
    ) {
      // Evaluate to determine if the result is primitive. If so, we don't need to preserve the identifier
      try {
        EagerExpressionResolver.getValueAsJinjavaStringSafe(
          ((AstNode) astNode).eval(bindings, context)
        );
      } catch (DeferredParsingException ignored) {}
    }
    Object evalResult = astNode.getEvalResult();
    if (
      exceptionMatchesNode(exception, astNode) &&
      exception.getIdentifierPreservationStrategy().isPreserving()
    ) {
      return exception.getDeferredEvalResult();
    }
    if (
      !preserveIdentifier.isPreserving() ||
      (astNode.hasEvalResult() && EagerExpressionResolver.isPrimitive(evalResult))
    ) {
      if (exceptionMatchesNode(exception, astNode)) {
        return exception.getDeferredEvalResult();
      }
      if (!astNode.hasEvalResult()) {
        try {
          evalResult = ((AstNode) astNode).eval(bindings, context);
        } catch (DeferredParsingException e) {
          return e.getDeferredEvalResult();
        }
      }
      try {
        return EagerExpressionResolver.getValueAsJinjavaStringSafe(evalResult);
      } catch (DeferredValueException ignored) {}
    }
    return astNode.getPartiallyResolved(
      bindings,
      context,
      exception,
      IdentifierPreservationStrategy.PRESERVING
    );
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

  static boolean exceptionMatchesNode(
    DeferredParsingException deferredParsingException,
    Object astNode
  ) {
    return (
      deferredParsingException != null &&
      deferredParsingException.getSourceNode() == astNode
    );
  }
}
