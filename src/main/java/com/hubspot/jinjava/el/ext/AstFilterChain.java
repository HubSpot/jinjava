package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.interpret.DisabledException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorItem;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.objects.SafeString;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstNode;
import de.odysseus.el.tree.impl.ast.AstParameters;
import de.odysseus.el.tree.impl.ast.AstRightValue;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.el.ELContext;
import javax.el.ELException;

/**
 * AST node for a chain of filters applied to an input expression.
 * Instead of creating nested AstMethod calls for each filter in a chain like:
 *   filter:length.filter(filter:lower.filter(filter:trim.filter(input)))
 *
 * This node represents the entire chain as a single evaluation unit:
 *   input|trim|lower|length
 *
 * This optimization reduces:
 * - Filter lookups (done once per filter instead of per AST node traversal)
 * - Method invocation overhead
 * - Object wrapping/unwrapping between filters
 * - Context operations
 */
public class AstFilterChain extends AstRightValue {

  protected final AstNode input;
  protected final List<FilterSpec> filterSpecs;

  public AstFilterChain(AstNode input, List<FilterSpec> filterSpecs) {
    this.input = Objects.requireNonNull(input, "Input node cannot be null");
    this.filterSpecs = Objects.requireNonNull(filterSpecs, "Filter specs cannot be null");
    if (filterSpecs.isEmpty()) {
      throw new IllegalArgumentException("Filter chain must have at least one filter");
    }
  }

  public AstNode getInput() {
    return input;
  }

  public List<FilterSpec> getFilterSpecs() {
    return filterSpecs;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    JinjavaInterpreter interpreter = getInterpreter(context);

    if (interpreter.getContext().isValidationMode()) {
      return "";
    }

    Object value = input.eval(bindings, context);

    for (FilterSpec spec : filterSpecs) {
      String filterKey = ExtendedParser.FILTER_PREFIX + spec.getName();
      interpreter.getContext().addResolvedValue(filterKey);

      Filter filter;
      try {
        filter = interpreter.getContext().getFilter(spec.getName());
      } catch (DisabledException e) {
        interpreter.addError(
          new TemplateError(
            ErrorType.FATAL,
            ErrorReason.DISABLED,
            ErrorItem.FILTER,
            e.getMessage(),
            spec.getName(),
            interpreter.getLineNumber(),
            -1,
            e
          )
        );
        return null;
      }
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

  protected JinjavaInterpreter getInterpreter(ELContext context) {
    return (JinjavaInterpreter) context
      .getELResolver()
      .getValue(context, null, ExtendedParser.INTERPRETER);
  }

  protected Object[] evaluateFilterArgs(
    FilterSpec spec,
    Bindings bindings,
    ELContext context
  ) {
    AstParameters params = spec.getParams();
    if (params == null || params.getCardinality() == 0) {
      return new Object[0];
    }

    Object[] args = new Object[params.getCardinality()];
    for (int i = 0; i < params.getCardinality(); i++) {
      args[i] = params.getChild(i).eval(bindings, context);
    }
    return args;
  }

  private Map<String, Object> extractNamedParams(Object[] args) {
    Map<String, Object> kwargs = new LinkedHashMap<>();
    for (Object arg : args) {
      if (arg instanceof NamedParameter) {
        NamedParameter namedParam = (NamedParameter) arg;
        kwargs.put(namedParam.getName(), namedParam.getValue());
      }
    }
    return kwargs;
  }

  private Object[] extractPositionalArgs(Object[] args) {
    List<Object> positional = new ArrayList<>();
    for (Object arg : args) {
      if (!(arg instanceof NamedParameter)) {
        positional.add(arg);
      }
    }
    return positional.toArray();
  }

  @Override
  public void appendStructure(StringBuilder builder, Bindings bindings) {
    input.appendStructure(builder, bindings);
    for (FilterSpec spec : filterSpecs) {
      builder.append('|').append(spec.getName());
      AstParameters params = spec.getParams();
      if (params != null && params.getCardinality() > 0) {
        builder.append('(');
        for (int i = 0; i < params.getCardinality(); i++) {
          if (i > 0) {
            builder.append(", ");
          }
          params.getChild(i).appendStructure(builder, bindings);
        }
        builder.append(')');
      }
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(input.toString());
    for (FilterSpec spec : filterSpecs) {
      sb.append('|').append(spec.toString());
    }
    return sb.toString();
  }

  @Override
  public int getCardinality() {
    return 1 + filterSpecs.size();
  }

  @Override
  public AstNode getChild(int i) {
    if (i == 0) {
      return input;
    }
    int filterIndex = i - 1;
    if (filterIndex < filterSpecs.size()) {
      FilterSpec spec = filterSpecs.get(filterIndex);
      return spec.getParams();
    }
    return null;
  }
}
