package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.math.BigDecimal;
import java.math.BigInteger;

@JinjavaDoc(
  value = "Return true if object is an integer or long",
  input = @JinjavaParam(value = "value", type = "object", required = true),
  snippets = {
    @JinjavaSnippet(
      code = "{% if num is integer %}\n" +
      "      <!--code to render if num contains an integral value-->\n" +
      "{% endif %}"
    )
  }
)
public class IsIntegerExpTest implements ExpTest {

  @Override
  public String getName() {
    return "integer";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args) {
    return (
      var instanceof Byte ||
      var instanceof Short ||
      var instanceof Integer ||
      var instanceof Long ||
      var instanceof BigInteger ||
      (var instanceof BigDecimal && ((BigDecimal) var).scale() == 0)
    );
  }
}
