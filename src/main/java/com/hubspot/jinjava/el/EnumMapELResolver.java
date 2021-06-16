package com.hubspot.jinjava.el;

import java.util.Map;
import java.util.Optional;
import javax.el.ELContext;
import javax.el.MapELResolver;

public class EnumMapELResolver extends MapELResolver {

  public EnumMapELResolver(boolean readOnly) {
    super(readOnly);
  }

  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    if (context == null) {
      throw new NullPointerException("context is null");
    }

    if (base instanceof Map && property instanceof String) {
      for (Object key : ((Map) base).keySet()) {
        if (key.getClass().isEnum()) {
          Optional<Object> value = getValueForEnumKey(base, property);
          if (value.isPresent()) {
            context.setPropertyResolved(true);
            return value.get();
          }
        }
      }
    }
    return super.getValue(context, base, property);
  }

  private Optional<Object> getValueForEnumKey(Object base, Object property) {
    for (Object key : ((Map) base).keySet()) {
      if (key.equals(property) || key.toString().equalsIgnoreCase((String) property)) {
        return Optional.of(((Map) base).get(key));
      }
    }
    return Optional.empty();
  }
}
