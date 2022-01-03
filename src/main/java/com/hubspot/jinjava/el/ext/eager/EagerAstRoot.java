package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.tree.impl.ast.AstNode;
import com.hubspot.jinjava.el.tree.Bindings;
import com.hubspot.jinjava.el.tree.Node;
import jakarta.el.ELContext;
import jakarta.el.MethodInfo;
import jakarta.el.ValueReference;

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
    try {
      return rootNode.eval(bindings, context);
    } finally {
      if (rootNode instanceof EvalResultHolder) {
        ((EvalResultHolder) rootNode).clearEvalResult();
      }
    }
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
