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
package com.hubspot.jinjava;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;

public class JinjavaConfig {

  private final Charset charset;
  private final Locale locale;
  private final ZoneId timeZone;
  private final int maxRenderDepth;
  
  public JinjavaConfig() {
    charset = StandardCharsets.UTF_8;
    locale = Locale.ENGLISH;
    timeZone = ZoneOffset.UTC;
    maxRenderDepth = 10;
  }

  public JinjavaConfig(Charset charset, Locale locale, ZoneId timeZone, int maxRenderDepth) {
    this.charset = charset;
    this.locale = locale;
    this.timeZone = timeZone;
    this.maxRenderDepth = maxRenderDepth;
  }
  
  public Charset getCharset() {
    return charset;
  }

  public Locale getLocale() {
    return locale;
  }

  public ZoneId getTimeZone() {
    return timeZone;
  }

  public int getMaxRenderDepth() {
    return maxRenderDepth;
  }

}