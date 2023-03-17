package com.hubspot.jinjava.objects.collections;

import com.google.common.base.CaseFormat;
import com.hubspot.jinjava.lib.filter.AllowSnakeCaseFilter;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import java.io.IOException;
import java.util.Map;

public class SnakeCaseAccessibleMap extends PyMap implements PyishSerializable {

  public SnakeCaseAccessibleMap(Map<String, Object> map) {
    super(map);
  }

  @Override
  public Object get(Object key) {
    Object result = super.get(key);
    if (result == null && key instanceof String) {
      return getWithCamelCase((String) key);
    }
    return result;
  }

  private Object getWithCamelCase(String key) {
    if (key == null) {
      return null;
    }
    if (key.indexOf('_') == -1) {
      return null;
    }
    return super.get(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, key));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Appendable & CharSequence> T appendPyishString(T appendable)
    throws IOException {
    return (T) appendable
      .append(PyishSerializable.writeValueAsString(toMap()))
      .append('|')
      .append(AllowSnakeCaseFilter.NAME);
  }
}
