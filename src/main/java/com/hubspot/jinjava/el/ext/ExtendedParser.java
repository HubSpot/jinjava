package com.hubspot.jinjava.el.ext;

import static de.odysseus.el.tree.impl.Builder.Feature.METHOD_INVOCATIONS;
import static de.odysseus.el.tree.impl.Builder.Feature.NULL_PROPERTIES;
import static de.odysseus.el.tree.impl.Scanner.Symbol.COLON;
import static de.odysseus.el.tree.impl.Scanner.Symbol.COMMA;
import static de.odysseus.el.tree.impl.Scanner.Symbol.IDENTIFIER;
import static de.odysseus.el.tree.impl.Scanner.Symbol.LBRACK;
import static de.odysseus.el.tree.impl.Scanner.Symbol.LPAREN;
import static de.odysseus.el.tree.impl.Scanner.Symbol.QUESTION;
import static de.odysseus.el.tree.impl.Scanner.Symbol.RBRACK;
import static de.odysseus.el.tree.impl.Scanner.Symbol.RPAREN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.el.ELException;

import com.google.common.collect.Lists;

import de.odysseus.el.tree.impl.Builder;
import de.odysseus.el.tree.impl.Builder.Feature;
import de.odysseus.el.tree.impl.Parser;
import de.odysseus.el.tree.impl.Scanner;
import de.odysseus.el.tree.impl.Scanner.ScanException;
import de.odysseus.el.tree.impl.Scanner.Symbol;
import de.odysseus.el.tree.impl.Scanner.Token;
import de.odysseus.el.tree.impl.ast.AstBinary;
import de.odysseus.el.tree.impl.ast.AstBracket;
import de.odysseus.el.tree.impl.ast.AstDot;
import de.odysseus.el.tree.impl.ast.AstFunction;
import de.odysseus.el.tree.impl.ast.AstNested;
import de.odysseus.el.tree.impl.ast.AstNode;
import de.odysseus.el.tree.impl.ast.AstNull;
import de.odysseus.el.tree.impl.ast.AstParameters;
import de.odysseus.el.tree.impl.ast.AstProperty;

public class ExtendedParser extends Parser {

  public static final String INTERPRETER = "____int3rpr3t3r____";
  public static final String FILTER_PREFIX = "filter:";
  public static final String EXPTEST_PREFIX = "exptest:";

  static final Scanner.ExtensionToken PIPE = new Scanner.ExtensionToken("|");
  static final Scanner.ExtensionToken IS = new Scanner.ExtensionToken("is");
  static final Token IF = new Scanner.Token(Symbol.QUESTION, "if");
  static final Token ELSE = new Scanner.Token(Symbol.COLON, "else");

  static final Scanner.ExtensionToken LITERAL_DICT_START = new Scanner.ExtensionToken("{");
  static final Scanner.ExtensionToken LITERAL_DICT_END = new Scanner.ExtensionToken("}");

  static final Scanner.ExtensionToken TRUNC_DIV = TruncDivOperator.TOKEN;
  static final Scanner.ExtensionToken POWER_OF  = PowerOfOperator.TOKEN;

  static {
    ExtendedScanner.addKeyToken(IF);
    ExtendedScanner.addKeyToken(ELSE);

    ExtendedScanner.addKeyToken(TruncDivOperator.TOKEN);
    ExtendedScanner.addKeyToken(PowerOfOperator.TOKEN);

    ExtendedScanner.addKeyToken(CollectionMembershipOperator.TOKEN);
  }

  public ExtendedParser(Builder context, String input) {
    super(context, input);

    putExtensionHandler(AbsOperator.TOKEN, AbsOperator.HANDLER);
    putExtensionHandler(NamedParameterOperator.TOKEN, NamedParameterOperator.HANDLER);
    putExtensionHandler(StringConcatOperator.TOKEN, StringConcatOperator.HANDLER);
    putExtensionHandler(TruncDivOperator.TOKEN, TruncDivOperator.HANDLER);
    putExtensionHandler(PowerOfOperator.TOKEN, PowerOfOperator.HANDLER);

    putExtensionHandler(CollectionMembershipOperator.TOKEN, CollectionMembershipOperator.HANDLER);

    putExtensionHandler(PIPE, new ExtensionHandler(ExtensionPoint.AND) {
      @Override
      public AstNode createAstNode(AstNode... children) {
        throw new ELException("Illegal use of '|' operator");
      }
    });

    putExtensionHandler(LITERAL_DICT_START, NULL_EXT_HANDLER);
    putExtensionHandler(LITERAL_DICT_END, NULL_EXT_HANDLER);
  }

  protected AstNode interpreter() {
    return identifier(INTERPRETER);
  }

