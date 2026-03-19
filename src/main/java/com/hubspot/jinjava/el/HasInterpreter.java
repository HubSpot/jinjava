package com.hubspot.jinjava.el;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public interface HasInterpreter {
  JinjavaInterpreter interpreter();
}
