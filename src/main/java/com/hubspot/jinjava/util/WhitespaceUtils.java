package com.hubspot.jinjava.util;

import com.google.common.base.Strings;
import com.hubspot.jinjava.interpret.InterpretException;

public final class WhitespaceUtils {
  private static final char[] QUOTE_CHARS = new char[] { '\'', '"' };

  public static boolean startsWith(String s, String prefix) {
    if (s == null) {
      return false;
    }

    for (int i = 0; i < s.length(); i++) {
      if (Character.isWhitespace(s.charAt(i))) {
        continue;
      } else {
        return s.regionMatches(i, prefix, 0, prefix.length());
      }
    }

    return false;
  }

  public static boolean endsWith(String s, String suffix) {
    if (s == null) {
      return false;
    }

    for (int i = s.length() - 1; i >= 0; i--) {
      if (Character.isWhitespace(s.charAt(i))) {
        continue;
      } else {
        return s.regionMatches(i - suffix.length() + 1, suffix, 0, suffix.length());
      }
    }

    return false;
  }

  public static boolean isWrappedWith(String s, String prefix, String suffix) {
    return startsWith(s, prefix) && endsWith(s, suffix);
  }

  public static boolean isQuoted(String s) {
    if (startsWith(s, "'")) {
      if (!endsWith(s, "'")) {
        throw new InterpretException("Unbalanced quotes: " + s);
      }
      return true;
    } else if (startsWith(s, "\"")) {
      if (!endsWith(s, "\"")) {
        throw new InterpretException("Unbalanced quotes: " + s);
      }
      return true;
    }
    return false;
  }

  public static boolean isExpressionQuoted(String s) {
    if (Strings.isNullOrEmpty(s)) {
      return false;
    }
    char[] charArray = s.toCharArray();
    char quoteChar = 0;
    for (char c : QUOTE_CHARS) {
      if (charArray[0] == c) {
        quoteChar = c;
        break;
      }
    }
    if (charArray[charArray.length - 1] != quoteChar) {
      return false;
    }
    char prevChar = 0;
    for (int i = 1; i < charArray.length - 1; i++) {
      if (charArray[i] == quoteChar && prevChar != '\\') {
        return false;
      }
      if (prevChar == '\\') {
        // Double escapes cancel out.
        prevChar = 0;
      } else {
        prevChar = charArray[i];
      }
    }
    return prevChar != '\\';
  }

  public static String unquote(String s) {
    if (s == null) {
      return "";
    }
    if (startsWith(s, "'")) {
      return unwrap(s, "'", "'");
    } else if (startsWith(s, "\"")) {
      return unwrap(s, "\"", "\"");
    }
    return s.trim();
  }

  // TODO see if all usages of unquote can use this method instead
  public static String unquoteAndUnescape(String s) {
    if (Strings.isNullOrEmpty(s)) {
      return "";
    }
    if (!isExpressionQuoted(s)) {
      return s.trim();
    }

    if (startsWith(s, "'")) {
      s = unwrap(s, "'", "'");
    } else if (startsWith(s, "\"")) {
      s = unwrap(s, "\"", "\"");
    } else {
      return s.trim();
    }
    // Since we're unquoting, we can unescape the quote characters in the string.
    return s.replace("\\\"", "\"").replace("\\'", "'").replace("\\\\", "\\");
  }

  public static String unwrap(String s, String prefix, String suffix) {
    int start = 0, end = s.length() - 1;

    while (start < s.length()) {
      if (!Character.isWhitespace(s.charAt(start))) {
        break;
      }
      ++start;
    }

    while (end >= 0) {
      if (!Character.isWhitespace(s.charAt(end))) {
        break;
      }
      --end;
    }

    return s.substring(start + prefix.length(), end - suffix.length() + 1);
  }

  private WhitespaceUtils() {}
}
