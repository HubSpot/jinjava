package com.hubspot.jinjava.el;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

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
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.util.JinjavaPropertyNotResolvedException;

import de.odysseus.el.tree.TreeBuilderException;

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
      interpreter.addError(new TemplateError(ErrorType.WARNING, ErrorReason.UNKNOWN, e.getMessage(), "", lineNumber, e));
    } catch (JinjavaPropertyNotResolvedException e) {
      interpreter.addError(TemplateError.fromUnknownProperty(e.getBase(), e.getProperty(), lineNumber));
    } catch (TreeBuilderException e) {
      interpreter.addError(TemplateError.fromException(new TemplateSyntaxException(expr,
          "Error parsing '" + expr + "': " + StringUtils.substringAfter(e.getMessage(), "': "), lineNumber, e)));
    } catch (ELException e) {
      if (e.getCause() instanceof JinjavaPropertyNotResolvedException) {
        JinjavaPropertyNotResolvedException jpe = (JinjavaPropertyNotResolvedException) e.getCause();
        interpreter.addError(TemplateError.fromUnknownProperty(jpe.getBase(), jpe.getProperty(), lineNumber));
      }
      else {
        interpreter.addError(TemplateError.fromException(new TemplateSyntaxException(expr, e.getMessage(), lineNumber, e)));
      }
    } catch (Exception e) {
      interpreter.addError(TemplateError.fromException(new InterpretException(
          String.format("Error resolving expression [%s]: " + getRootCauseMessage(e), expr), e, lineNumber)));
    }

    return "";
  }

}
