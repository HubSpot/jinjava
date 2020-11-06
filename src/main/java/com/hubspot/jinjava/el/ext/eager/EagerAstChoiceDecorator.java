package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstChoice;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELContext;
import javax.el.ELException;

public class EagerAstChoiceDecorator extends AstChoice implements EvalResultHolder {
  private Object evalResult;

  private final EvalResultHolder question;
  private final EvalResultHolder yes;
  private final EvalResultHolder no;

  public EagerAstChoiceDecorator(AstNode question, AstNode yes, AstNode no) {
    this(
      EagerAstNodeDecorator.getAsEvalResultHolder(question),
      EagerAstNodeDecorator.getAsEvalResultHolder(yes),
      EagerAstNodeDecorator.getAsEvalResultHolder(no)
    );
  }

  private EagerAstChoiceDecorator(
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
    if (evalResult != null) {
      return evalResult;
    }
    try {
      evalResult = super.eval(bindings, context);
      return evalResult;
    } catch (DeferredParsingException e) {
      StringBuilder sb = new StringBuilder();
      sb.append(e.getDeferredEvalResult());
      if (question.getEvalResult() != null) {
        // the question was evaluated so jump to either yes or no
        throw new DeferredParsingException(sb.toString());
      }
      sb.append(" ? ");
      if (yes.getEvalResult() != null) {
        sb.append(yes.getEvalResult());
      } else {
        try {
          sb.append(((AstNode) yes).eval(bindings, context));
        } catch (DeferredParsingException e1) {
          sb.append(e1.getDeferredEvalResult());
        }
      }
      sb.append(" : ");
      if (no.getEvalResult() != null) {
        sb.append(no.getEvalResult());
      } else {
        try {
          sb.append(((AstNode) no).eval(bindings, context));
        } catch (DeferredParsingException e1) {
          sb.append(e1.getDeferredEvalResult());
        }
      }
      throw new DeferredParsingException(sb.toString());
    }
  }

  @Override
  public Object getEvalResult() {
    return evalResult;
  }
}
