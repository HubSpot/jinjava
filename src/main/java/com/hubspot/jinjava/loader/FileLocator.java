/**********************************************************************
Copyright (c) 2014 HubSpot Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 **********************************************************************/
package com.hubspot.jinjava.loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.io.Files;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class FileLocator implements ResourceLocator {

  private File baseDir;

  /**
   * initializes the locator with the base dir for relative paths set to the current working dir
   */
  public FileLocator() {
    this.baseDir = new File(".");
  }

  public FileLocator(File baseDir) throws FileNotFoundException {
    if (!baseDir.exists()) {
      throw new FileNotFoundException(String.format("Specified baseDir [%s] does not exist", baseDir.getAbsolutePath()));
    }
    this.baseDir = baseDir;
  }

  private File resolveFileName(String name) {
    File f = new File(name);

    if (f.isAbsolute()) {
      return f;
    }

    return new File(baseDir, name);
  }

  @Override
  public String getString(String name, Charset encoding, JinjavaInterpreter interpreter) throws IOException {
    File file = resolveFileName(name);

    if (!file.exists() || !file.isFile()) {
      throw new ResourceNotFoundException("Couldn't find resource: " + file);
    }

    return Files.toString(file, encoding);
  }

}
