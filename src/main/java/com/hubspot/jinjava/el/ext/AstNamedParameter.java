package com.hubspot.jinjava.el.ext;

import javax.el.ELContext;

import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.impl.ast.AstIdentifier;
import de.odysseus.el.tree.impl.ast.AstLiteral;
import de.odysseus.el.tree.impl.ast.AstNode;

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
    throw new UnsupportedOperationException("appendStructure not implemented in " + getClass().getSimpleName());
  }

}
