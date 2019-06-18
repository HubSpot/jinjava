package com.hubspot.jinjava.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class RelativePathResolver {

  public static String resolveToAbsolutePath(String path, JinjavaInterpreter interpreter) {
    if (path.startsWith("./")) {
      String parentPath = interpreter.getContext().getCurrentPathStack().peek()
          .orElseGet(() -> (String) interpreter.getContext().get(Context.CURRENT_PATH_KEY));

      Path templatePath = Paths.get(parentPath);
      Path folderPath = templatePath.getParent() != null ? templatePath.getParent() : Paths.get("");
      return folderPath.resolve(path).normalize().toString();
    }
    return path;
  }

}
