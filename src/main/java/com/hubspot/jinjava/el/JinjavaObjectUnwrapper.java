package com.hubspot.jinjava.el;

import com.hubspot.jinjava.interpret.LazyExpression;
import java.util.Optional;

public class JinjavaObjectUnwrapper implements ObjectUnwrapper {

  @Override
  public Object unwrapObject(Object o) {
    if (o instanceof LazyExpression) {
      o = ((LazyExpression) o).get();
    }

    if (o instanceof Optional) {
      Optional<?> optValue = (Optional<?>) o;
      if (!optValue.isPresent()) {
        return null;
      }

      o = optValue.get();
    }

    return o;
  }
}
