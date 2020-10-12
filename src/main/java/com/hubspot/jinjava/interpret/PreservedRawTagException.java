/**********************************************************************
 * Copyright (c) 2020 HubSpot Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************/
package com.hubspot.jinjava.interpret;

public class PreservedRawTagException extends InterpretException {
  private String preservedImage;

  public PreservedRawTagException() {
    super("Encountered a preserved raw tag");
  }

  public PreservedRawTagException(
    String preservedImage,
    int lineNumber,
    int startPosition
  ) {
    super("Encountered a preserved raw tag", lineNumber, startPosition);
    this.preservedImage = preservedImage;
  }

  public String getPreservedImage() {
    return preservedImage;
  }
}
