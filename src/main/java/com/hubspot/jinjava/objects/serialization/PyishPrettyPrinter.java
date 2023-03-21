package com.hubspot.jinjava.objects.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.google.common.annotations.Beta;
import java.io.IOException;

@Beta
public class PyishPrettyPrinter extends DefaultPrettyPrinter {
  public static final PyishPrettyPrinter INSTANCE = new PyishPrettyPrinter();

  @Override
  public DefaultPrettyPrinter createInstance() {
    return INSTANCE;
  }

  private PyishPrettyPrinter() {
    _objectIndenter = FixedSpaceIndenter.instance;
    _spacesInObjectEntries = false;
  }

  @Override
  public void beforeArrayValues(JsonGenerator jg) {}

  @Override
  public void writeEndArray(JsonGenerator jg, int nrOfValues) throws IOException {
    if (!this._arrayIndenter.isInline()) {
      --this._nesting;
    }
    jg.writeRaw(']');
  }

  @Override
  public void writeObjectFieldValueSeparator(JsonGenerator jg) throws IOException {
    jg.writeRaw(": ");
  }

  @Override
  public void beforeObjectEntries(JsonGenerator jg) {}

  @Override
  public void writeEndObject(JsonGenerator jg, int nrOfEntries) throws IOException {
    if (!this._objectIndenter.isInline()) {
      --this._nesting;
    }
    jg.writeRaw("} ");
  }
}
