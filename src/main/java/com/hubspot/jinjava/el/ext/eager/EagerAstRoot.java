package com.hubspot.jinjava.el.ext.eager;

import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.Node;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELContext;
import javax.el.MethodInfo;
import javax.el.ValueReference;

public class EagerAstRoot extends AstNode {

  private AstNode rootNode;

  public EagerAstRoot(AstNode rootNode) {
    this.rootNode = rootNode;
  }

  @Override
  public void appendStructure(StringBuilder builder, Bindings bindings) {
    rootNode.appendStructure(builder, bindings);
  }

  @Override
  public Object eval(Bindings bindings, ELContext context) {
    return rootNode.eval(bindings, context);
  }

  @Override
  public boolean isLiteralText() {
    return rootNode.isLiteralText();
  }

  @Override
  public boolean isLeftValue() {
    return rootNode.isLeftValue();
  }

  @Override
  public boolean isMethodInvocation() {
    return rootNode.isMethodInvocation();
  }

  @Override
  public ValueReference getValueReference(Bindings bindings, ELContext context) {
    return rootNode.getValueReference(bindings, context);
  }

  @Override
  public Class<?> getType(Bindings bindings, ELContext context) {
    return rootNode.getType(bindings, context);
  }

  @Override
  public boolean isReadOnly(Bindings bindings, ELContext context) {
    return rootNode.isReadOnly(bindings, context);
  }

  @Override
  public void setValue(Bindings bindings, ELContext context, Object value) {
    rootNode.setValue(bindings, context, value);
  }

  @Override
  public MethodInfo getMethodInfo(
    Bindings bindings,
    ELContext context,
    Class<?> returnType,
    Class<?>[] paramTypes
  ) {
    return rootNode.getMethodInfo(bindings, context, returnType, paramTypes);
  }

  @Override
  public Object invoke(
    Bindings bindings,
    ELContext context,
    Class<?> returnType,
    Class<?>[] paramTypes,
    Object[] paramValues
  ) {
    return rootNode.invoke(bindings, context, returnType, paramTypes, paramValues);
  }

  @Override
  public int getCardinality() {
    return rootNode.getCardinality();
  }

  @Override
  public Node getChild(int i) {
    return rootNode.getChild(i);
  }
}
