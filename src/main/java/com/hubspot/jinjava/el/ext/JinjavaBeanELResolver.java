package com.hubspot.jinjava.el.ext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.BeanELResolver;
import javax.el.ELContext;

/**
 * {@link BeanELResolver} supporting snake case property names.
 */
public class JinjavaBeanELResolver extends BeanELResolver {
   /**
    * Pattern to convert snake case to property names.
    */
   private static final Pattern SNAKE_CASE = Pattern.compile("_([^_]?)");

   /**
    * Creates a new read/write {@link JinjavaBeanELResolver}.
    */
   public JinjavaBeanELResolver() {
   }

   /**
    * Creates a new {@link JinjavaBeanELResolver} whose read-only status is determined by the given parameter.
    */
   public JinjavaBeanELResolver(boolean readOnly) {
      super(readOnly);
   }

   @Override
   public Class<?> getType(ELContext context, Object base, Object property) {
      return super.getType(context, base, transformPropertyName(property));
   }

   @Override
   public Object getValue(ELContext context, Object base, Object property) {
      return super.getValue(context, base, transformPropertyName(property));
   }

   @Override
   public boolean isReadOnly(ELContext context, Object base, Object property) {
      return super.isReadOnly(context, base, transformPropertyName(property));
   }

   @Override
   public void setValue(ELContext context, Object base, Object property, Object value) {
      super.setValue(context, base, transformPropertyName(property), value);
   }

   /**
    * Transform snake case to property name.
    */
   private String transformPropertyName(Object property) {
      if (property == null) {
         return null;
      }

      String name = property.toString();
      Matcher m = SNAKE_CASE.matcher(name);

      StringBuffer result = new StringBuffer(name.length());
      while (m.find()) {
         String replacement = m.group(1).toUpperCase();
         m.appendReplacement(result, replacement);
      }
      m.appendTail(result);

      return result.toString();
   }

}
