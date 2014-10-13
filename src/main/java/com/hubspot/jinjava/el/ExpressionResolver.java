package com.hubspot.jinjava.el;

import java.util.Objects;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.PropertyNotFoundException;
import javax.el.ValueExpression;

import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;

public class ExpressionResolver {

  private JinjavaInterpreter interpreter;
  private ExpressionFactory expressionFactory;
  private ELContext elContext;
  
  public ExpressionResolver(JinjavaInterpreter interpreter, ELContext elContext) {
    this.interpreter = interpreter;
    this.expressionFactory = interpreter.getExpressionFactory();
    this.elContext = elContext;
  }

  public Object resolve(String expr, int lineNumber) {
    interpreter.setLineNumber(lineNumber);

    CharSequence cleanExpr = Objects.toString(expr, "").trim();
    if (StringUtils.isBlank(cleanExpr)) {
      return "";
    }

    try {
      ValueExpression valueExp = expressionFactory.createValueExpression(
          elContext, new StringBuilder("#{").append(cleanExpr).append('}')
              .toString(), Object.class);
      return valueExp.getValue(elContext);
    } catch (PropertyNotFoundException e) {
      interpreter.addError(TemplateError.fromUnknownProperty(e.getMessage(), lineNumber));
    } catch (ELException e) {
      interpreter.addError(TemplateError
          .fromSyntaxError(new InterpretException(String.format(
              "Syntax error in [%s]", expr), e, lineNumber)));
    } catch (Exception e) {
      interpreter.addError(TemplateError.fromException(new InterpretException(
          String.format("Error resolving expression: [%s]", expr), e, lineNumber)));
    }

    return Boolean.FALSE;
  }

}
