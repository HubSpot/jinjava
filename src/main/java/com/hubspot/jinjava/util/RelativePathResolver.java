package com.hubspot.jinjava.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class RelativePathResolver {

  public static String resolveToAbsolutePath(String relativePath, JinjavaInterpreter interpreter) {
    if (relativePath.startsWith("./")) {
      String parentPath = interpreter.getContext().getCurrentPathStack().peek().orElseGet(() -> (String) interpreter.getContext().get("current_path"));
      Path templatePath = Paths.get(parentPath);
      Path folderPath = templatePath.getParent() != null ? templatePath.getParent() : Paths.get("");
      return folderPath.resolve(relativePath).normalize().toString();
    }
    return relativePath;
  }

}
