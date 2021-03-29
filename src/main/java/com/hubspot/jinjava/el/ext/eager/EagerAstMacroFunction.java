package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstMacroFunction;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.util.ChunkResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstParameters;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.el.ELContext;

public class EagerAstMacroFunction extends AstMacroFunction implements EvalResultHolder {
  protected Object evalResult;
  // instanceof AstParameters
  protected EvalResultHolder params;

  public EagerAstMacroFunction(
    String name,
    int index,
    AstParameters params,
    boolean varargs
  ) {
    this(name, index, EagerAstParameters.getAsEvalResultHolder(params), varargs);
  }

  private EagerAstMacroFunction(
    String name,
    int index,
    EvalResultHolder params,
    boolean varargs
  ) {
    super(name, index, (AstParameters) params, varargs);
    this.params = params;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredValueException e) {
      StringBuilder sb = new StringBuilder();
      sb.append(getName());
      String paramString;
      try {
        paramString =
          Arrays
            .stream(((AstParameters) params).eval(bindings, context))
            .map(ChunkResolver::getValueAsJinjavaStringSafe)
            .collect(Collectors.joining(","));
      } catch (DeferredParsingException dpe) {
        paramString = dpe.getDeferredEvalResult();
      }
      sb.append(String.format("(%s)", paramString));
      throw new DeferredParsingException(sb.toString());
    } finally {
      params.getAndClearEvalResult();
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
