package com.hubspot.jinjava.util;

public class CharArrayUtils {

  public static boolean charArrayRegionMatches(char[] value, int startPos, CharSequence toMatch) {
    int matchLen = toMatch.length(),
        endPos = startPos + matchLen;

    if (endPos > value.length) {
      return false;
    }

    for (int matchIndex = 0, i = startPos; i < endPos; i++, matchIndex++) {
      if (value[i] != toMatch.charAt(matchIndex)) {
        return false;
      }
    }

    return true;
  }

}
