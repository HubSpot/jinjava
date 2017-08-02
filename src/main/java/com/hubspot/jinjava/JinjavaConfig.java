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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.Context.Library;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;

public class JinjavaConfig {

  private final Charset charset;
  private final Locale locale;
  private final ZoneId timeZone;
  private final int maxRenderDepth;
  private final long maxOutputSize;

  private final boolean trimBlocks;
  private final boolean lstripBlocks;

  private final boolean readOnlyResolver;
  private final boolean enableRecursiveMacroCalls;

  private Map<Context.Library, Set<String>> disabled;
  private final boolean failOnUnknownTokens;
  private final boolean nestedInterpretationEnabled;
  private final RandomNumberGeneratorStrategy randomNumberGenerator;

  public static Builder newBuilder() {
    return new Builder();
  }

  public JinjavaConfig() {
    this(StandardCharsets.UTF_8, Locale.ENGLISH, ZoneOffset.UTC, 10, new HashMap<>(), false, false, true, false, false, 0, true, RandomNumberGeneratorStrategy.THREAD_LOCAL);
  }

  public JinjavaConfig(Charset charset, Locale locale, ZoneId timeZone, int maxRenderDepth) {
    this(charset, locale, timeZone, maxRenderDepth, new HashMap<>(), false, false, true, false, false, 0, true, RandomNumberGeneratorStrategy.THREAD_LOCAL);
  }

  private JinjavaConfig(Charset charset,
                        Locale locale,
                        ZoneId timeZone,
                        int maxRenderDepth,
                        Map<Context.Library,
                        Set<String>> disabled,
                        boolean trimBlocks,
                        boolean lstripBlocks,
                        boolean readOnlyResolver,
                        boolean enableRecursiveMacroCalls,
                        boolean failOnUnknownTokens,
                        long maxOutputSize,
                        boolean nestedInterpretationEnabled,
                        RandomNumberGeneratorStrategy randomNumberGenerator) {
    this.charset = charset;
    this.locale = locale;
    this.timeZone = timeZone;
    this.maxRenderDepth = maxRenderDepth;
    this.disabled = disabled;
    this.trimBlocks = trimBlocks;
    this.lstripBlocks = lstripBlocks;
    this.readOnlyResolver = readOnlyResolver;
    this.enableRecursiveMacroCalls = enableRecursiveMacroCalls;
    this.failOnUnknownTokens = failOnUnknownTokens;
    this.maxOutputSize = maxOutputSize;
    this.nestedInterpretationEnabled = nestedInterpretationEnabled;
    this.randomNumberGenerator = randomNumberGenerator;
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

  public long getMaxOutputSize() {
    return maxOutputSize;
  }

  public RandomNumberGeneratorStrategy getRandomNumberGeneratorStrategy() {
    return randomNumberGenerator;
  }

  public boolean isTrimBlocks() {
    return trimBlocks;
  }

  public boolean isLstripBlocks() {
    return lstripBlocks;
  }

  public boolean isReadOnlyResolver() {
    return readOnlyResolver;
  }

  public boolean isEnableRecursiveMacroCalls() {
    return enableRecursiveMacroCalls;
  }

  public Map<Library, Set<String>> getDisabled() {
    return disabled;
  }

  public boolean isFailOnUnknownTokens() {
    return failOnUnknownTokens;
  }

  public boolean isNestedInterpretationEnabled() {
    return nestedInterpretationEnabled;
  }

  public static class Builder {
    private Charset charset = StandardCharsets.UTF_8;
    private Locale locale = Locale.ENGLISH;
    private ZoneId timeZone = ZoneOffset.UTC;
    private int maxRenderDepth = 10;
    private long maxOutputSize = 0; // in bytes
    private Map<Context.Library, Set<String>> disabled = new HashMap<>();

    private boolean trimBlocks;
    private boolean lstripBlocks;

    private boolean readOnlyResolver = true;
    private boolean enableRecursiveMacroCalls;
    private boolean failOnUnknownTokens;
    private boolean nestedInterpretationEnabled = true;
    private RandomNumberGeneratorStrategy randomNumberGeneratorStrategy = RandomNumberGeneratorStrategy.THREAD_LOCAL;

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

    public Builder withDisabled(Map<Context.Library, Set<String>> disabled) {
      this.disabled = disabled;
      return this;
    }

    public Builder withMaxRenderDepth(int maxRenderDepth) {
      this.maxRenderDepth = maxRenderDepth;
      return this;
    }

    public Builder withRandomNumberGeneratorStrategy(RandomNumberGeneratorStrategy randomNumberGeneratorStrategy) {
      this.randomNumberGeneratorStrategy = randomNumberGeneratorStrategy;
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

    public Builder withEnableRecursiveMacroCalls(boolean enableRecursiveMacroCalls) {
      this.enableRecursiveMacroCalls = enableRecursiveMacroCalls;
      return this;
    }

    public Builder withReadOnlyResolver(boolean readOnlyResolver) {
      this.readOnlyResolver = readOnlyResolver;
      return this;
    }

    public Builder withFailOnUnknownTokens(boolean failOnUnknownTokens) {
      this.failOnUnknownTokens = failOnUnknownTokens;
      return this;
    }

    public Builder withMaxOutputSize(long maxOutputSize) {
      this.maxOutputSize = maxOutputSize;
      return this;
    }

    public Builder withNestedInterpretationEnabled(boolean nestedInterpretationEnabled) {
      this.nestedInterpretationEnabled = nestedInterpretationEnabled;
      return this;
    }

    public JinjavaConfig build() {
      return new JinjavaConfig(charset, locale, timeZone, maxRenderDepth, disabled, trimBlocks, lstripBlocks, readOnlyResolver, enableRecursiveMacroCalls, failOnUnknownTokens, maxOutputSize, nestedInterpretationEnabled, randomNumberGeneratorStrategy);
    }

  }

}
