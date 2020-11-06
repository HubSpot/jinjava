package com.hubspot.jinjava.el.ext.eager;

import de.odysseus.el.tree.Bindings;
import javax.el.ELContext;

public interface EvalResultHolder {
  Object getEvalResult();

  Object eval(Bindings bindings, ELContext elContext);
}
