package com.hubspot.jinjava.util;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class EscapeHubLUtil {

  public static <T> Map<String, T> escapeHubL(Map<String, T> inputMap) {
    return inputMap
      .entrySet()
      .stream()
      .filter(entry -> entry.getKey() != null)
      .collect(Collectors.toMap(Entry::getKey, entry -> escapeHubL(entry.getValue())));
  }

  public static <T> T escapeHubL(T input) {
    if (input instanceof String) {
      return (T) escapeHubL((String) input);
    } else if (input instanceof Map) {
      return (T) escapeHubL((Map<String, ?>) input);
    } else if (input instanceof Collection) {
      return (T) ((Collection<?>) input).stream()
        .map(EscapeHubLUtil::escapeHubL)
        .collect(Collectors.toList());
    }
    return input;
  }

  public static String escapeHubL(String text) {
    return EscapeJinjavaFilter.escapeFullJinjavaEntities(text);
  }
}
