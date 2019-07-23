package com.hubspot.jinjava.el.ext;

import java.util.List;
import javax.el.ELContext;
import javax.el.ListELResolver;

public class JinjavaListELResolver extends ListELResolver {

  public JinjavaListELResolver(boolean readOnly) {
    super(readOnly);
  }

  @Override
  public Class<?> getType(ELContext context, Object base, Object property) {
    try {
      return super.getType(context, base, property);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property) {
    try {
      return super.isReadOnly(context, base, property);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public Object getValue(ELContext context, Object base, Object property) {
    try {
      // If we're dealing with a negative index, convert it to a positive one.
      if (isResolvable(base)) {
        int index = toIndex(property);
        if (index < 0) {
          // Leave the range checking to the superclass.
          property = index + ((List<?>) base).size();
        }
      }
      return super.getValue(context, base, property);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Copied from the unfortunately private ListELResolver.isResolvable
   */
  private static boolean isResolvable(Object base) {
    return base instanceof List<?>;
  }

  /**
   * Convert the given property to an index.  Inspired by
   * ListELResolver.toIndex, but without the base param since we only use it for
   * getValue where base is null.
   *
   * @param property
   *            The name of the property to analyze. Will be coerced to a String.
   * @return The index of property in base.
   * @throws IllegalArgumentException
   *             if property cannot be coerced to an integer.
   */
  private static int toIndex(Object property) {
     int index = 0;
     if (property instanceof Number) {
       index = ((Number) property).intValue();
     } else if (property instanceof String) {
       try {
         // ListELResolver uses valueOf, but findbugs complains.
         index = Integer.parseInt((String) property);
       } catch (NumberFormatException e) {
         throw new IllegalArgumentException("Cannot parse list index: " + property);
       }
     } else if (property instanceof Character) {
       index = ((Character) property).charValue();
     } else if (property instanceof Boolean) {
       index = ((Boolean) property).booleanValue() ? 1 : 0;
     } else {
       throw new IllegalArgumentException("Cannot coerce property to list index: " + property);
     }
     return index;
   }

  @Override
  public void setValue(ELContext context, Object base, Object property, Object value) {
    try {
      super.setValue(context, base, property, value);
    } catch (IllegalArgumentException ignored) {
    }
  }
}
