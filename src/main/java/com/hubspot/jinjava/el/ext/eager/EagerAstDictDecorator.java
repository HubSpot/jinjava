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
    if (evalResult != null) {
      return evalResult;
    }
    try {
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      StringJoiner joiner = new StringJoiner(",");
      dict.forEach(
        (key, value) -> {
          StringJoiner kvJoiner = new StringJoiner(":");
          if (((EvalResultHolder) key).getEvalResult() != null) {
            kvJoiner.add(
              ChunkResolver.getValueAsJinjavaStringSafe(
                ((EvalResultHolder) key).getEvalResult()
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

          if (((EvalResultHolder) value).getEvalResult() != null) {
            kvJoiner.add(
              ChunkResolver.getValueAsJinjavaStringSafe(
                ((EvalResultHolder) value).getEvalResult()
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
          joiner.add(kvJoiner.toString());
        }
      );
      throw new DeferredParsingException(String.format("{%s}", joiner.toString()));
    }
  }

  @Override
  public Object getEvalResult() {
    return evalResult;
  }
}
