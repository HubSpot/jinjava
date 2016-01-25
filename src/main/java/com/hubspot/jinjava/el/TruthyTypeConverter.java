package com.hubspot.jinjava.el;

import com.hubspot.jinjava.util.ObjectTruthValue;

import de.odysseus.el.misc.TypeConverterImpl;

public class TruthyTypeConverter extends TypeConverterImpl {
  private static final long serialVersionUID = 1L;

  @Override
  protected Boolean coerceToBoolean(Object value) {
    return Boolean.valueOf(ObjectTruthValue.evaluate(value));
  }

}
