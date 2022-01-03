package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import com.hubspot.jinjava.el.tree.Bindings;
import com.hubspot.jinjava.el.tree.Node;
import com.hubspot.jinjava.el.tree.impl.ast.AstNode;
import jakarta.el.ELContext;
import jakarta.el.MethodInfo;
import jakarta.el.ValueReference;
import java.util.function.Supplier;

/**
 * This decorator exists to ensure that every EvalResultHolder is an
 * instanceof AstNode. When using eager parsing, every AstNode should either
 * be an EvalResultHolder or wrapped with this decorator.
 */
public class EagerAstNodeDecorator extends AstNode implements EvalResultHolder {
  private final AstNode astNode;
  protected Object evalResult;
  protected boolean hasEvalResult;

  public static EvalResultHolder getAsEvalResultHolder(AstNode astNode) {
    if (astNode instanceof EvalResultHolder) {
      return (EvalResultHolder) astNode;
    }
    if (astNode != null) { // Wraps nodes such as AstString, AstNumber
      return new EagerAstNodeDecorator(astNode);
    }
    return null;
  }

  private EagerAstNodeDecorator(AstNode astNode) {
    this.astNode = astNode;
  }

  @Override
  public Object getEvalResult() {
    return evalResult;
  }

  @Override
  public void setEvalResult(Object evalResult) {
    this.evalResult = evalResult;
    hasEvalResult = true;
  }

  @Override
  public void clearEvalResult() {
    evalResult = null;
    hasEvalResult = false;
  }

  @Override
  public boolean hasEvalResult() {
    return hasEvalResult;
  }

  @Override
  public Object eval(Supplier<Object> evalSupplier, Bindings bindings, ELContext context) {
    return EvalResultHolder.super.eval(evalSupplier, bindings, context);
  }

  @Override
  public void appendStructure(StringBuilder stringBuilder, Bindings bindings) {
    astNode.appendStructure(stringBuilder, bindings);
  }

  @Override
  public Object eval(Bindings bindings, ELContext elContext) {
    setEvalResult(astNode.eval(bindings, elContext));
    return evalResult;
  }

  @Override
  public String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException deferredParsingException,
    boolean preserveIdentifier
  ) {
    return null;
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
    if (hasEvalResult) {
      return evalResult;
    }
    evalResult = astNode.invoke(bindings, elContext, aClass, classes, objects);
    return evalResult;
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
