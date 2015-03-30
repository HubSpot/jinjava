package com.hubspot.jinjava.lib.exptest;

import java.util.Objects;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;


@JinjavaDoc(
    value="Check if an object has the same value as another object:\n\n" +
          
          "{% if foo.expression is equalto 42 %}\n" +
          "    the foo attribute evaluates to the constant 42\n" +
          "{% endif %}\n" +
          "This appears to be a useless test as it does exactly the same as the == operator, but it can be useful when used together with the selectattr function:\n" +
          
          "{{ users|selectattr(\"email\", \"equalto\", \"foo@bar.invalid\") }}",
    params={
        @JinjavaParam(value="other", type="object")
    })
public class IsEqualToExpTest implements ExpTest {

  @Override
  public String getName() {
    return "equalto";
  }

  @Override
  public boolean evaluate(Object var, JinjavaInterpreter interpreter,
      Object... args) {
    if(args.length == 0) {
      throw new InterpretException(getName() + " test requires 1 argument");
    }
    
    return Objects.equals(var, args[0]);
  }

}
