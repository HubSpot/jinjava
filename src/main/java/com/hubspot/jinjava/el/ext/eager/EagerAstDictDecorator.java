package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstDict;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.util.ChunkResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstNode;
import java.util.Map;
import java.util.StringJoiner;
import javax.el.ELContext;

public class EagerAstDictDecorator extends AstDict implements EvalResultHolder {
  private Object evalResult;

  public EagerAstDictDecorator(Map<AstNode, AstNode> dict) {
    super(dict);
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      StringJoiner joiner = new StringJoiner(",");
      dict.forEach(
        (key, value) -> {
          StringJoiner kvJoiner = new StringJoiner(":");
          if (key instanceof EvalResultHolder) {
            if (((EvalResultHolder) key).hasEvalResult()) {
              kvJoiner.add(
                ChunkResolver.getValueAsJinjavaStringSafe(
                  ((EvalResultHolder) key).getAndClearEvalResult()
                )
              );
            } else {
              try {
                kvJoiner.add(
                  ChunkResolver.getValueAsJinjavaStringSafe(key.eval(bindings, context))
                );
              } catch (DeferredParsingException e1) {
                kvJoiner.add(e1.getDeferredEvalResult());
              }
            }
          } else {
            kvJoiner.add(key.toString());
          }

          if (value instanceof EvalResultHolder) {
            if (((EvalResultHolder) value).hasEvalResult()) {
              kvJoiner.add(
                ChunkResolver.getValueAsJinjavaStringSafe(
                  ((EvalResultHolder) value).getAndClearEvalResult()
                )
              );
            } else {
              try {
                kvJoiner.add(
                  ChunkResolver.getValueAsJinjavaStringSafe(value.eval(bindings, context))
                );
              } catch (DeferredParsingException e1) {
                kvJoiner.add(e1.getDeferredEvalResult());
              }
            }
          } else {
            kvJoiner.add(value.toString());
          }
          joiner.add(kvJoiner.toString());
        }
      );
      throw new DeferredParsingException(String.format("{%s}", joiner.toString()));
    } finally {
      dict.forEach(
        (key, value) -> {
          if (key instanceof EvalResultHolder) {
            ((EvalResultHolder) key).getAndClearEvalResult();
          }
          if (value instanceof EvalResultHolder) {
            ((EvalResultHolder) value).getAndClearEvalResult();
          }
        }
      );
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
}
