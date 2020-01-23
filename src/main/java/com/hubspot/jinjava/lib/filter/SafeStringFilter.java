package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.SafeString;

public interface SafeStringFilter extends Filter {

  default Object safeFilter(Object object, JinjavaInterpreter interpreter, String... args) {
    if (object instanceof SafeString) {
      return new SafeString(filter(object.toString(), interpreter, args).toString());
    }
    return object;
  }
}
