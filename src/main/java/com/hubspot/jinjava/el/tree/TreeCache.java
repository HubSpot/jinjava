package com.hubspot.jinjava.el.tree;

/**
 * Tree cache interface.
 * A tree cache holds expression trees by expression strings. A tree cache implementation
 * must be thread-safe.
 *
 * @author Christoph Beck
 */
public interface TreeCache {
    /**
     * Lookup tree
     */
    Tree get(String expression);

    /**
     * Cache tree
     */
    void put(String expression, Tree tree);
}
