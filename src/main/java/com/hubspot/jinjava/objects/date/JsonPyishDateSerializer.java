package com.hubspot.jinjava.objects.date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class JsonPyishDateSerializer extends JsonSerializer<PyishDate> {

  @Override
  public void serialize(
    PyishDate pyishDate,
    JsonGenerator jsonGenerator,
    SerializerProvider serializerProvider
  )
    throws IOException {
    jsonGenerator.writeString(
      DateTimeFormatter
        .ofPattern(PyishDate.PYISH_DATE_FORMAT)
        .format(pyishDate.toDateTime())
    );
  }
}
