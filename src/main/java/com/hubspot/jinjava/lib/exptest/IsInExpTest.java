package com.hubspot.jinjava.lib.exptest;

import static com.hubspot.jinjava.lib.exptest.IsIterableExpTest.isIterable;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
  value = "Returns true if value is contained in the iterable",
  input = @JinjavaParam(value = "value", type = "object", required = true),
  params = @JinjavaParam(
    value = "list",
    type = "object",
    desc = "The iterable to check for the value",
    required = true
  ),
  snippets = {
    @JinjavaSnippet(code = "{{ 2 is in [1, 2, 3] }}"),
    @JinjavaSnippet(code = "{{ 'b' is in 'abc' }}"),
    @JinjavaSnippet(code = "{{ 'k2' is in {'k1':'v1', 'k2':'v2'} }}"),
  }
)
public class IsInExpTest extends CollectionExpTest {

  @Override
  public String getName() {
    return "in";
  }

  @Override
  public boolean evaluate(Object value, JinjavaInterpreter interpreter, Object... args) {
    if (args == null || args.length == 0) {
      return false;
    }
    if (!isIterable(args[0])) {
      throw new InvalidArgumentException(
        interpreter,
        this,
        InvalidReason.NOT_ITERABLE,
        0,
        args[0]
      );
    }
    return (Boolean) COLLECTION_MEMBERSHIP_OPERATOR.apply(TYPE_CONVERTER, value, args[0]);
  }
}
