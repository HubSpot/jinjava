package com.hubspot.jinjava.interpret;

import java.util.Map;

public interface RenderTimings {
  void start(JinjavaInterpreter interpreter, String name);
  void end(JinjavaInterpreter interpreter, String name);
  void end(JinjavaInterpreter interpreter, String name, Map<String, Object> data);
}