  @Override
  protected AstNode expr(boolean required) throws ScanException, ParseException {
    AstNode v = or(required);
    if (v == null) {
      return null;
    }
    if (getToken().getSymbol() == QUESTION) {
      if (!getToken().getImage().equals("if")) {
        consumeToken();
        AstNode a = expr(true);
        consumeToken(COLON);
        AstNode b = expr(true);
        v = createAstChoice(v, a, b);
      } else {
        consumeToken();
        AstNode cond = expr(true);
        AstNode elseNode = new AstNull();

        if (getToken().getImage().equals("else")) {
          consumeToken();
          elseNode = expr(true);
        }

        v = createAstChoice(cond, v, elseNode);
      }
    }

    return v;
  }

  @Override
  protected AstNode or(boolean required) throws ScanException, ParseException {
    AstNode v = and(required);
    if (v == null) {
      return null;
    }
    while (true) {
      switch (getToken().getSymbol()) {
      case OR:
        consumeToken();
        v = createAstBinary(v, and(true), OrOperator.OP);
        break;
      case EXTENSION:
        if (getExtensionHandler(getToken()).getExtensionPoint() == ExtensionPoint.OR) {
          v = getExtensionHandler(consumeToken()).createAstNode(v, and(true));
          break;
        }
      default:
        return v;
      }
    }
  }

  @Override
  protected AstNode add(boolean required) throws ScanException, ParseException {
    AstNode v = mul(required);
    if (v == null) {
      return null;
    }
    while (true) {
      switch (getToken().getSymbol()) {
      case PLUS:
        consumeToken();
        v = createAstBinary(v, mul(true), AdditionOperator.OP);
        break;
      case MINUS:
        consumeToken();
        v = createAstBinary(v, mul(true), AstBinary.SUB);
        break;
      case EXTENSION:
        if (getExtensionHandler(getToken()).getExtensionPoint() == ExtensionPoint.ADD) {
          v = getExtensionHandler(consumeToken()).createAstNode(v, mul(true));
          break;
        }
      default:
        return v;
      }
    }
  }

  @Override
  protected AstParameters params() throws ScanException, ParseException {
    return params(LPAREN, RPAREN);
  }

  protected AstParameters params(Symbol left, Symbol right) throws ScanException, ParseException {
    consumeToken(left);
    List<AstNode> l = Collections.emptyList();
    AstNode v = expr(false);
    if (v != null) {
      l = new ArrayList<>();
      l.add(v);
      while (getToken().getSymbol() == COMMA) {
        consumeToken();
        l.add(expr(true));
      }
    }
    consumeToken(right);
    return new AstParameters(l);
  }

  protected AstDict dict() throws ScanException, ParseException {
    consumeToken();
    Map<AstNode, AstNode> dict = new LinkedHashMap<>();

    AstNode k = expr(false);
    if (k != null) {
      consumeToken(COLON);
      AstNode v = expr(true);

      dict.put(k, v);
      while (getToken().getSymbol() == COMMA) {
        consumeToken();

        // python is lenient about dangling commas...!
        k = expr(false);

        if (k != null) {
          consumeToken(COLON);
          v = expr(true);
          dict.put(k, v);
        }
      }
    }

    if (!getToken().getImage().equals("}")) {
      fail("}");
    }
    consumeToken();
    return new AstDict(dict);
  }

  @Override
  protected AstFunction createAstFunction(String name, int index, AstParameters params) {
    return new AstMacroFunction(name, index, params, context.isEnabled(Feature.VARARGS));
  }

  @Override
  protected AstNode nonliteral() throws ScanException, ParseException {
    AstNode v = null;
    switch (getToken().getSymbol()) {
    case IDENTIFIER:
      String name = consumeToken().getImage();
      if (getToken().getSymbol() == COLON && lookahead(0).getSymbol() == IDENTIFIER && lookahead(1).getSymbol() == LPAREN) { // ns:f(...)
        consumeToken();
        name += ":" + getToken().getImage();
        consumeToken();
      }
      if (getToken().getSymbol() == LPAREN) { // function
        v = function(name, params());
      } else { // identifier
        v = identifier(name);
      }
      break;
    case LPAREN:
      int i = 0;
      Symbol s;
      do {
        s = lookahead(i++).getSymbol();
        if (s == Symbol.COMMA) {
          return new AstTuple(params());
        }
      } while (s != Symbol.RPAREN && s != Symbol.EOF);

      consumeToken();
      v = expr(true);
      consumeToken(RPAREN);
      v = new AstNested(v);
      break;

    default:
      break;
    }
    return v;
  }

