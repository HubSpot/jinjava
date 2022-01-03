package com.hubspot.jinjava.el.tree;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;

import com.hubspot.jinjava.el.misc.TypeConverter;
import jakarta.el.ELException;
import jakarta.el.FunctionMapper;
import jakarta.el.ValueExpression;
import jakarta.el.VariableMapper;

/**
 * Parsed expression, usually created by a {@link TreeBuilder}.
 * The {@link #bind(FunctionMapper, VariableMapper)} method is used to create
 * {@link Bindings}, which are needed at evaluation time to
 * lookup functions and variables. The tree itself does not contain such information,
 * because it would make the tree depend on the function/variable mapper supplied at
 * parse time.
 *
 * @author Christoph Beck
 */
public class Tree {
    private final ExpressionNode root;
    private final List<FunctionNode> functions;
    private final List<IdentifierNode> identifiers;
    private final boolean deferred;

    private static final String ERROR_FUNCTION_PARAMS = "Parameters for function ''{0}'' do not match";

    /**
     *
     * Constructor.
     * @param root root node
     * @param functions collection of function nodes
     * @param identifiers collection of identifier nodes
     */
    public Tree(ExpressionNode root, List<FunctionNode> functions, List<IdentifierNode> identifiers, boolean deferred) {
        super();
        this.root = root;
        this.functions = functions;
        this.identifiers = identifiers;
        this.deferred = deferred;
    }

    /**
     * Get function nodes (in no particular order)
     */
    public Iterable<FunctionNode> getFunctionNodes() {
        return functions;
    }

    /**
     * Get identifier nodes (in no particular order)
     */
    public Iterable<IdentifierNode> getIdentifierNodes() {
        return identifiers;
    }

    /**
     * @return root node
     */
    public ExpressionNode getRoot() {
        return root;
    }

    public boolean isDeferred() {
        return deferred;
    }

    @Override
    public String toString() {
        return getRoot().getStructuralId(null);
    }

    /**
     * Create a bindings.
     * @param fnMapper the function mapper to use
     * @param varMapper the variable mapper to use
     * @return tree bindings
     */
    public Bindings bind(FunctionMapper fnMapper, VariableMapper varMapper) {
        return bind(fnMapper, varMapper, null);
    }

    /**
     * Create a bindings.
     * @param fnMapper the function mapper to use
     * @param varMapper the variable mapper to use
     * @param converter custom type converter
     * @return tree bindings
     */
    public Bindings bind(FunctionMapper fnMapper, VariableMapper varMapper, TypeConverter converter) {
        Method[] methods = null;
        if (!functions.isEmpty()) {
            if (fnMapper == null) {
                throw new ELException("Expression uses functions, but no function mapper was provided");
            }
            methods = new Method[functions.size()];
            for (FunctionNode node : functions) {
                String image = node.getName();
                Method method;
                int colon = image.indexOf(':');
                if (colon < 0) {
                    method = fnMapper.resolveFunction("", image);
                } else {
                    method = fnMapper.resolveFunction(image.substring(0, colon), image.substring(colon + 1));
                }
                if (method == null) {
                    throw new ELException("Could not resolve function '" + image + "'");
                }
                if (node.isVarArgs() && method.isVarArgs()) {
                    if (method.getParameterTypes().length > node.getParamCount() + 1) {
                        throw new ELException(MessageFormat.format(ERROR_FUNCTION_PARAMS, image));
                    }
                } else {
                    if (method.getParameterTypes().length != node.getParamCount()) {
                        throw new ELException(MessageFormat.format(ERROR_FUNCTION_PARAMS, image));
                    }
                }
                methods[node.getIndex()] = method;
            }
        }
        ValueExpression[] expressions = null;
        if (identifiers.size() > 0) {
            expressions = new ValueExpression[identifiers.size()];
            for (IdentifierNode node : identifiers) {
                ValueExpression expression = null;
                if (varMapper != null) {
                    expression = varMapper.resolveVariable(node.getName());
                }
                expressions[node.getIndex()] = expression;
            }
        }
        return new Bindings(methods, expressions, converter);
    }
}
