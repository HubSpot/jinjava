package com.hubspot.jinjava.interpret;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;

public interface InterpreterFactory {
    JinjavaInterpreter newInstance(JinjavaInterpreter orig);
    JinjavaInterpreter newInstance(Jinjava application, Context context, JinjavaConfig renderConfig);
}
