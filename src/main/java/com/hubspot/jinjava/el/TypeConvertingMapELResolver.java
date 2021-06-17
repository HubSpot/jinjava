package com.hubspot.jinjava.el;

import com.hubspot.jinjava.el.ext.CollectionMembershipOperator;
import java.util.Map;
import javax.el.ELContext;
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

    if (
      base instanceof Map &&
      (Boolean) CollectionMembershipOperator.OP.apply(TYPE_CONVERTER, property, base)
    ) {
      Class<?> keyClass = ((Map) base).keySet().iterator().next().getClass();
      value = ((Map) base).get(TYPE_CONVERTER.convert(property, keyClass));
      if (value != null) {
        context.setPropertyResolved(true);
      }
    }

    return value;
  }
}
