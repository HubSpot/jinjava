package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstNamedParameter;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstIdentifier;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELContext;

public class EagerAstNamedParameter
  extends AstNamedParameter
  implements EvalResultHolder {
  private Object evalResult;
  private final AstIdentifier name;
  private final EvalResultHolder value;

  public EagerAstNamedParameter(AstIdentifier name, AstNode value) {
    this(name, EagerAstNode.getAsEvalResultHolder(value));
  }

  private EagerAstNamedParameter(AstIdentifier name, EvalResultHolder value) {
    super(name, (AstNode) value);
    this.name = name;
    this.value = value;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      throw new DeferredParsingException(
        AstNamedParameter.class,
        String.format("%s=%s", name, e.getDeferredEvalResult())
      );
    } finally {
      value.getAndClearEvalResult();
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
