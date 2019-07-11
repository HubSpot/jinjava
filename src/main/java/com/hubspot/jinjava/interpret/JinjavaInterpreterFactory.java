package com.hubspot.jinjava.interpret;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;

public class JinjavaInterpreterFactory implements InterpreterFactory {
    @Override
    public JinjavaInterpreter newInstance(JinjavaInterpreter orig) {
        return new JinjavaInterpreter(orig);
    }

    @Override
    public JinjavaInterpreter newInstance(Jinjava application, Context context, JinjavaConfig renderConfig) {
        return new JinjavaInterpreter(application, context, renderConfig);
    }
}

