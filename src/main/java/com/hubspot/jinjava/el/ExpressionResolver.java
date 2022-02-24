package com.hubspot.jinjava.el;

import static org.apache.commons.lang3.exception.ExceptionUtils.getRootCauseMessage;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.el.ext.NamedParameter;
import com.hubspot.jinjava.el.tree.TreeBuilderException;
import com.hubspot.jinjava.interpret.CollectionTooBigException;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.DisabledException;
import com.hubspot.jinjava.interpret.IndexOutOfRangeException;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.LazyExpression;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorItem;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.interpret.UnknownTokenException;
import com.hubspot.jinjava.interpret.errorcategory.BasicTemplateErrorCategory;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.hubspot.jinjava.util.WhitespaceUtils;
import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.ValueExpression;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves Jinja expressions.
 */
public class ExpressionResolver {
  private static final Logger LOG = LoggerFactory.getLogger(ExpressionResolver.class);

  private final JinjavaInterpreter interpreter;
  private final ExpressionFactory expressionFactory;
  private final JinjavaInterpreterResolver resolver;
  private final JinjavaELContext elContext;

  private static final String EXPRESSION_START_TOKEN = "#{";
  private static final String EXPRESSION_END_TOKEN = "}";

  public ExpressionResolver(JinjavaInterpreter interpreter, Jinjava jinjava) {
    this.interpreter = interpreter;
    this.expressionFactory =
      interpreter.getConfig().getExecutionMode().useEagerParser()
        ? jinjava.getEagerExpressionFactory()
        : jinjava.getExpressionFactory();

    this.resolver = new JinjavaInterpreterResolver(interpreter);
    this.elContext = new JinjavaELContext(interpreter, resolver);
    for (ELFunctionDefinition fn : jinjava.getGlobalContext().getAllFunctions()) {
      this.elContext.setFunction(fn.getNamespace(), fn.getLocalName(), fn.getMethod());
    }
  }

