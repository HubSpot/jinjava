package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstDict;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstNode;
import java.util.Map;
import java.util.StringJoiner;
import javax.el.ELContext;

public class EagerAstDict extends AstDict implements EvalResultHolder {
  protected Object evalResult;
  protected boolean hasEvalResult;

  public EagerAstDict(Map<AstNode, AstNode> dict) {
    super(dict);
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      evalResult = super.eval(bindings, context);
      hasEvalResult = true;
      return evalResult;
    } catch (DeferredParsingException e) {
      JinjavaInterpreter interpreter = (JinjavaInterpreter) context
        .getELResolver()
        .getValue(context, null, ExtendedParser.INTERPRETER);
      StringJoiner joiner = new StringJoiner(", ");
      dict.forEach(
        (key, value) -> {
          StringJoiner kvJoiner = new StringJoiner(": ");
          if (key instanceof EvalResultHolder) {
            kvJoiner.add(
              EvalResultHolder.reconstructNode(
                bindings,
                context,
                (EvalResultHolder) key,
                e,
                !interpreter.getConfig().getLegacyOverrides().isEvaluateMapKeys()
              )
            );
          } else {
            kvJoiner.add(key.toString());
          }
          if (value instanceof EvalResultHolder) {
            kvJoiner.add(
              EvalResultHolder.reconstructNode(
                bindings,
                context,
                (EvalResultHolder) value,
                e,
                false
              )
            );
          } else {
            kvJoiner.add(value.toString());
          }
          joiner.add(kvJoiner.toString());
        }
      );
      throw new DeferredParsingException(this, String.format("{%s}", joiner.toString()));
    }
  }

  @Override
  public Object getEvalResult() {
    return evalResult;
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
