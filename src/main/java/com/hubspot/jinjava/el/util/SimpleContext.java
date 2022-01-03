package com.hubspot.jinjava.el.util;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.FunctionMapper;
import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;

/**
 * Simple context implementation.
 *
 * @author Christoph Beck
 */
public class SimpleContext extends ELContext {
    static class Functions extends FunctionMapper {
        Map<String, Method> map = Collections.emptyMap();

        @Override
        public Method resolveFunction(String prefix, String localName) {
            return map.get(prefix + ":" + localName);
        }

        public void setFunction(String prefix, String localName, Method method) {
            if (map.isEmpty()) {
                map = new HashMap<String, Method>();
            }
            map.put(prefix + ":" + localName, method);
        }
    }

    static class Variables extends VariableMapper {
        Map<String, ValueExpression> map = Collections.emptyMap();

        @Override
        public ValueExpression resolveVariable(String variable) {
            return map.get(variable);
        }

        @Override
        public ValueExpression setVariable(String variable, ValueExpression expression) {
            if (map.isEmpty()) {
                map = new HashMap<String, ValueExpression>();
            }
            return map.put(variable, expression);
        }
    }

    private Functions functions;
    private Variables variables;
    private ELResolver resolver;

    /**
     * Create a context.
     */
    public SimpleContext() {
        this(null);
    }

    /**
     * Create a context, use the specified resolver.
     */
    public SimpleContext(ELResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Define a function.
     */
    public void setFunction(String prefix, String localName, Method method) {
        if (functions == null) {
            functions = new Functions();
        }
        functions.setFunction(prefix, localName, method);
    }

    /**
     * Define a variable.
     */
    public ValueExpression setVariable(String name, ValueExpression expression) {
        if (variables == null) {
            variables = new Variables();
        }
        return variables.setVariable(name, expression);
    }

    /**
     * Get our function mapper.
     */
    @Override
    public FunctionMapper getFunctionMapper() {
        if (functions == null) {
            functions = new Functions();
        }
        return functions;
    }

    /**
     * Get our variable mapper.
     */
    @Override
    public VariableMapper getVariableMapper() {
        if (variables == null) {
            variables = new Variables();
        }
        return variables;
    }

    /**
     * Get our resolver. Lazy initialize to a {@link SimpleResolver} if necessary.
     */
    @Override
    public ELResolver getELResolver() {
        if (resolver == null) {
            resolver = new SimpleResolver();
        }
        return resolver;
    }

    /**
     * Set our resolver.
     *
     * @param resolver
     */
    public void setELResolver(ELResolver resolver) {
        this.resolver = resolver;
    }
}
