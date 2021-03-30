package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstTuple;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.collections.SizeLimitingPyList;
import com.hubspot.jinjava.util.ChunkResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstParameters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.el.ELContext;

public class EagerAstTuple extends AstTuple implements EvalResultHolder {
  private Object evalResult;

  public EagerAstTuple(AstParameters elements) {
    super(elements);
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    List<Object> list = new ArrayList<>();
    int deferredValuePosition = -1;

    for (int i = 0; i < elements.getCardinality(); i++) {
      try {
        Object result = elements.getChild(i).eval(bindings, context);
        if (deferredValuePosition >= 0) {
          list.add(ChunkResolver.getValueAsJinjavaStringSafe(result));
        } else {
          list.add(result);
        }
      } catch (DeferredParsingException e) {
        list.add(e.getDeferredEvalResult());
        if (deferredValuePosition == -1) {
          deferredValuePosition = i;
        }
      }
    }

    if (deferredValuePosition >= 0) {
      for (int i = deferredValuePosition - 1; i >= 0; i--) {
        // backfill any elements before the deferred value was encountered.
        list.set(i, ChunkResolver.getValueAsJinjavaStringSafe(list.get(i)));
      }
      throw new DeferredParsingException(
        AstTuple.class,
        String.format(
          "(%s)",
          list
            .stream()
            .map(Object::toString) // Already will be strings
            .collect(Collectors.joining(", "))
        )
      );
    }

    JinjavaInterpreter interpreter = (JinjavaInterpreter) context
      .getELResolver()
      .getValue(context, null, ExtendedParser.INTERPRETER);

    evalResult =
      new SizeLimitingPyList(
        Collections.unmodifiableList(list),
        interpreter.getConfig().getMaxListSize()
      );
    return evalResult;
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
