package com.hubspot.jinjava.loader;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public interface RelativePathResolver {

  String resolveToAbsolutePath(String path, JinjavaInterpreter interpreter);

}
