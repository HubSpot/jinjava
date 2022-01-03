package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.el.tree.Bindings;
import com.hubspot.jinjava.el.tree.impl.ast.AstIdentifier;
import com.hubspot.jinjava.el.tree.impl.ast.AstLiteral;
import com.hubspot.jinjava.el.tree.impl.ast.AstNode;
import jakarta.el.ELContext;

public class AstNamedParameter extends AstLiteral {
  private final AstIdentifier name;
  private final AstNode value;

  public AstNamedParameter(AstIdentifier name, AstNode value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    return new NamedParameter(name.getName(), value.eval(bindings, context));
  }

  @Override
  public void appendStructure(StringBuilder builder, Bindings bindings) {
    throw new UnsupportedOperationException(
      "appendStructure not implemented in " + getClass().getSimpleName()
    );
  }
}
