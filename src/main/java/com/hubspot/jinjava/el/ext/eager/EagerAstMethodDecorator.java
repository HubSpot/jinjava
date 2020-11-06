package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.util.ChunkResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstMethod;
import de.odysseus.el.tree.impl.ast.AstParameters;
import de.odysseus.el.tree.impl.ast.AstProperty;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.el.ELContext;

public class EagerAstMethodDecorator extends AstMethod implements EvalResultHolder {
  private Object evalResult;
  // instanceof AstProperty
  protected final EvalResultHolder property;
  // instanceof AstParameters
  protected final EvalResultHolder params;

  public EagerAstMethodDecorator(AstProperty property, AstParameters params) {
    this(
      EagerAstNodeDecorator.getAsEvalResultHolder(property),
      EagerAstParametersDecorator.getAsEvalResultHolder(params)
    );
  }

  private EagerAstMethodDecorator(EvalResultHolder property, EvalResultHolder params) {
    super((AstProperty) property, (AstParameters) params);
    this.property = property;
    this.params = params;
  }

  @Override
  protected Object eval(
    Bindings bindings,
    ELContext context,
    boolean answerNullIfBaseIsNull
  ) {
    if (evalResult != null) {
      return evalResult;
    }
    try {
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      StringBuilder sb = new StringBuilder();
      if (property.getEvalResult() != null) {
        sb.append(ChunkResolver.getValueAsJinjavaStringSafe(property.getEvalResult()));
      } else {
        sb.append(e.getDeferredEvalResult());
        e = null;
      }
      String paramString;
      if (params.getEvalResult() != null) {
        paramString = ChunkResolver.getValueAsJinjavaStringSafe(params.getEvalResult());
      } else if (e != null) {
        paramString = e.getDeferredEvalResult();
      } else {
        try {
          paramString =
            Arrays
              .stream(((AstParameters) params).eval(bindings, context))
              .map(ChunkResolver::getValueAsJinjavaStringSafe)
              .collect(Collectors.joining(","));
        } catch (DeferredParsingException e1) {
          paramString = e1.getDeferredEvalResult();
        }
      }
      sb.append(String.format("(%s)", paramString));
      throw new DeferredParsingException(sb.toString());
    }
  }

  @Override
  public Object getEvalResult() {
    return evalResult;
  }
}
