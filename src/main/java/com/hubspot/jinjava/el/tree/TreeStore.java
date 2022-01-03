package com.hubspot.jinjava.el.tree;

/**
 * Tree store class.
 * A tree store holds a {@link TreeBuilder} and a
 * {@link TreeCache}, provided at construction time.
 * The <code>get(String)</code> method is then used to serve expression trees.
 *
 * @author Christoph Beck
 */
public class TreeStore {
    private final TreeCache cache;
    private final TreeBuilder builder;

    /**
     * Constructor.
     * @param builder the tree builder
     * @param cache the tree cache (may be <code>null</code>)
     */
    public TreeStore(TreeBuilder builder, TreeCache cache) {
        super();

        this.builder = builder;
        this.cache = cache;
    }

    public TreeBuilder getBuilder() {
        return builder;
    }

    /**
     * Get a {@link Tree}.
     * If a tree for the given expression is present in the cache, it is
     * taken from there; otherwise, the expression string is parsed and
     * the resulting tree is added to the cache.
     * @param expression expression string
     * @return expression tree
     */
    public Tree get(String expression) throws TreeBuilderException {
        if (cache == null) {
            return builder.build(expression);
        }
        Tree tree = cache.get(expression);
        if (tree == null) {
            cache.put(expression, tree = builder.build(expression));
        }
        return tree;
    }
}
