package com.hubspot.jinjava.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RelativePathResolver {

  public static String resolveToAbsolutePath(String parentPath, String relativePath) {
    if (relativePath.startsWith("./")) {
      Path templatePath = Paths.get(parentPath);
      Path folderPath = templatePath.getParent() != null ? templatePath.getParent() : Paths.get("");
      return folderPath.resolve(relativePath).normalize().toString();
    }
    return relativePath;
  }

}
