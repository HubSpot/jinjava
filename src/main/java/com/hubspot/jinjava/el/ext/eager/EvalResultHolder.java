package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstIdentifier;
import javax.el.ELContext;

public interface EvalResultHolder {
  Object getAndClearEvalResult();

  boolean hasEvalResult();

  Object eval(Bindings bindings, ELContext elContext);

  static String reconstructNode(
    Bindings bindings,
    ELContext context,
    EvalResultHolder astNode,
    DeferredParsingException exception,
    boolean preserveIdentifier
  ) {
    String partiallyResolvedImage;
    if (
      astNode instanceof AstIdentifier &&
      (
        preserveIdentifier ||
        ExtendedParser.INTERPRETER.equals(((AstIdentifier) astNode).getName())
      )
    ) {
      astNode.getAndClearEvalResult(); // clear unused result
      partiallyResolvedImage = ((AstIdentifier) astNode).getName();
    } else if (astNode.hasEvalResult()) {
      partiallyResolvedImage =
        EagerExpressionResolver.getValueAsJinjavaStringSafe(
          astNode.getAndClearEvalResult()
        );
    } else if (exception.getSourceNode() == astNode) {
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
        astNode.getAndClearEvalResult();
      }
    }
    return partiallyResolvedImage;
  }
}
