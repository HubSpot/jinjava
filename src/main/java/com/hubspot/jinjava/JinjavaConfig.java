/**********************************************************************
 * Copyright (c) 2014 HubSpot Inc.
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

  private final boolean trimBlocks;
  private final boolean lstripBlocks;

  public static Builder newBuilder() {
    return new Builder();
  }

  public JinjavaConfig() {
    this(StandardCharsets.UTF_8, Locale.ENGLISH, ZoneOffset.UTC, 10, false, false);
  }

  public JinjavaConfig(Charset charset, Locale locale, ZoneId timeZone, int maxRenderDepth) {
    this(charset, locale, timeZone, maxRenderDepth, false, false);
  }

  private JinjavaConfig(Charset charset,
      Locale locale,
      ZoneId timeZone,
      int maxRenderDepth,
      boolean trimBlocks,
      boolean lstripBlocks) {
    this.charset = charset;
    this.locale = locale;
    this.timeZone = timeZone;
    this.maxRenderDepth = maxRenderDepth;
    this.trimBlocks = trimBlocks;
    this.lstripBlocks = lstripBlocks;
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

  public boolean isTrimBlocks() {
    return trimBlocks;
  }

  public boolean isLstripBlocks() {
    return lstripBlocks;
  }

  public static class Builder {
    private Charset charset = StandardCharsets.UTF_8;
    private Locale locale = Locale.ENGLISH;
    private ZoneId timeZone = ZoneOffset.UTC;
    private int maxRenderDepth = 10;

    private boolean trimBlocks;
    private boolean lstripBlocks;

    private Builder() {}

    public Builder withCharset(Charset charset) {
      this.charset = charset;
      return this;
    }

    public Builder withLocale(Locale locale) {
      this.locale = locale;
      return this;
    }

    public Builder withTimeZone(ZoneId timeZone) {
      this.timeZone = timeZone;
      return this;
    }

    public Builder withMaxRenderDepth(int maxRenderDepth) {
      this.maxRenderDepth = maxRenderDepth;
      return this;
    }

    public Builder withTrimBlocks(boolean trimBlocks) {
      this.trimBlocks = trimBlocks;
      return this;
    }

    public Builder withLstripBlocks(boolean lstripBlocks) {
      this.lstripBlocks = lstripBlocks;
      return this;
    }

    public JinjavaConfig build() {
      return new JinjavaConfig(charset, locale, timeZone, maxRenderDepth, trimBlocks, lstripBlocks);
    }
  }

}
