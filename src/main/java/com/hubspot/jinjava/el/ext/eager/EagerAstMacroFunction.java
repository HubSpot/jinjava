package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AstMacroFunction;
import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.interpret.Context.TemporaryValueClosable;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstParameters;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.StringJoiner;
import jakarta.el.ELContext;
import jakarta.el.ELException;

public class EagerAstMacroFunction extends AstMacroFunction implements EvalResultHolder {
  protected Object evalResult;
  protected boolean hasEvalResult;
  // instanceof AstParameters
  protected EvalResultHolder params;
  protected boolean varargs;

  public EagerAstMacroFunction(
    String name,
    int index,
    AstParameters params,
    boolean varargs
  ) {
    this(name, index, EagerAstNodeDecorator.getAsEvalResultHolder(params), varargs);
  }

  private EagerAstMacroFunction(
    String name,
    int index,
    EvalResultHolder params,
    boolean varargs
  ) {
    super(name, index, (AstParameters) params, varargs);
    this.params = params;
    this.varargs = varargs;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    try {
      setEvalResult(super.eval(bindings, context));
      return checkEvalResultSize(context);
    } catch (DeferredValueException | ELException originalException) {
      DeferredParsingException e = EvalResultHolder.convertToDeferredParsingException(
        originalException
      );
      throw new DeferredParsingException(
        this,
        getPartiallyResolved(bindings, context, e, true) // Need this to always be true because the macro function may modify the identifier
      );
    }
  }

  @Override
  protected Object invoke(
    Bindings bindings,
    ELContext context,
    Object base,
    Method method
  )
    throws InvocationTargetException, IllegalAccessException {
    Class<?>[] types = method.getParameterTypes();
    Object[] params = null;
    if (types.length > 0) {
      // This is just the AstFunction.invoke, but surrounded with this try-with-resources
      try (
        TemporaryValueClosable<Boolean> c = (
          (JinjavaInterpreter) context
            .getELResolver()
            .getValue(context, null, ExtendedParser.INTERPRETER)
        ).getContext()
          .withPartialMacroEvaluation(false)
      ) {
        params = new Object[types.length];
        int varargIndex;
        Object param;
        if (this.varargs && method.isVarArgs()) {
          for (varargIndex = 0; varargIndex < params.length - 1; ++varargIndex) {
            param = this.getParam(varargIndex).eval(bindings, context);
            if (param != null || types[varargIndex].isPrimitive()) {
              params[varargIndex] = bindings.convert(param, types[varargIndex]);
            }
          }

          varargIndex = types.length - 1;
          Class<?> varargType = types[varargIndex].getComponentType();
          int length = this.getParamCount() - varargIndex;
          Object array = null;
          if (length == 1) {
            param = this.getParam(varargIndex).eval(bindings, context);
            if (param != null && param.getClass().isArray()) {
              if (types[varargIndex].isInstance(param)) {
                array = param;
              } else {
                length = Array.getLength(param);
                array = Array.newInstance(varargType, length);

                for (int i = 0; i < length; ++i) {
                  Object elem = Array.get(param, i);
                  if (elem != null || varargType.isPrimitive()) {
                    Array.set(array, i, bindings.convert(elem, varargType));
                  }
                }
              }
            } else {
              array = Array.newInstance(varargType, 1);
              if (param != null || varargType.isPrimitive()) {
                Array.set(array, 0, bindings.convert(param, varargType));
              }
            }
          } else {
            array = Array.newInstance(varargType, length);

            for (int i = 0; i < length; ++i) {
              param = this.getParam(varargIndex + i).eval(bindings, context);
              if (param != null || varargType.isPrimitive()) {
                Array.set(array, i, bindings.convert(param, varargType));
              }
            }
          }

          params[varargIndex] = array;
        } else {
          for (varargIndex = 0; varargIndex < params.length; ++varargIndex) {
            param = this.getParam(varargIndex).eval(bindings, context);
            if (param != null || types[varargIndex].isPrimitive()) {
              params[varargIndex] = bindings.convert(param, types[varargIndex]);
            }
          }
        }
      }
    }
    return method.invoke(base, params);
  }

  @Override
  public String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException deferredParsingException,
    boolean preserveIdentifier
  ) {
    if (
      deferredParsingException != null &&
      deferredParsingException.getSourceNode() instanceof MacroFunction
    ) {
      return deferredParsingException.getDeferredEvalResult();
    }
    StringBuilder sb = new StringBuilder();
    sb.append(getName());
    try {
      StringJoiner paramString = new StringJoiner(", ");
      for (int i = 0; i < ((AstParameters) params).getCardinality(); i++) {
        paramString.add(
          EvalResultHolder.reconstructNode(
            bindings,
            context,
            (EvalResultHolder) ((AstParameters) params).getChild(i),
            deferredParsingException,
            preserveIdentifier
          )
        );
      }
      sb.append(String.format("(%s)", paramString));
    } catch (DeferredParsingException dpe) {
      sb.append(String.format("(%s)", dpe.getDeferredEvalResult()));
    }
    return sb.toString();
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
}
