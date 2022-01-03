package com.hubspot.jinjava.el.tree;

/**
 * Identifier node interface.
 *
 * @author Christoph Beck
 */
public interface IdentifierNode extends Node {
    /**
     * Get the identifier name
     */
    String getName();

    /**
     * Get the unique index of this identifier in the expression (e.g. preorder index)
     */
    int getIndex();
}
