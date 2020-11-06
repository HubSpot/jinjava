package com.hubspot.jinjava.el.ext.eager;

import de.odysseus.el.tree.Bindings;
import de.odysseus.el.tree.Node;
import de.odysseus.el.tree.impl.ast.AstNode;
import javax.el.ELContext;
import javax.el.MethodInfo;
import javax.el.ValueReference;

public class EagerAstNodeDecorator extends AstNode implements EvalResultHolder {
  private final AstNode astNode;
  private Object eagerResult;

  public static EvalResultHolder getAsEvalResultHolder(AstNode astNode) {
    if (astNode instanceof EvalResultHolder) {
      return (EvalResultHolder) astNode;
    }
    return new EagerAstNodeDecorator(astNode);
  }

  private EagerAstNodeDecorator(AstNode astNode) {
    this.astNode = astNode;
  }

  @Override
  public Object getEvalResult() {
    return eagerResult;
  }

  @Override
  public void appendStructure(StringBuilder stringBuilder, Bindings bindings) {
    astNode.appendStructure(stringBuilder, bindings);
  }

  @Override
  public Object eval(Bindings bindings, ELContext elContext) {
    if (eagerResult != null) {
      return eagerResult;
    }
    eagerResult = astNode.eval(bindings, elContext);
    return eagerResult;
  }

  @Override
  public boolean isLiteralText() {
    return astNode.isLiteralText();
  }

  @Override
  public boolean isLeftValue() {
    return astNode.isLeftValue();
  }

  @Override
  public boolean isMethodInvocation() {
    return astNode.isMethodInvocation();
  }

  @Override
  public ValueReference getValueReference(Bindings bindings, ELContext elContext) {
    return astNode.getValueReference(bindings, elContext);
  }

  @Override
  public Class<?> getType(Bindings bindings, ELContext elContext) {
    return astNode.getType(bindings, elContext);
  }

  @Override
  public boolean isReadOnly(Bindings bindings, ELContext elContext) {
    return astNode.isReadOnly(bindings, elContext);
  }

  @Override
  public void setValue(Bindings bindings, ELContext elContext, Object o) {
    astNode.setValue(bindings, elContext, o);
  }

  @Override
  public MethodInfo getMethodInfo(
    Bindings bindings,
    ELContext elContext,
    Class<?> aClass,
    Class<?>[] classes
  ) {
    return astNode.getMethodInfo(bindings, elContext, aClass, classes);
  }

  @Override
  public Object invoke(
    Bindings bindings,
    ELContext elContext,
    Class<?> aClass,
    Class<?>[] classes,
    Object[] objects
  ) {
    if (eagerResult != null) {
      return eagerResult;
    }
    eagerResult = astNode.invoke(bindings, elContext, aClass, classes, objects);
    return eagerResult;
  }

  @Override
  public int getCardinality() {
    return astNode.getCardinality();
  }

  @Override
  public Node getChild(int i) {
    return astNode.getChild(i);
  }
}
