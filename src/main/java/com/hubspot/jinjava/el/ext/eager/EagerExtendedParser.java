package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.AbsOperator;
import com.hubspot.jinjava.el.ext.AstDict;
import com.hubspot.jinjava.el.ext.AstList;
import com.hubspot.jinjava.el.ext.AstRangeBracket;
import com.hubspot.jinjava.el.ext.AstTuple;
import com.hubspot.jinjava.el.ext.CollectionMembershipOperator;
import com.hubspot.jinjava.el.ext.CollectionNonMembershipOperator;
import com.hubspot.jinjava.el.ext.ExtendedParser;
import com.hubspot.jinjava.el.ext.NamedParameterOperator;
import com.hubspot.jinjava.el.ext.PowerOfOperator;
import com.hubspot.jinjava.el.ext.StringConcatOperator;
import com.hubspot.jinjava.el.ext.TruncDivOperator;
import com.hubspot.jinjava.el.tree.impl.Builder;
import com.hubspot.jinjava.el.tree.impl.Builder.Feature;
import com.hubspot.jinjava.el.tree.impl.ast.AstBinary;
import com.hubspot.jinjava.el.tree.impl.ast.AstBinary.Operator;
import com.hubspot.jinjava.el.tree.impl.ast.AstBracket;
import com.hubspot.jinjava.el.tree.impl.ast.AstChoice;
import com.hubspot.jinjava.el.tree.impl.ast.AstDot;
import com.hubspot.jinjava.el.tree.impl.ast.AstFunction;
import com.hubspot.jinjava.el.tree.impl.ast.AstIdentifier;
import com.hubspot.jinjava.el.tree.impl.ast.AstMethod;
import com.hubspot.jinjava.el.tree.impl.ast.AstNode;
import com.hubspot.jinjava.el.tree.impl.ast.AstParameters;
import com.hubspot.jinjava.el.tree.impl.ast.AstProperty;
import com.hubspot.jinjava.el.tree.impl.ast.AstRightValue;
import com.hubspot.jinjava.el.tree.impl.ast.AstUnary;
import java.util.List;
import java.util.Map;

public class EagerExtendedParser extends ExtendedParser {

  public EagerExtendedParser(Builder context, String input) {
    super(context, input);
    putExtensionHandler(AbsOperator.TOKEN, AbsOperator.getHandler(true));
    putExtensionHandler(
      NamedParameterOperator.TOKEN,
      NamedParameterOperator.getHandler(true)
    );
    putExtensionHandler(
      StringConcatOperator.TOKEN,
      StringConcatOperator.getHandler(true)
    );
    putExtensionHandler(TruncDivOperator.TOKEN, TruncDivOperator.getHandler(true));
    putExtensionHandler(PowerOfOperator.TOKEN, PowerOfOperator.getHandler(true));

    putExtensionHandler(
      CollectionMembershipOperator.TOKEN,
      CollectionMembershipOperator.EAGER_HANDLER
    );

    putExtensionHandler(
      CollectionNonMembershipOperator.TOKEN,
      CollectionNonMembershipOperator.EAGER_HANDLER
    );
  }

  @Override
  protected AstBinary createAstBinary(AstNode left, AstNode right, Operator operator) {
    return new EagerAstBinary(left, right, operator);
  }

  @Override
  protected AstBracket createAstBracket(
    AstNode base,
    AstNode property,
    boolean lvalue,
    boolean strict
  ) {
    return new EagerAstBracket(
      base,
      property,
      lvalue,
      strict,
      this.context.isEnabled(Feature.IGNORE_RETURN_TYPE)
    );
  }

  @Override
  protected AstFunction createAstFunction(String name, int index, AstParameters params) {
    return new EagerAstMacroFunction(
      name,
      index,
      params,
      context.isEnabled(Feature.VARARGS)
    );
  }

  @Override
  protected AstChoice createAstChoice(AstNode question, AstNode yes, AstNode no) {
    return new EagerAstChoice(question, yes, no);
  }

  //  @Override
  //  protected AstComposite createAstComposite(List<AstNode> nodes) {
  //    return new AstComposite(nodes);
  //  }

  @Override
  protected AstDot createAstDot(AstNode base, String property, boolean lvalue) {
    return new EagerAstDot(
      base,
      property,
      lvalue,
      this.context.isEnabled(Feature.IGNORE_RETURN_TYPE)
    );
  }

  @Override
  protected AstIdentifier createAstIdentifier(String name, int index) {
    return new EagerAstIdentifier(
      name,
      index,
      this.context.isEnabled(Feature.IGNORE_RETURN_TYPE)
    );
  }

  @Override
  protected AstMethod createAstMethod(AstProperty property, AstParameters params) {
    return new EagerAstMethod(property, params);
  }

  @Override
  protected AstUnary createAstUnary(
    AstNode child,
    com.hubspot.jinjava.el.tree.impl.ast.AstUnary.Operator operator
  ) {
    return new EagerAstUnary(child, operator);
  }

  @Override
  protected AstRangeBracket createAstRangeBracket(
    AstNode base,
    AstNode rangeStart,
    AstNode rangeMax,
    boolean lvalue,
    boolean strict
  ) {
    return new EagerAstRangeBracket(
      base,
      rangeStart,
      rangeMax,
      lvalue,
      strict,
      context.isEnabled(Feature.IGNORE_RETURN_TYPE)
    );
  }

  @Override
  protected AstDict createAstDict(Map<AstNode, AstNode> dict) {
    return new EagerAstDict(dict);
  }

  @Override
  protected AstRightValue createAstNested(AstNode node) {
    return new EagerAstNested(
      (AstNode) EagerAstNodeDecorator.getAsEvalResultHolder(node)
    );
  }

  @Override
  protected AstTuple createAstTuple(AstParameters parameters) {
    return new EagerAstTuple(parameters);
  }

  @Override
  protected AstList createAstList(AstParameters parameters) {
    return new EagerAstList(parameters);
  }

  @Override
  protected AstParameters createAstParameters(List<AstNode> nodes) {
    return new EagerAstParameters(nodes);
  }
}