  @Override
  protected AstNode literal() throws ScanException, ParseException {
    AstNode v = null;
    switch (getToken().getSymbol()) {
    case LBRACK:
      v = new AstList(params(LBRACK, RBRACK));

      break;
    case LPAREN:
      v = new AstTuple(params());
      break;
    case EXTENSION:
      if (getToken() == LITERAL_DICT_START) {
        v = dict();
      } else if (getToken() == LITERAL_DICT_END) {
        return null;
      }
      break;
    default:
      break;
    }

    if (v != null) {
      return v;
    }

    return super.literal();
  }

  protected AstRangeBracket createAstRangeBracket(AstNode base, AstNode rangeStart, AstNode rangeMax, boolean lvalue, boolean strict) {
    return new AstRangeBracket(base, rangeStart, rangeMax, lvalue, strict, context.isEnabled(Feature.IGNORE_RETURN_TYPE));
  }

  @Override
  protected AstNode value() throws ScanException, ParseException {
    boolean lvalue = true;
    AstNode v = nonliteral();
    if (v == null) {
      v = literal();
      if (v == null) {
        return null;
      }
      lvalue = false;
    }
    while (true) {
      switch (getToken().getSymbol()) {
      case DOT:
        consumeToken();
        String name = consumeToken(IDENTIFIER).getImage();
        AstDot dot = createAstDot(v, name, lvalue);
        if (getToken().getSymbol() == LPAREN && context.isEnabled(METHOD_INVOCATIONS)) {
          v = createAstMethod(dot, params());
        } else {
          v = dot;
        }
        break;
      case LBRACK:
        consumeToken();
        AstNode property = expr(false);
        boolean strict = !context.isEnabled(NULL_PROPERTIES);

        Token nextToken = consumeToken();

        if (nextToken.getSymbol() == COLON) {
          AstNode rangeMax = expr(false);
          consumeToken(RBRACK);
          v = createAstRangeBracket(v, property, rangeMax, lvalue, strict);
        } else if (nextToken.getSymbol() == RBRACK) {
          AstBracket bracket = createAstBracket(v, property, lvalue, strict);
          if (getToken().getSymbol() == LPAREN && context.isEnabled(METHOD_INVOCATIONS)) {
            v = createAstMethod(bracket, params());
          } else {
            v = bracket;
          }
        } else {
          fail(RBRACK);
        }

        break;
      default:
        if ("|".equals(getToken().getImage()) && lookahead(0).getSymbol() == IDENTIFIER) {
          do {
            consumeToken(); // '|'
            String filterName = consumeToken().getImage();
            List<AstNode> filterParams = Lists.newArrayList(v, interpreter());

            // optional filter args
            if (getToken().getSymbol() == Symbol.LPAREN) {
              AstParameters astParameters = params();
              for (int i = 0; i < astParameters.getCardinality(); i++) {
                filterParams.add(astParameters.getChild(i));
              }
            }

            AstProperty filterProperty = createAstDot(identifier(FILTER_PREFIX + filterName), "filter", true);
            v = createAstMethod(filterProperty, new AstParameters(filterParams)); // function("filter:" + filterName, new AstParameters(filterParams));
          } while ("|".equals(getToken().getImage()));
        } else if ("is".equals(getToken().getImage()) &&
            "not".equals(lookahead(0).getImage()) &&
            lookahead(1).getSymbol() == IDENTIFIER) {
          consumeToken(); // 'is'
          consumeToken(); // 'not'
          String exptestName = consumeToken().getImage();
          List<AstNode> exptestParams = Lists.newArrayList(v, interpreter());

          // optional exptest arg
          AstNode arg = expr(false);
          if (arg != null) {
            exptestParams.add(arg);
          }

          AstProperty exptestProperty = createAstDot(identifier(EXPTEST_PREFIX + exptestName), "evaluateNegated", true);
          v = createAstMethod(exptestProperty, new AstParameters(exptestParams));
        } else if ("is".equals(getToken().getImage()) && lookahead(0).getSymbol() == IDENTIFIER) {
          consumeToken(); // 'is'
          String exptestName = consumeToken().getImage();
          List<AstNode> exptestParams = Lists.newArrayList(v, interpreter());

          // optional exptest arg
          AstNode arg = expr(false);
          if (arg != null) {
            exptestParams.add(arg);
          }

          AstProperty exptestProperty = createAstDot(identifier(EXPTEST_PREFIX + exptestName), "evaluate", true);
          v = createAstMethod(exptestProperty, new AstParameters(exptestParams));
        }

        return v;
      }
    }
  }

  @Override
  protected Scanner createScanner(String expression) {
    return new ExtendedScanner(expression);
  }

  private static final ExtensionHandler NULL_EXT_HANDLER = new ExtensionHandler(null) {
    @Override
    public AstNode createAstNode(AstNode... children) {
      return null;
    }
  };

}
