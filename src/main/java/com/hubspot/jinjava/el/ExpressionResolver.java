package com.hubspot.jinjava.el;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

import java.util.List;

import javax.el.ELException;
import javax.el.ELResolver;
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
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import de.odysseus.el.tree.TreeBuilderException;

public class ExpressionResolver {

  private final JinjavaInterpreter interpreter;
  private final ExpressionFactory expressionFactory;
  private final JinjavaELContext elContext;

  public ExpressionResolver(JinjavaInterpreter interpreter) {
    this.interpreter = interpreter;
    this.expressionFactory = interpreter.getExpressionFactory();

    this.elContext = new JinjavaELContext(new JinjavaInterpreterResolver(interpreter));
    for (ELFunctionDefinition fn : interpreter.getContext().getAllFunctions()) {
      this.elContext.setFunction(fn.getNamespace(), fn.getLocalName(), fn.getMethod());
    }
  }

  public Object resolveExpression(String expr) {
    if (StringUtils.isBlank(expr)) {
      return "";
    }

    try {
      String elExpression = "#{" + expr.trim() + "}";
      ValueExpression valueExp = expressionFactory.createValueExpression(elContext, elExpression, Object.class);
      return valueExp.getValue(elContext);

    } catch (PropertyNotFoundException e) {
      interpreter.addError(new TemplateError(ErrorType.WARNING, ErrorReason.UNKNOWN, e.getMessage(), "", interpreter.getLineNumber(), e));
    } catch (TreeBuilderException e) {
      interpreter.addError(TemplateError.fromException(new TemplateSyntaxException(expr,
          "Error parsing '" + expr + "': " + StringUtils.substringAfter(e.getMessage(), "': "), interpreter.getLineNumber(), e)));
    } catch (ELException e) {
      interpreter.addError(TemplateError.fromException(new TemplateSyntaxException(expr, e.getMessage(), interpreter.getLineNumber(), e)));
    } catch (Exception e) {
      interpreter.addError(TemplateError.fromException(new InterpretException(
          String.format("Error resolving expression [%s]: " + getRootCauseMessage(e), expr), e, interpreter.getLineNumber())));
    }

    return "";
  }

  public Object resolveProperty(Object base, List<String> chain) {
    ELResolver resolver = elContext.getELResolver();

    Object value = base;
    for (String name : chain) {
      if (value == null) {
        return null;
      }

      value = resolver.getValue(elContext, value, name);
    }

    return value;
  }
}
