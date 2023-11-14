package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.serialization.LengthLimitingWriter;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicInteger;

@JinjavaDoc(
  value = "Writes object as a JSON string",
  input = @JinjavaParam(
    value = "object",
    desc = "Object to write to JSON",
    required = true
  ),
  snippets = { @JinjavaSnippet(code = "{{object|tojson}}") }
)
public class ToJsonFilter implements Filter {

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    try {
      if (interpreter.getConfig().getMaxOutputSize() > 0) {
        AtomicInteger remainingLength = new AtomicInteger(
          (int) Math.min(Integer.MAX_VALUE, interpreter.getConfig().getMaxOutputSize())
        );
        Writer writer = new LengthLimitingWriter(new CharArrayWriter(), remainingLength);
        interpreter.getConfig().getObjectMapper().writeValue(writer, var);
        return writer.toString();
      } else {
        return interpreter.getConfig().getObjectMapper().writeValueAsString(var);
      }
    } catch (IOException e) {
      if (e.getCause() instanceof DeferredValueException) {
        throw (DeferredValueException) e.getCause();
      }
      PyishObjectMapper.handleLengthLimitingException(e);
      throw new InvalidInputException(interpreter, this, InvalidReason.JSON_WRITE);
    }
  }

  @Override
  public String getName() {
    return "tojson";
  }
}
