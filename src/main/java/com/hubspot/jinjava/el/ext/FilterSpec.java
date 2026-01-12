package com.hubspot.jinjava.el.ext;

import de.odysseus.el.tree.impl.ast.AstParameters;
import java.util.Objects;

/**
 * Specification for a filter in a filter chain.
 * Holds the filter name and optional parameters.
 */
public class FilterSpec {

  private final String name;
  private final AstParameters params;

  public FilterSpec(String name, AstParameters params) {
    this.name = Objects.requireNonNull(name, "Filter name cannot be null");
    this.params = params;
  }

  public String getName() {
    return name;
  }

  public AstParameters getParams() {
    return params;
  }

  public boolean hasParams() {
    return params != null && params.getCardinality() > 0;
  }

  @Override
  public String toString() {
    if (hasParams()) {
      StringBuilder sb = new StringBuilder(name);
      sb.append('(');
      for (int i = 0; i < params.getCardinality(); i++) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(params.getChild(i));
      }
      sb.append(')');
      return sb.toString();
    }
    return name;
  }
}
