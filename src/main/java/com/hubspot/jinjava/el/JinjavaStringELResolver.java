/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hubspot.jinjava.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotWritableException;

class JinjavaStringELResolver extends ELResolver {

  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    if (!isResolvable(base, property)) {
      return null;
    }
    context.setPropertyResolved(true);

    int index;
    try {
      index = Integer.parseInt(property.toString());
    } catch (NumberFormatException e) {
      return null;
    }

    String value = (String) base;
    if (index < 0) {
      index += value.length();
    }
    // Match the UTF-16 indexing used by string slices and the length filter.
    return index >= 0 && index < value.length()
      ? value.substring(index, index + 1)
      : null;
  }

  @Override
  public Class<?> getType(ELContext context, Object base, Object property) {
    if (isResolvable(base, property)) {
      context.setPropertyResolved(true);
      return String.class;
    }
    return null;
  }

  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property) {
    if (isResolvable(base, property)) {
      context.setPropertyResolved(true);
      return true;
    }
    return false;
  }

  @Override
  public void setValue(ELContext context, Object base, Object property, Object value) {
    if (isResolvable(base, property)) {
      context.setPropertyResolved(true);
      throw new PropertyNotWritableException("String indices are read-only");
    }
  }

  @Override
  public Class<?> getCommonPropertyType(ELContext context, Object base) {
    return base instanceof String ? Integer.class : null;
  }

  @Override
  public Iterator<FeatureDescriptor> getFeatureDescriptors(
    ELContext context,
    Object base
  ) {
    return null;
  }

  private static boolean isResolvable(Object base, Object property) {
    if (!(base instanceof String) || property == null) {
      return false;
    }
    String index = property.toString();
    int start = index.startsWith("-") ? 1 : 0;
    if (start == index.length()) {
      return false;
    }
    for (int i = start; i < index.length(); i++) {
      if (!Character.isDigit(index.charAt(i))) {
        return false;
      }
    }
    return true;
  }
}
