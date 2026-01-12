package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstFilterChain;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.FilterSpec;
import com.hubspot.jinjava.el.ext.IdentifierPreservationStrategy;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.objects.SafeString;
import com.hubspot.jinjava.util.EagerExpressionResolver;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstNode;
import de.odysseus.el.tree.impl.ast.AstParameters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.el.ELContext;
import javax.el.ELException;

/**
 * Eager variant of AstFilterChain that supports deferred value resolution.
 * When a deferred value is encountered during filter chain evaluation,
 * this class reconstructs the partially resolved expression.
 */
public class EagerAstFilterChain extends AstFilterChain implements EvalResultHolder {

  protected Object evalResult;
  protected boolean hasEvalResult;
  protected final EvalResultHolder inputHolder;
  protected final List<EvalResultHolder> paramHolders;

  public EagerAstFilterChain(AstNode input, List<FilterSpec> filterSpecs) {
    super(
      (AstNode) EagerAstNodeDecorator.getAsEvalResultHolder(input),
      wrapFilterSpecs(filterSpecs)
    );
    this.inputHolder = (EvalResultHolder) this.input;
    this.paramHolders = extractParamHolders(this.filterSpecs);
  }

  private static List<FilterSpec> wrapFilterSpecs(List<FilterSpec> specs) {
    List<FilterSpec> wrapped = new ArrayList<>(specs.size());
    for (FilterSpec spec : specs) {
      AstParameters params = spec.getParams();
      if (params != null) {
        params = (AstParameters) EagerAstNodeDecorator.getAsEvalResultHolder(params);
      }
      wrapped.add(new FilterSpec(spec.getName(), params));
    }
    return wrapped;
  }

  private static List<EvalResultHolder> extractParamHolders(List<FilterSpec> specs) {
    List<EvalResultHolder> holders = new ArrayList<>();
    for (FilterSpec spec : specs) {
      if (spec.getParams() instanceof EvalResultHolder) {
        holders.add((EvalResultHolder) spec.getParams());
      }
    }
    return holders;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      setEvalResult(evalFilterChain(bindings, context));
      return checkEvalResultSize(context);
    } catch (DeferredValueException | ELException originalException) {
      DeferredParsingException e = EvalResultHolder.convertToDeferredParsingException(
        originalException
      );
      throw new DeferredParsingException(
        this,
        getPartiallyResolved(
          bindings,
          context,
          e,
          IdentifierPreservationStrategy.PRESERVING
        ),
        IdentifierPreservationStrategy.PRESERVING
      );
    }
  }

  /**
   * Evaluates the filter chain, keeping track of which filters have been applied
   * to support partial resolution when deferred values are encountered.
   */
  protected Object evalFilterChain(Bindings bindings, ELContext context) {
    JinjavaInterpreter interpreter = getInterpreter(context);

    if (interpreter.getContext().isValidationMode()) {
      return "";
    }

    Object value = input.eval(bindings, context);

    for (FilterSpec spec : filterSpecs) {
      Filter filter = interpreter.getContext().getFilter(spec.getName());
      if (filter == null) {
        continue;
      }

      Object[] args = evaluateFilterArgs(spec, bindings, context);
      Map<String, Object> kwargs = extractNamedParams(args);
      Object[] positionalArgs = extractPositionalArgs(args);

      boolean wasSafeString = value instanceof SafeString;
      if (wasSafeString) {
        value = value.toString();
      }

      try {
        value = filter.filter(value, interpreter, positionalArgs, kwargs);
      } catch (ELException e) {
        throw e;
      } catch (DeferredValueException e) {
        throw e;
      } catch (RuntimeException e) {
        throw new ELException(
          String.format("Error in filter '%s': %s", spec.getName(), e.getMessage()),
          e
        );
      }

      if (wasSafeString && filter.preserveSafeString() && value instanceof String) {
        value = new SafeString((String) value);
      }
    }

    return value;
  }

  private Map<String, Object> extractNamedParams(Object[] args) {
    java.util.Map<String, Object> kwargs = new java.util.LinkedHashMap<>();
    for (Object arg : args) {
      if (arg instanceof com.hubspot.jinjava.el.ext.NamedParameter) {
        com.hubspot.jinjava.el.ext.NamedParameter namedParam =
          (com.hubspot.jinjava.el.ext.NamedParameter) arg;
        kwargs.put(namedParam.getName(), namedParam.getValue());
      }
    }
    return kwargs;
  }

  private Object[] extractPositionalArgs(Object[] args) {
    java.util.List<Object> positional = new java.util.ArrayList<>();
    for (Object arg : args) {
      if (!(arg instanceof com.hubspot.jinjava.el.ext.NamedParameter)) {
        positional.add(arg);
      }
    }
    return positional.toArray();
  }

  @Override
  public Object getEvalResult() {
    return evalResult;
  }

  @Override
  public void setEvalResult(Object evalResult) {
    this.evalResult = evalResult;
    hasEvalResult = true;
  }

  @Override
  public boolean hasEvalResult() {
    return hasEvalResult;
  }

  @Override
  public String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException deferredParsingException,
    IdentifierPreservationStrategy identifierPreservationStrategy
  ) {
    StringBuilder sb = new StringBuilder();

    String inputResult = EvalResultHolder.reconstructNode(
      bindings,
      context,
      inputHolder,
      deferredParsingException,
      identifierPreservationStrategy
    );
    sb.append(inputResult);

    for (FilterSpec spec : filterSpecs) {
      sb.append('|').append(spec.getName());
      AstParameters params = spec.getParams();
      if (params != null && params.getCardinality() > 0) {
        sb.append('(');
        for (int i = 0; i < params.getCardinality(); i++) {
          if (i > 0) {
            sb.append(", ");
          }
          AstNode paramNode = params.getChild(i);
          if (paramNode instanceof EvalResultHolder) {
            String paramResult = EvalResultHolder.reconstructNode(
              bindings,
              context,
              (EvalResultHolder) paramNode,
              deferredParsingException,
              identifierPreservationStrategy
            );
            sb.append(paramResult);
          } else {
            try {
              Object paramValue = paramNode.eval(bindings, context);
              sb.append(EagerExpressionResolver.getValueAsJinjavaStringSafe(paramValue));
            } catch (DeferredValueException e) {
              sb.append(paramNode.toString());
            }
          }
        }
        sb.append(')');
      }
    }

    return sb.toString();
  }
}