  /**
   * Resolve expression against current context.
   *
   * @param expression Jinja expression.
   * @return Value of expression.
   */
  public Object resolveExpression(String expression) {
    if (StringUtils.isBlank(expression)) {
      return null;
    }
    expression = expression.trim();
    interpreter.getContext().addResolvedExpression(expression);

    if (WhitespaceUtils.isWrappedWith(expression, "[", "]")) {
      Arrays
        .stream(expression.substring(1, expression.length() - 1).split(","))
        .forEach(
          substring -> interpreter.getContext().addResolvedExpression(substring.trim())
        );
    }
    try {
      String elExpression = EXPRESSION_START_TOKEN + expression + EXPRESSION_END_TOKEN;
      ValueExpression valueExp = expressionFactory.createValueExpression(
        elContext,
        elExpression,
        Object.class
      );
      LOG.debug("EL expression is: " + elExpression);
      Object result = valueExp.getValue(elContext);
      if (result == null && interpreter.getConfig().isFailOnUnknownTokens()) {
        throw new UnknownTokenException(
          expression,
          interpreter.getLineNumber(),
          interpreter.getPosition()
        );
      }

      // resolve the LazyExpression supplier automatically
      if (result instanceof LazyExpression) {
        result = ((LazyExpression) result).get();
      }

      validateResult(result);

      return result;
    } catch (PropertyNotFoundException e) {
      interpreter.addError(
        new TemplateError(
          ErrorType.WARNING,
          ErrorReason.UNKNOWN,
          ErrorItem.PROPERTY,
          e.getMessage(),
          "",
          interpreter.getLineNumber(),
          interpreter.getPosition(),
          e,
          BasicTemplateErrorCategory.UNKNOWN,
          ImmutableMap.of("exception", e.getMessage())
        )
      );
    } catch (TreeBuilderException e) {
      int position = interpreter.getPosition() + e.getPosition();
      // replacing the position in the string like this isn't great, but JUEL's parser does not allow passing in a starting position
      String errorMessage = StringUtils
        .substringAfter(e.getMessage(), "': ")
        .replaceFirst("position [0-9]+", "position " + position);
      interpreter.addError(
        TemplateError.fromException(
          new TemplateSyntaxException(
            expression.substring(e.getPosition() - EXPRESSION_START_TOKEN.length()),
            "Error parsing '" + expression + "': " + errorMessage,
            interpreter.getLineNumber(),
            position,
            e
          )
        )
      );
    } catch (ELException e) {
      if (e.getCause() != null && e.getCause() instanceof DeferredValueException) {
        throw (DeferredValueException) e.getCause();
      }
      if (e.getCause() != null && e.getCause() instanceof TemplateSyntaxException) {
        interpreter.addError(
          TemplateError.fromException((TemplateSyntaxException) e.getCause())
        );
      } else if (e.getCause() != null && e.getCause() instanceof InvalidInputException) {
        interpreter.addError(
          TemplateError.fromInvalidInputException((InvalidInputException) e.getCause())
        );
      } else if (
        e.getCause() != null && e.getCause() instanceof InvalidArgumentException
      ) {
        interpreter.addError(
          TemplateError.fromInvalidArgumentException(
            (InvalidArgumentException) e.getCause()
          )
        );
      } else if (
        e.getCause() != null && e.getCause() instanceof CollectionTooBigException
      ) {
        interpreter.addError(
          new TemplateError(
            ErrorType.FATAL,
            ErrorReason.COLLECTION_TOO_BIG,
            e.getCause().getMessage(),
            null,
            interpreter.getLineNumber(),
            interpreter.getPosition(),
            e
          )
        );
        // rethrow because this is a hard limit and it will likely only happen in loops that we need to terminate
        throw e;
      } else if (
        e.getCause() != null && e.getCause() instanceof IndexOutOfRangeException
      ) {
        interpreter.addError(
          new TemplateError(
            ErrorType.WARNING,
            ErrorReason.EXCEPTION,
            ErrorItem.FUNCTION,
            e.getMessage(),
            null,
            interpreter.getLineNumber(),
            interpreter.getPosition(),
            e
          )
        );
      } else {
        String originatingException = getRootCauseMessage(e);
        final String combinedMessage = String.format(
          "%s%nOriginating Exception:%n%s",
          e.getMessage(),
          originatingException
        );
        interpreter.addError(
          TemplateError.fromException(
            new TemplateSyntaxException(
              expression,
              (
                  e.getCause() == null ||
                  StringUtils.endsWith(originatingException, e.getCause().getMessage())
                )
                ? e.getMessage()
                : combinedMessage,
              interpreter.getLineNumber(),
              e
            )
          )
        );
      }
    } catch (DisabledException e) {
      interpreter.addError(
        new TemplateError(
          ErrorType.FATAL,
          ErrorReason.DISABLED,
          ErrorItem.FUNCTION,
          e.getMessage(),
          expression,
          interpreter.getLineNumber(),
          interpreter.getPosition(),
          e
        )
      );
    } catch (UnknownTokenException | DeferredValueException e) {
      // Re-throw the exception because you only get this when the config failOnUnknownTokens is enabled.
      throw e;
    } catch (InvalidInputException e) { // Re-throw so that it can be handled in JinjavaInterpreter
      interpreter.addError(TemplateError.fromInvalidInputException(e));
    } catch (InvalidArgumentException e) {
      interpreter.addError(TemplateError.fromInvalidArgumentException(e));
    } catch (Exception e) {
      LOG.error("Error during expression resolving", e);
      interpreter.addError(
        TemplateError.fromException(
          new InterpretException(
            String.format(
              "Error resolving expression [%s]: " + getRootCauseMessage(e),
              expression
            ),
            e,
            interpreter.getLineNumber(),
            interpreter.getPosition()
          )
        )
      );
    }

    return null;
  }

  private void validateResult(Object result) {
    if (result instanceof NamedParameter) {
      throw new ELException(
        "Unexpected '=' operator (use {% set %} tag for variable assignment)"
      );
    }
  }

  /**
   * Resolve property of bean.
   *
   * @param object        Bean.
   * @param propertyNames Names of properties to resolve recursively.
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

  /**
   * Wrap an object in it's PyIsh equivalent
   *
   * @param object Bean.
   * @return Wrapped bean.
   */
  public Object wrap(Object object) {
    return resolver.wrap(object);
  }
}
