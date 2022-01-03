package com.hubspot.jinjava.el.tree;

/**
 * Basic node interface.
 *
 * @author Christoph Beck
 */
public interface Node {
    /**
     * Get the node's number of children.
     */
    int getCardinality();

    /**
     * Get i'th child
     */
    Node getChild(int i);
}
