package com.hubspot.jinjava.el;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import de.odysseus.el.util.SimpleContext;
import java.lang.reflect.Method;
import jakarta.el.ELResolver;

public class JinjavaELContext extends SimpleContext {
  private JinjavaInterpreter interpreter;
  private MacroFunctionMapper functionMapper;

  public JinjavaELContext() {
    super();
  }

  public JinjavaELContext(JinjavaInterpreter interpreter, ELResolver resolver) {
    super(resolver);
    this.interpreter = interpreter;
  }

  @Override
  public MacroFunctionMapper getFunctionMapper() {
    if (functionMapper == null) {
      functionMapper = new MacroFunctionMapper(interpreter);
    }
    return functionMapper;
  }

  @Override
  public void setFunction(String prefix, String localName, Method method) {
    getFunctionMapper().setFunction(prefix, localName, method);
  }
}
