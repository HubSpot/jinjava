package com.hubspot.jinjava.el.ext;

import static de.odysseus.el.tree.impl.Builder.Feature.METHOD_INVOCATIONS;
import static de.odysseus.el.tree.impl.Builder.Feature.NULL_PROPERTIES;
import static de.odysseus.el.tree.impl.Scanner.Symbol.COLON;
import static de.odysseus.el.tree.impl.Scanner.Symbol.COMMA;
import static de.odysseus.el.tree.impl.Scanner.Symbol.DOT;
import static de.odysseus.el.tree.impl.Scanner.Symbol.EQ;
import static de.odysseus.el.tree.impl.Scanner.Symbol.FALSE;
import static de.odysseus.el.tree.impl.Scanner.Symbol.GE;
import static de.odysseus.el.tree.impl.Scanner.Symbol.GT;
import static de.odysseus.el.tree.impl.Scanner.Symbol.IDENTIFIER;
import static de.odysseus.el.tree.impl.Scanner.Symbol.LBRACK;
import static de.odysseus.el.tree.impl.Scanner.Symbol.LE;
import static de.odysseus.el.tree.impl.Scanner.Symbol.LPAREN;
import static de.odysseus.el.tree.impl.Scanner.Symbol.LT;
import static de.odysseus.el.tree.impl.Scanner.Symbol.NE;
import static de.odysseus.el.tree.impl.Scanner.Symbol.QUESTION;
import static de.odysseus.el.tree.impl.Scanner.Symbol.RBRACK;
import static de.odysseus.el.tree.impl.Scanner.Symbol.RPAREN;
import static de.odysseus.el.tree.impl.Scanner.Symbol.TRUE;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.LegacyOverrides;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
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
import de.odysseus.el.tree.impl.ast.AstRightValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.el.ELException;

public class ExtendedParser extends Parser {

  public static final String INTERPRETER = "____int3rpr3t3r____";
  public static final String FILTER_PREFIX = "filter:";
  public static final String EXPTEST_PREFIX = "exptest:";

  static final Scanner.ExtensionToken PIPE = new Scanner.ExtensionToken("|");
  static final Scanner.ExtensionToken IS = new Scanner.ExtensionToken("is");
  static final Token IF = new Scanner.Token(Symbol.QUESTION, "if");
  static final Token ELSE = new Scanner.Token(Symbol.COLON, "else");
  static final Token PYTRUE = new Scanner.Token(Symbol.TRUE, "True");
  static final Token PYFALSE = new Scanner.Token(Symbol.FALSE, "False");

  static final Scanner.ExtensionToken LITERAL_DICT_START = new Scanner.ExtensionToken(
    "{"
  );
  static final Scanner.ExtensionToken LITERAL_DICT_END = new Scanner.ExtensionToken("}");

  static final Scanner.ExtensionToken TRUNC_DIV = TruncDivOperator.TOKEN;
  static final Scanner.ExtensionToken POWER_OF = PowerOfOperator.TOKEN;

  static final Set<Symbol> VALID_SYMBOLS_FOR_EXP_TEST = Sets.newHashSet(
    IDENTIFIER,
    EQ,
    NE,
    LT,
    LE,
    GT,
    GE,
    TRUE,
    FALSE,
    CollectionMembershipOperator.TOKEN.getSymbol()
  );

  static {
    ExtendedScanner.addKeyToken(IF);
    ExtendedScanner.addKeyToken(ELSE);
    ExtendedScanner.addKeyToken(PYTRUE);
    ExtendedScanner.addKeyToken(PYFALSE);

    ExtendedScanner.addKeyToken(TruncDivOperator.TOKEN);
    ExtendedScanner.addKeyToken(PowerOfOperator.TOKEN);

    ExtendedScanner.addKeyToken(CollectionMembershipOperator.TOKEN);
    ExtendedScanner.addKeyToken(CollectionNonMembershipOperator.TOKEN);
  }

