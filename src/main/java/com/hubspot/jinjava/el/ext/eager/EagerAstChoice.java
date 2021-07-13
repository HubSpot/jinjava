package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstChoice;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELContext;
import javax.el.ELException;

public class EagerAstChoice extends AstChoice implements EvalResultHolder {
  protected Object evalResult;
  protected boolean hasEvalResult;
  protected final EvalResultHolder question;
  protected final EvalResultHolder yes;
  protected final EvalResultHolder no;

  public EagerAstChoice(AstNode question, AstNode yes, AstNode no) {
    this(
      EagerAstNodeDecorator.getAsEvalResultHolder(question),
      EagerAstNodeDecorator.getAsEvalResultHolder(yes),
      EagerAstNodeDecorator.getAsEvalResultHolder(no)
    );
  }

  private EagerAstChoice(
    EvalResultHolder question,
    EvalResultHolder yes,
    EvalResultHolder no
  ) {
    super((AstNode) question, (AstNode) yes, (AstNode) no);
    this.question = question;
    this.yes = yes;
    this.no = no;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) throws ELException {
    try {
      evalResult = super.eval(bindings, context);
      hasEvalResult = true;
      return evalResult;
    } catch (DeferredParsingException e) {
      if (question.hasEvalResult()) {
        // the question was evaluated so jump to either yes or no
        throw new DeferredParsingException(this, e.getDeferredEvalResult());
      }
      String sb =
        e.getDeferredEvalResult() +
        " ? " +
        EvalResultHolder.reconstructNode(bindings, context, yes, e, false) +
        " : " +
        EvalResultHolder.reconstructNode(bindings, context, no, e, false);
      throw new DeferredParsingException(this, sb);
    } finally {
      question.getAndClearEvalResult();
      yes.getAndClearEvalResult();
      no.getAndClearEvalResult();
    }
  }

  @Override
  public Object getAndClearEvalResult() {
    Object temp = evalResult;
    evalResult = null;
    hasEvalResult = false;
    return temp;
  }

  @Override
  public boolean hasEvalResult() {
    return hasEvalResult;
  }
}
