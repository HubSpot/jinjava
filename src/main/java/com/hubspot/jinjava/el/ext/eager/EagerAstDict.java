package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstDict;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.el.tree.impl.ast.AstIdentifier;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.el.tree.Bindings;
import com.hubspot.jinjava.el.tree.impl.ast.AstNode;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import java.util.Map;
import java.util.StringJoiner;
import jakarta.el.ELContext;

public class EagerAstDict extends AstDict implements EvalResultHolder {
  protected Object evalResult;
  protected boolean hasEvalResult;

  public EagerAstDict(Map<AstNode, AstNode> dict) {
    super(dict);
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
    JinjavaInterpreter interpreter = (JinjavaInterpreter) context
      .getELResolver()
      .getValue(context, null, ExtendedParser.INTERPRETER);
    StringJoiner joiner = new StringJoiner(", ");
    dict.forEach(
      (key, value) -> {
        StringJoiner kvJoiner = new StringJoiner(": ");
        if (key instanceof AstIdentifier) {
          kvJoiner.add(((AstIdentifier) key).getName());
        } else if (key instanceof EvalResultHolder) {
          kvJoiner.add(
            EvalResultHolder.reconstructNode(
              bindings,
              context,
              (EvalResultHolder) key,
              deferredParsingException,
              !interpreter.getConfig().getLegacyOverrides().isEvaluateMapKeys()
            )
          );
        } else {
          kvJoiner.add(
            EagerExpressionResolver.getValueAsJinjavaStringSafe(
              key.eval(bindings, context)
            )
          );
        }
        if (value instanceof EvalResultHolder) {
          kvJoiner.add(
            EvalResultHolder.reconstructNode(
              bindings,
              context,
              (EvalResultHolder) value,
              deferredParsingException,
              preserveIdentifier
            )
          );
        } else {
          kvJoiner.add(
            EagerExpressionResolver.getValueAsJinjavaStringSafe(
              value.eval(bindings, context)
            )
          );
        }
        joiner.add(kvJoiner.toString());
      }
    );
    return String.format("{%s}", joiner);
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

    dict.forEach(
      (key, value) -> {
        if (key instanceof EvalResultHolder) {
          ((EvalResultHolder) key).clearEvalResult();
        }
        if (value instanceof EvalResultHolder) {
          ((EvalResultHolder) value).clearEvalResult();
        }
      }
    );
  }

  @Override
  public boolean hasEvalResult() {
    return hasEvalResult;
  }
}
