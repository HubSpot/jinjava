package com.hubspot.jinjava.el;

import java.util.Iterator;
import java.util.Map;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.MapELResolver;

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
      Iterator<?> iterator = ((Map) base).keySet().iterator();
      Class<?> keyClass = null;
      while (iterator.hasNext()) {
        Object nextObject = iterator.next();
        if (nextObject != null) {
          keyClass = nextObject.getClass();
          break;
        }
      }

      if (keyClass == null) {
        value = ((Map) base).get(property);
      } else {
        try {
          value = ((Map) base).get(TYPE_CONVERTER.convert(property, keyClass));
          if (value != null) {
            context.setPropertyResolved(true);
          }
        } catch (ELException ex) {
          value = null;
        }
      }
    }

    return value;
  }
}
