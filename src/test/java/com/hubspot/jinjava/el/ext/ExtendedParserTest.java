package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.hubspot.jinjava.el.tree.impl.Builder;
import com.hubspot.jinjava.el.tree.impl.ast.AstBinary;
import com.hubspot.jinjava.el.tree.impl.ast.AstIdentifier;
import com.hubspot.jinjava.el.tree.impl.ast.AstMethod;
import com.hubspot.jinjava.el.tree.impl.ast.AstNested;
import com.hubspot.jinjava.el.tree.impl.ast.AstNode;
import com.hubspot.jinjava.el.tree.impl.ast.AstParameters;
import com.hubspot.jinjava.el.tree.impl.ast.AstString;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ExtendedParserTest {

  @Test
  public void itParseSingleBinaryEqualCondition() {
    AstNode astNode = buildExpressionNodes("#{'a' == 'b'}");

    assertThat(astNode).isInstanceOf(AstBinary.class);
    assertLeftAndRightByOperator((AstBinary) astNode, "a", "b", AstBinary.EQ);
  }

  @Test
  public void itParseBinaryOrEqualCondition() {
    AstNode astNode = buildExpressionNodes("#{'a' == 'b' or 'c' == 'd'}");

    assertThat(astNode).isInstanceOf(AstBinary.class);

    AstBinary astBinary = (AstBinary) astNode;
    AstNode left = astBinary.getChild(0);
    AstNode right = astBinary.getChild(1);

    assertThat(astBinary.getOperator()).isEqualTo(OrOperator.OP);
    assertThat(left).isInstanceOf(AstBinary.class);
    assertThat(right).isInstanceOf(AstBinary.class);

    assertLeftAndRightByOperator((AstBinary) left, "a", "b", AstBinary.EQ);
    assertLeftAndRightByOperator((AstBinary) right, "c", "d", AstBinary.EQ);
  }

  @Test
  public void itParseSingleBinaryWithExpressionCondition() {
    AstNode astNode = buildExpressionNodes("#{'a' is equalto 'b'}");

    assertThat(astNode).isInstanceOf(AstMethod.class);
    assertForExpression(astNode, "a", "b", "exptest:equalto");
  }

  @Test
  public void itParseBinaryOrWithEqualSymbolAndExpressionCondition() {
    AstNode astNode = buildExpressionNodes("#{'a' == 'b' or 'c' is equalto 'd'}");

    assertThat(astNode).isInstanceOf(AstBinary.class);

    AstBinary astBinary = (AstBinary) astNode;
    AstNode left = astBinary.getChild(0);
    AstNode right = astBinary.getChild(1);

    assertThat(astBinary.getOperator()).isEqualTo(OrOperator.OP);
    assertThat(left).isInstanceOf(AstBinary.class);
    assertThat(right).isInstanceOf(AstMethod.class);

    assertLeftAndRightByOperator((AstBinary) left, "a", "b", AstBinary.EQ);
    assertForExpression(right, "c", "d", "exptest:equalto");
  }

  @Test
  public void itParseBinaryOrWithExpressionsCondition() {
    AstNode astNode = buildExpressionNodes("#{'a' is equalto 'b' or 'c' is equalto 'd'}");

    assertThat(astNode).isInstanceOf(AstBinary.class);

    AstBinary astBinary = (AstBinary) astNode;
    AstNode left = astBinary.getChild(0);
    AstNode right = astBinary.getChild(1);

    assertThat(astBinary.getOperator()).isEqualTo(OrOperator.OP);
    assertThat(left).isInstanceOf(AstMethod.class);
    assertThat(right).isInstanceOf(AstMethod.class);

    assertForExpression(left, "a", "b", "exptest:equalto");
    assertForExpression(right, "c", "d", "exptest:equalto");
  }

  @Test
  public void itParseBinaryOrWithNegativeExpressionsCondition() {
    AstNode astNode = buildExpressionNodes(
      "#{'a' is not equalto 'b' or 'c' is not equalto 'd'}"
    );

    assertThat(astNode).isInstanceOf(AstBinary.class);

    AstBinary astBinary = (AstBinary) astNode;
    AstNode left = astBinary.getChild(0);
    AstNode right = astBinary.getChild(1);

    assertThat(astBinary.getOperator()).isEqualTo(OrOperator.OP);
    assertThat(left).isInstanceOf(AstMethod.class);
    assertThat(right).isInstanceOf(AstMethod.class);

    assertForExpression(left, "a", "b", "exptest:equalto");
    assertForExpression(right, "c", "d", "exptest:equalto");
  }

  @Test
  public void itParsesNestedCommasNotAsTuple() {
    AstNode astNode = buildExpressionNodes("#{(range(0,range(0,2)[1]))}");
    assertThat(astNode).isInstanceOf(AstNested.class);
  }

  @Test
  public void itChecksForTupleUntilFinalParentheses() {
    AstNode astNode = buildExpressionNodes("#{((0),2)}");
    assertThat(astNode).isInstanceOf(AstTuple.class);
  }

  @Test
  public void itParsesExpTestLikeDictionary() {
    // Don't want to accidentally try to parse these as a filter or exptest
    AstNode astNode = buildExpressionNodes(
      "#{{filter:length.filter, exptest:equalto.evaluate}}"
    );
    assertThat(astNode).isInstanceOf(AstDict.class);
  }

  private void assertForExpression(
    AstNode astNode,
    String leftExpected,
    String rightExpected,
    String expression
  ) {
    AstIdentifier astIdentifier = (AstIdentifier) astNode.getChild(0).getChild(0);
    assertThat(astIdentifier.getName()).isEqualTo(expression);

    AstParameters astParameters = (AstParameters) astNode.getChild(1);
    assertThat(astParameters.getChild(0)).isInstanceOf(AstString.class);
    assertThat(astParameters.getChild(1)).isInstanceOf(AstIdentifier.class);
    assertThat(astParameters.getChild(2)).isInstanceOf(AstString.class);

    assertThat(astParameters.getChild(0).eval(null, null)).isEqualTo(leftExpected);
    assertThat(((AstIdentifier) astParameters.getChild(1)).getName())
      .isEqualTo("____int3rpr3t3r____");
    assertThat(astParameters.getChild(2).eval(null, null)).isEqualTo(rightExpected);
  }

  private void assertLeftAndRightByOperator(
    AstBinary astBinary,
    String leftExpected,
    String rightExpected,
    AstBinary.Operator operator
  ) {
    AstNode left = astBinary.getChild(0);
    AstNode right = astBinary.getChild(1);

    assertThat(astBinary.getOperator()).isEqualTo(operator);
    assertThat(left).isInstanceOf(AstString.class);
    assertThat(right).isInstanceOf(AstString.class);
    assertThat(left.eval(null, null)).isEqualTo(leftExpected);
    assertThat(right.eval(null, null)).isEqualTo(rightExpected);
  }

  private AstNode buildExpressionNodes(String input) {
    ExtendedCustomParser extendedParser = new ExtendedCustomParser(new Builder(), input);
    extendedParser.consumeTokenExpose();
    extendedParser.consumeTokenExpose();

    try {
      return extendedParser.expr(true);
    } catch (Exception exception) {
      fail(exception.getMessage(), exception);
      return null;
    }
  }

  private static class ExtendedCustomParser extends ExtendedParser {

    private ExtendedCustomParser(Builder context, String input) {
      super(context, input);
    }

    private void consumeTokenExpose() {
      try {
        super.consumeToken();
      } catch (Exception exception) {
        Assertions.fail(exception.getMessage(), exception);
      }
    }
  }
}
