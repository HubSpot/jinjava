package com.hubspot.jinjava.el;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

import java.util.List;

import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.PropertyNotFoundException;
import javax.el.ValueExpression;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.el.ext.NamedParameter;
import com.hubspot.jinjava.interpret.DisabledException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorItem;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.interpret.UnknownTokenException;
import com.hubspot.jinjava.interpret.errorcategory.BasicTemplateErrorCategory;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;

import de.odysseus.el.tree.TreeBuilderException;

/**
 * Resolves Jinja expressions.
 */
public class ExpressionResolver {

  private final JinjavaInterpreter interpreter;
  private final ExpressionFactory expressionFactory;
  private final JinjavaInterpreterResolver resolver;
  private final JinjavaELContext elContext;

  public ExpressionResolver(JinjavaInterpreter interpreter, ExpressionFactory expressionFactory) {
    this.interpreter = interpreter;
    this.expressionFactory = expressionFactory;

    this.resolver = new JinjavaInterpreterResolver(interpreter);
    this.elContext = new JinjavaELContext(resolver);
    for (ELFunctionDefinition fn : interpreter.getContext().getAllFunctions()) {
      this.elContext.setFunction(fn.getNamespace(), fn.getLocalName(), fn.getMethod());
    }
  }

  /**
   * Resolve expression against current context.
   *
   * @param expression
   *          Jinja expression.
   * @return Value of expression.
   */
  public Object resolveExpression(String expression) {
    if (StringUtils.isBlank(expression)) {
      return "";
    }

    interpreter.getContext().addResolvedExpression(expression.trim());

    try {
      String elExpression = "#{" + expression.trim() + "}";
      ValueExpression valueExp = expressionFactory.createValueExpression(elContext, elExpression, Object.class);
      Object result = valueExp.getValue(elContext);

      validateResult(result);

      return result;

    } catch (PropertyNotFoundException e) {
      interpreter.addError(new TemplateError(ErrorType.WARNING, ErrorReason.UNKNOWN, ErrorItem.PROPERTY, e.getMessage(), "", interpreter.getLineNumber(), interpreter.getPosition(), e,
          BasicTemplateErrorCategory.UNKNOWN, ImmutableMap.of("exception", e.getMessage())));
    } catch (TreeBuilderException e) {
      interpreter.addError(TemplateError.fromException(new TemplateSyntaxException(expression,
          "Error parsing '" + expression + "': " + StringUtils.substringAfter(e.getMessage(), "': "), interpreter.getLineNumber(), interpreter.getPosition() + e.getPosition(), e)));
    } catch (ELException e) {
      interpreter.addError(TemplateError.fromException(new TemplateSyntaxException(expression, e.getMessage(), interpreter.getLineNumber(), e)));
    } catch (DisabledException e) {
      interpreter.addError(new TemplateError(ErrorType.FATAL, ErrorReason.DISABLED, ErrorItem.FUNCTION, e.getMessage(), expression, interpreter.getLineNumber(), interpreter.getPosition(), e));
    } catch (UnknownTokenException e) {
      // Re-throw the exception because you only get this when the config failOnUnknownTokens is enabled.
      throw e;
    } catch (Exception e) {
      interpreter.addError(TemplateError.fromException(new InterpretException(
          String.format("Error resolving expression [%s]: " + getRootCauseMessage(e), expression), e, interpreter.getLineNumber(), interpreter.getPosition())));
    }

    return "";
  }

  private void validateResult(Object result) {
    if (result instanceof NamedParameter) {
      throw new ELException("Unexpected '=' operator (use {% set %} tag for variable assignment)");
    }
  }

  /**
   * Resolve property of bean.
   *
   * @param object
   *          Bean.
   * @param propertyNames
   *          Names of properties to resolve recursively.
   * @return Value of property.
   */
  public Object resolveProperty(Object object, List<String> propertyNames) {
    // Always wrap base object.
    Object value = resolver.wrap(object);

    for (String propertyName : propertyNames) {
      if (value == null) {
        return null;
      }

      value = resolver.getValue(elContext, value, propertyName);
    }

    return value;
  }
}
