package com.hubspot.jinjava.el.ext;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.common.base.Throwables;

import de.odysseus.el.tree.impl.Scanner;

public class ExtendedScanner extends Scanner {

  protected ExtendedScanner(String input) {
    super(input);
  }

  @Override
  public Token next() throws ScanException {
    if (getToken() != null) {
      incrPosition(getToken().getSize());
    }

    int length = getInput().length();

    if (isEval()) {
      while (getPosition() < length && isWhitespace(getInput().charAt(getPosition()))) {
        incrPosition(1);
      }
    }

    Token token = null;

    if (getPosition() == length) {
      token = fixed(Symbol.EOF);
    } else {
      token = nextToken();
    }

    setToken(token);
    return token;
  }

  protected boolean isWhitespace(char c) {
    return Character.isWhitespace(c) || Character.isSpaceChar(c);
  }

  private static final Method ADD_KEY_TOKEN_METHOD;
  private static final Field TOKEN_FIELD;
  private static final Field POSITION_FIELD;

  static {
    try {
      ADD_KEY_TOKEN_METHOD = Scanner.class.getDeclaredMethod("addKeyToken", Token.class);
      ADD_KEY_TOKEN_METHOD.setAccessible(true);
      TOKEN_FIELD = Scanner.class.getDeclaredField("token");
      TOKEN_FIELD.setAccessible(true);
      POSITION_FIELD = Scanner.class.getDeclaredField("position");
      POSITION_FIELD.setAccessible(true);
    } catch (NoSuchFieldException | SecurityException | NoSuchMethodException e) {
      throw Throwables.propagate(e);
    }
  }

  protected static void addKeyToken(Token token) {
    try {
      ADD_KEY_TOKEN_METHOD.invoke(null, token);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw Throwables.propagate(e);
    }
  }

  protected void setToken(Token token) {
    try {
      TOKEN_FIELD.set(this, token);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw Throwables.propagate(e);
    }
  }

  @SuppressWarnings("boxing")
  protected void incrPosition(int n) {
    try {
      POSITION_FIELD.set(this, getPosition() + n);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  protected Token nextToken() throws ScanException {
    if (isEval()) {
      if (getInput().charAt(getPosition()) == '}') {
        if (getPosition() < getInput().length() - 1) {
          return ExtendedParser.LITERAL_DICT_END;
        } else {
          return fixed(Symbol.END_EVAL);
        }
      }
      return nextEval();
    } else {
      if (getPosition() + 1 < getInput().length() && getInput().charAt(getPosition() + 1) == '{') {
        switch (getInput().charAt(getPosition())) {
          case '#':
            return fixed(Symbol.START_EVAL_DEFERRED);
          case '$':
            return fixed(Symbol.START_EVAL_DYNAMIC);
        }
      }
      return nextText();
    }
  }

  @Override
  protected Token nextEval() throws ScanException {
    char c1 = getInput().charAt(getPosition());
    char c2 = getPosition() < getInput().length() - 1 ? getInput().charAt(getPosition() + 1) : (char) 0;

    if (c1 == '/' && c2 == '/') {
        return ExtendedParser.TRUNC_DIV;
    }
    if (c1 == '*' && c2 == '*') {
        return ExtendedParser.POWER_OF;
    }
    if (c1 == '|' && c2 != '|') {
      return ExtendedParser.PIPE;
    }
    if (c1 == '+' && Character.isDigit(c2)) {
      return AbsOperator.TOKEN;
    }
    if (c1 == '=' && c2 != '=') {
      return NamedParameterOperator.TOKEN;
    }
    if (c1 == '{') {
      return ExtendedParser.LITERAL_DICT_START;
    }
    if (c1 == '}' && c2 != 0) {
      return ExtendedParser.LITERAL_DICT_END;
    }
    if (c1 == '~') {
      return StringConcatOperator.TOKEN;
    }

    return super.nextEval();
  }

  @Override
  protected Token nextString() throws ScanException {
    builder.setLength(0);
    char quote = getInput().charAt(getPosition());
    int i = getPosition() + 1;
    int l = getInput().length();
    while (i < l) {
      char c = getInput().charAt(i++);
      if (c == '\\') {
        if (i == l) {
          throw new ScanException(getPosition(), "unterminated string", quote + " or \\");
        } else {
          c = getInput().charAt(i++);
          switch (c) {
            case '\\':
            case '\'':
            case '"':
              builder.append(c);
              break;

            case 'n':
              builder.append('\n');
              break;
            case 't':
              builder.append('\t');
              break;
            case 'b':
              builder.append('\b');
              break;
            case 'f':
              builder.append('\f');
              break;
            case 'r':
              builder.append('\r');
              break;
            default:
              throw new ScanException(getPosition(), "invalid escape sequence \\" + c, "\\" + quote + " or \\\\");
          }
        }
      } else if (c == quote) {
        return token(Symbol.STRING, builder.toString(), i - getPosition());
      } else {
        builder.append(c);
      }
    }
    throw new ScanException(getPosition(), "unterminated string", String.valueOf(quote));
  }

}
