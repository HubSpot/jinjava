package com.hubspot.jinjava.loader;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RelativePathResolver implements LocationResolver {
  public static final String CURRENT_PATH_CONTEXT_KEY = "current_path";

  @Override
  public String resolve(String path, JinjavaInterpreter interpreter) {
    if (path.startsWith("./") || path.startsWith("../")) {
      String parentPath = interpreter
        .getContext()
        .getCurrentPathStack()
        .peek()
        .orElseGet(
          () ->
            (String) interpreter.getContext().getOrDefault(CURRENT_PATH_CONTEXT_KEY, "")
        );

      Path templatePath = Paths.get(parentPath);
      Path folderPath = templatePath.getParent() != null
        ? templatePath.getParent()
        : Paths.get("");
      if (folderPath != null) {
        return folderPath.resolve(path).normalize().toString();
      }
    }
    return path;
  }
}
