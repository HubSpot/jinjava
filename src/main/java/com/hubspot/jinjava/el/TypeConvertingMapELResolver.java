package com.hubspot.jinjava.el;

import java.util.Map;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.MapELResolver;

public class TypeConvertingMapELResolver extends MapELResolver {
  private static final TruthyTypeConverter TYPE_CONVERTER = new TruthyTypeConverter();

  public TypeConvertingMapELResolver(boolean readOnly) {
    super(readOnly);
  }

  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    Object value = super.getValue(context, base, property);

    if (value != null) {
      return value;
    }

    if (base instanceof Map && !((Map) base).isEmpty()) {
      Class<?> keyClass = ((Map) base).keySet().iterator().next().getClass();
      try {
        value = ((Map) base).get(TYPE_CONVERTER.convert(property, keyClass));
        if (value != null) {
          context.setPropertyResolved(true);
        }
      } catch (ELException ex) {
        value = null;
      }
    }

    return value;
  }
}
