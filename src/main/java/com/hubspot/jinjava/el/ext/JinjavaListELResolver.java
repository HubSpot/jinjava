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
      final Object superValue = super.getValue(context, base, property);
      if (superValue != null) {
        return superValue;
      }
      return getValueNegativeIndex(context, base, property);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Based on ListELResolver::getValue.  If the base object is a list, and the
   * given index is negative, returns the value at the given index. The index is
   * specified by the property argument, and coerced into an integer. If the
   * coercion could not be performed, an IllegalArgumentException is thrown. If
   * the index is positive or out of bounds, null is returned. If the base is a
   * List, the propertyResolved property of the ELContext object must be set to
   * true by this resolver, before returning. If this property is not true after
   * this method is called, the caller should ignore the return value.
   *
   * <p>-1: the last element
   * -2: the second to last element
   * etc.
   *
   * @param context
   *            The context of this evaluation.
   * @param base
   *            The list to analyze. Only bases of type List are handled by this resolver.
   * @param property
   *            The index of the element in the list to return the acceptable type for. Will be
   *            coerced into an integer, but otherwise ignored by this resolver.
   * @return If the propertyResolved property of ELContext was set to true, then the value at the
   *         given index or null if the index was out of bounds. Otherwise, undefined.
   * @throws IllegalArgumentException
   *             if the property could not be coerced into an integer.
   * @throws NullPointerException
   *             if context is null
   * @throws ELException
   *             if an exception was thrown while performing the property or variable resolution.
   *             The thrown exception must be included as the cause property of this exception, if
   *             available.
   */
  private Object getValueNegativeIndex(ELContext context, Object base, Object property) {
    if (context == null) {
      throw new NullPointerException("context is null");
    }

    Object result = null;
    if (isResolvable(base)) {
      int index = toIndex(property);
      List<?> list = (List<?>) base;
      // Handle negative indeces.  Assume 0 and positive indeces are handled
      // elsewhere.
      //
      // For example, with a 4 element list, -4 means the element at index 0.
      // -5 is out of bounds.
      if ((index < 0) && (index >= (-1 * list.size()))) {
         result = list.get(list.size() + index);
      }
      context.setPropertyResolved(true);
    }
    return result;
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
    } catch (IllegalArgumentException e) {
      /* */ }
  }

}
