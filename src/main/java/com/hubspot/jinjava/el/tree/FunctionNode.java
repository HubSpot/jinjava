package com.hubspot.jinjava.el.tree;

/**
 * Function node interface.
 *
 * @author Christoph Beck
 */
public interface FunctionNode extends Node {
    /**
     * Get the full function name
     */
    String getName();

    /**
     * Get the unique index of this identifier in the expression (e.g. preorder index)
     */
    int getIndex();

    /**
     * Get the number of parameters for this function
     */
    int getParamCount();

    /**
     * @return <code>true</code> if this node supports varargs.
     */
    boolean isVarArgs();
}