  public ExtendedParser(Builder context, String input) {
    super(context, input);
    putExtensionHandler(AbsOperator.TOKEN, AbsOperator.HANDLER);
    putExtensionHandler(NamedParameterOperator.TOKEN, NamedParameterOperator.HANDLER);
    putExtensionHandler(StringConcatOperator.TOKEN, StringConcatOperator.HANDLER);
    putExtensionHandler(TruncDivOperator.TOKEN, TruncDivOperator.HANDLER);
    putExtensionHandler(PowerOfOperator.TOKEN, PowerOfOperator.HANDLER);

    putExtensionHandler(
      CollectionMembershipOperator.TOKEN,
      CollectionMembershipOperator.HANDLER
    );
    putExtensionHandler(
      CollectionNonMembershipOperator.TOKEN,
      CollectionNonMembershipOperator.HANDLER
    );

    putExtensionHandler(
      PIPE,
      new ExtensionHandler(ExtensionPoint.AND) {
        @Override
        public AstNode createAstNode(AstNode... children) {
          throw new ELException("Illegal use of '|' operator");
        }
      }
    );

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

  protected AstParameters params(Symbol left, Symbol right)
    throws ScanException, ParseException {
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
    return createAstParameters(l);
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

    Scanner.Token nextToken = getToken();
    if (nextToken == null || !"}".equals(nextToken.getImage())) {
      fail("}");
    }
    consumeToken();
    return createAstDict(dict);
  }

  protected AstDict createAstDict(Map<AstNode, AstNode> dict) {
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
        if (getToken().getSymbol() == COLON) {
          Symbol lookahead = lookahead(0).getSymbol();
          if (
            isPossibleExpTest(lookahead) &&
            (lookahead(1).getSymbol() == LPAREN || (isPossibleExpTestOrFilter(name)))
          ) { // ns:f(...)
            consumeToken();
            name += ":" + getToken().getImage();
            consumeToken();
          }
        }
        if (getToken().getSymbol() == LPAREN) { // function
          v = function(name, params());
        } else { // identifier
          v = identifier(name);
        }
        break;
      case LPAREN:
        int i = 0;
        Symbol s = lookahead(i++).getSymbol();
        int depth = 0;
        while (s != Symbol.EOF && (depth > 0 || s != Symbol.RPAREN)) {
          if (s == LPAREN || s == LBRACK) {
            depth++;
          } else if (depth > 0 && (s == RPAREN || s == RBRACK)) {
            depth--;
          } else if (depth == 0) {
            if (s == Symbol.COMMA) {
              return createAstTuple(params());
            }
          }
          s = lookahead(i++).getSymbol();
        }

        consumeToken();
        v = expr(true);
        consumeToken(RPAREN);
        v = createAstNested(v);
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
        v = createAstList(params(LBRACK, RBRACK));

        break;
      case LPAREN:
        v = createAstTuple(params());
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

  @Override
  protected AstNode cmp(boolean required) throws ScanException, ParseException {
    AstNode v = add(required);
    if (v == null) {
      return null;
    }
    while (true) {
      switch (getToken().getSymbol()) {
        case LT:
          consumeToken();
          v = createAstBinary(v, add(true), AstBinary.LT);
          break;
        case LE:
          consumeToken();
          v = createAstBinary(v, add(true), AstBinary.LE);
          break;
        case GE:
          consumeToken();
          v = createAstBinary(v, add(true), AstBinary.GE);
          break;
        case GT:
          consumeToken();
          v = createAstBinary(v, add(true), AstBinary.GT);
          break;
        case EXTENSION:
          if (getExtensionHandler(getToken()).getExtensionPoint() == ExtensionPoint.CMP) {
            v = getExtensionHandler(consumeToken()).createAstNode(v, add(true));
            break;
          }
        default:
          if (
            "not".equals(getToken().getImage()) && "in".equals(lookahead(0).getImage())
          ) {
            consumeToken(); // not
            consumeToken(); // in
            v =
              getExtensionHandler(CollectionNonMembershipOperator.TOKEN)
                .createAstNode(v, add(true));
            break;
          }
      }
      return v;
    }
  }

  @Override
  protected AstNode mul(boolean required) throws ScanException, ParseException {
    ParseLevel next = shouldUseNaturalOperatorPrecedence() ? this::filter : this::unary;

    AstNode v = next.apply(required);
    if (v == null) {
      return null;
    }
    while (true) {
      switch (getToken().getSymbol()) {
        case MUL:
          consumeToken();
          v = createAstBinary(v, next.apply(true), AstBinary.MUL);
          break;
        case DIV:
          consumeToken();
          v = createAstBinary(v, next.apply(true), AstBinary.DIV);
          break;
        case MOD:
          consumeToken();
          v = createAstBinary(v, next.apply(true), AstBinary.MOD);
          break;
        case EXTENSION:
          if (getExtensionHandler(getToken()).getExtensionPoint() == ExtensionPoint.MUL) {
            v = getExtensionHandler(consumeToken()).createAstNode(v, next.apply(true));
            break;
          }
        default:
          return v;
      }
    }
  }

  protected AstNode filter(boolean required) throws ScanException, ParseException {
    AstNode v = unary(required);
    if (v == null) {
      return null;
    }
    return parseOperators(v);
  }

  protected AstRightValue createAstNested(AstNode node) {
    return new AstNested(node);
  }

  protected AstTuple createAstTuple(AstParameters parameters)
    throws ScanException, ParseException {
    return new AstTuple(parameters);
  }

  protected AstList createAstList(AstParameters parameters)
    throws ScanException, ParseException {
    return new AstList(parameters);
  }

  protected AstRangeBracket createAstRangeBracket(
    AstNode base,
    AstNode rangeStart,
    AstNode rangeMax,
    boolean lvalue,
    boolean strict
  ) {
    return new AstRangeBracket(
      base,
      rangeStart,
      rangeMax,
      lvalue,
      strict,
      context.isEnabled(Feature.IGNORE_RETURN_TYPE)
    );
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
            if (
              getToken().getSymbol() == LPAREN && context.isEnabled(METHOD_INVOCATIONS)
            ) {
              v = createAstMethod(bracket, params());
            } else {
              v = bracket;
            }
          } else {
            fail(RBRACK);
          }

          break;
        default:
          if (shouldUseNaturalOperatorPrecedence()) {
            return v;
          }
          return parseOperators(v);
      }
    }
  }

  private AstNode parseOperators(AstNode left) throws ScanException, ParseException {
    if ("|".equals(getToken().getImage()) && lookahead(0).getSymbol() == IDENTIFIER) {
      AstNode v = left;

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

        AstProperty filterProperty = createAstDot(
          identifier(FILTER_PREFIX + filterName),
          "filter",
          true
        );
        v = createAstMethod(filterProperty, createAstParameters(filterParams)); // function("filter:" + filterName, new AstParameters(filterParams));
      } while ("|".equals(getToken().getImage()));

      return v;
    } else if (
      "is".equals(getToken().getImage()) &&
      "not".equals(lookahead(0).getImage()) &&
      isPossibleExpTest(lookahead(1).getSymbol())
    ) {
      consumeToken(); // 'is'
      consumeToken(); // 'not'
      return buildAstMethodForIdentifier(left, "evaluateNegated");
    } else if (
      "is".equals(getToken().getImage()) && isPossibleExpTest(lookahead(0).getSymbol())
    ) {
      consumeToken(); // 'is'
      return buildAstMethodForIdentifier(left, "evaluate");
    }

    return left;
  }

  protected AstParameters createAstParameters(List<AstNode> nodes) {
    return new AstParameters(nodes);
  }

  private boolean isPossibleExpTest(Symbol symbol) {
    return VALID_SYMBOLS_FOR_EXP_TEST.contains(symbol);
  }

  private boolean isPossibleExpTestOrFilter(String namespace)
    throws ParseException, ScanException {
    if (
      FILTER_PREFIX.substring(0, FILTER_PREFIX.length() - 1).equals(namespace) ||
      EXPTEST_PREFIX.substring(0, EXPTEST_PREFIX.length() - 1).equals(namespace) &&
      lookahead(1).getSymbol() == DOT &&
      lookahead(2).getSymbol() == IDENTIFIER
    ) {
      Token property = lookahead(2);
      if (
        "filter".equals(property.getImage()) ||
        "evaluate".equals(property.getImage()) ||
        "evaluateNegated".equals(property.getImage())
      ) { // exptest:equalto.evaluate(...
        return lookahead(3).getSymbol() == LPAREN;
      }
    }
    return false;
  }

  private AstNode buildAstMethodForIdentifier(AstNode astNode, String property)
    throws ScanException, ParseException {
    String exptestName = consumeToken().getImage();
    List<AstNode> exptestParams = Lists.newArrayList(astNode, interpreter());

    // optional exptest arg
    AstNode arg = value();
    if (arg != null) {
      exptestParams.add(arg);
    }

    AstProperty exptestProperty = createAstDot(
      identifier(EXPTEST_PREFIX + exptestName),
      property,
      true
    );
    return createAstMethod(exptestProperty, createAstParameters(exptestParams));
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

  private static boolean shouldUseNaturalOperatorPrecedence() {
    return JinjavaInterpreter
      .getCurrentMaybe()
      .map(JinjavaInterpreter::getConfig)
      .map(JinjavaConfig::getLegacyOverrides)
      .map(LegacyOverrides::isUseNaturalOperatorPrecedence)
      .orElse(false);
  }

  @FunctionalInterface
  private interface ParseLevel {
    AstNode apply(boolean required) throws ScanException, ParseException;
  }
}
