package com.hubspot.jinjava.loader;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public interface LocationResolver {

  String resolve(String path, JinjavaInterpreter interpreter);

}
