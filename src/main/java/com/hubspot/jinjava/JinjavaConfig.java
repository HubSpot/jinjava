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

import static com.hubspot.jinjava.lib.fn.Functions.DEFAULT_RANGE_LIMIT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.jinjava.el.JinjavaInterpreterResolver;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.Context.Library;
import com.hubspot.jinjava.interpret.InterpreterFactory;
import com.hubspot.jinjava.interpret.JinjavaInterpreterFactory;
import com.hubspot.jinjava.mode.DefaultExecutionMode;
import com.hubspot.jinjava.mode.ExecutionMode;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
import com.hubspot.jinjava.tree.parse.DefaultTokenScannerSymbols;
import com.hubspot.jinjava.tree.parse.TokenScannerSymbols;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.el.ELResolver;

public class JinjavaConfig {
  private final Charset charset;
  private final Locale locale;
  private final ZoneId timeZone;
  private final int maxRenderDepth;
  private final long maxOutputSize;

  private final boolean trimBlocks;
  private final boolean lstripBlocks;

  private final boolean enableRecursiveMacroCalls;
  private final int maxMacroRecursionDepth;

  private final Map<Context.Library, Set<String>> disabled;
  private final boolean failOnUnknownTokens;
  private final boolean nestedInterpretationEnabled;
  private final RandomNumberGeneratorStrategy randomNumberGenerator;
  private final boolean validationMode;
  private final long maxStringLength;
  private final int maxListSize;
  private final int maxMapSize;
  private final int rangeLimit;
  private final int maxNumDeferredTokens;
  private final InterpreterFactory interpreterFactory;
  private TokenScannerSymbols tokenScannerSymbols;
  private final ELResolver elResolver;
  private final ExecutionMode executionMode;
  private final LegacyOverrides legacyOverrides;
  private final boolean enablePreciseDivideFilter;
  private final ObjectMapper objectMapper;

  public static Builder newBuilder() {
    return new Builder();
  }

  public JinjavaConfig() {
    this(newBuilder());
  }

  public JinjavaConfig(InterpreterFactory interpreterFactory) {
    this(newBuilder().withInterperterFactory(interpreterFactory));
  }

  public JinjavaConfig(
    Charset charset,
    Locale locale,
    ZoneId timeZone,
    int maxRenderDepth
  ) {
    this(
      newBuilder()
        .withCharset(charset)
        .withLocale(locale)
        .withTimeZone(timeZone)
        .withMaxRenderDepth(maxRenderDepth)
    );
  }

  private JinjavaConfig(Builder builder) {
    charset = builder.charset;
    locale = builder.locale;
    timeZone = builder.timeZone;
    maxRenderDepth = builder.maxRenderDepth;
    disabled = builder.disabled;
    trimBlocks = builder.trimBlocks;
    lstripBlocks = builder.lstripBlocks;
    enableRecursiveMacroCalls = builder.enableRecursiveMacroCalls;
    maxMacroRecursionDepth = builder.maxMacroRecursionDepth;
    failOnUnknownTokens = builder.failOnUnknownTokens;
    maxOutputSize = builder.maxOutputSize;
    nestedInterpretationEnabled = builder.nestedInterpretationEnabled;
    randomNumberGenerator = builder.randomNumberGeneratorStrategy;
    validationMode = builder.validationMode;
    maxStringLength = builder.maxStringLength;
    maxListSize = builder.maxListSize;
    maxMapSize = builder.maxMapSize;
    rangeLimit = builder.rangeLimit;
    maxNumDeferredTokens = builder.maxNumDeferredTokens;
    interpreterFactory = builder.interpreterFactory;
    tokenScannerSymbols = builder.tokenScannerSymbols;
    elResolver = builder.elResolver;
    executionMode = builder.executionMode;
    legacyOverrides = builder.legacyOverrides;
    enablePreciseDivideFilter = builder.enablePreciseDivideFilter;
    objectMapper = builder.objectMapper;
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

  public int getMaxListSize() {
    return maxListSize;
  }

  public int getMaxMapSize() {
    return maxMapSize;
  }

  public int getRangeLimit() {
    return rangeLimit;
  }

  public int getMaxNumDeferredTokens() {
    return maxNumDeferredTokens;
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

  public boolean isEnableRecursiveMacroCalls() {
    return enableRecursiveMacroCalls;
  }

  public int getMaxMacroRecursionDepth() {
    return maxMacroRecursionDepth;
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

  public boolean isValidationMode() {
    return validationMode;
  }

  public long getMaxStringLength() {
    return maxStringLength;
  }

  public InterpreterFactory getInterpreterFactory() {
    return interpreterFactory;
  }

  public TokenScannerSymbols getTokenScannerSymbols() {
    return tokenScannerSymbols;
  }

  public void setTokenScannerSymbols(TokenScannerSymbols tokenScannerSymbols) {
    this.tokenScannerSymbols = tokenScannerSymbols;
  }

  public ELResolver getElResolver() {
    return elResolver;
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  /**
   * @deprecated  Replaced by {@link LegacyOverrides#isIterateOverMapKeys()}
   */
  @Deprecated
  public boolean isIterateOverMapKeys() {
    return legacyOverrides.isIterateOverMapKeys();
  }

  public ExecutionMode getExecutionMode() {
    return executionMode;
  }

  public LegacyOverrides getLegacyOverrides() {
    return legacyOverrides;
  }

  public boolean getEnablePreciseDivideFilter() {
    return enablePreciseDivideFilter;
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

    private boolean enableRecursiveMacroCalls;
    private int maxMacroRecursionDepth;
    private boolean failOnUnknownTokens;
    private boolean nestedInterpretationEnabled = true;
    private RandomNumberGeneratorStrategy randomNumberGeneratorStrategy =
      RandomNumberGeneratorStrategy.THREAD_LOCAL;
    private boolean validationMode = false;
    private long maxStringLength = 0;
    private int rangeLimit = DEFAULT_RANGE_LIMIT;
    private int maxNumDeferredTokens = 1000;
    private InterpreterFactory interpreterFactory = new JinjavaInterpreterFactory();
    private TokenScannerSymbols tokenScannerSymbols = new DefaultTokenScannerSymbols();
    private ELResolver elResolver = JinjavaInterpreterResolver.DEFAULT_RESOLVER_READ_ONLY;
    private int maxListSize = Integer.MAX_VALUE;
    private int maxMapSize = Integer.MAX_VALUE;
    private ExecutionMode executionMode = DefaultExecutionMode.instance();
    private LegacyOverrides legacyOverrides = LegacyOverrides.NONE;
    private boolean enablePreciseDivideFilter = false;
    private ObjectMapper objectMapper = new ObjectMapper();

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

    public Builder withRandomNumberGeneratorStrategy(
      RandomNumberGeneratorStrategy randomNumberGeneratorStrategy
    ) {
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

    public Builder withMaxMacroRecursionDepth(int maxMacroRecursionDepth) {
      this.maxMacroRecursionDepth = maxMacroRecursionDepth;
      return this;
    }

    public Builder withReadOnlyResolver(boolean readOnlyResolver) {
      this.elResolver =
        readOnlyResolver
          ? JinjavaInterpreterResolver.DEFAULT_RESOLVER_READ_ONLY
          : JinjavaInterpreterResolver.DEFAULT_RESOLVER_READ_WRITE;
      return this;
    }

    public Builder withElResolver(ELResolver elResolver) {
      this.elResolver = elResolver;
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

    public Builder withValidationMode(boolean validationMode) {
      this.validationMode = validationMode;
      return this;
    }

    public Builder withMaxStringLength(long maxStringLength) {
      this.maxStringLength = maxStringLength;
      return this;
    }

    public Builder withMaxListSize(int maxListSize) {
      this.maxListSize = maxListSize;
      return this;
    }

    public Builder withMaxMapSize(int maxMapSize) {
      this.maxMapSize = maxMapSize;
      return this;
    }

    public Builder withRangeLimit(int rangeLimit) {
      this.rangeLimit = rangeLimit;
      return this;
    }

    public Builder withMaxNumDeferredTokens(int maxNumDeferredTokens) {
      this.maxNumDeferredTokens = maxNumDeferredTokens;
      return this;
    }

    public Builder withInterperterFactory(InterpreterFactory interperterFactory) {
      this.interpreterFactory = interperterFactory;
      return this;
    }

    public Builder withTokenScannerSymbols(TokenScannerSymbols tokenScannerSymbols) {
      this.tokenScannerSymbols = tokenScannerSymbols;
      return this;
    }

    /**
     * @deprecated  Replaced by {@link LegacyOverrides.Builder#withIterateOverMapKeys(boolean)}}
     */
    @Deprecated
    public Builder withIterateOverMapKeys(boolean iterateOverMapKeys) {
      return withLegacyOverrides(
        LegacyOverrides
          .Builder.from(legacyOverrides)
          .withIterateOverMapKeys(iterateOverMapKeys)
          .build()
      );
    }

    public Builder withExecutionMode(ExecutionMode executionMode) {
      this.executionMode = executionMode;
      return this;
    }

    public Builder withLegacyOverrides(LegacyOverrides legacyOverrides) {
      this.legacyOverrides = legacyOverrides;
      return this;
    }

    public Builder withEnablePreciseDivideFilter(boolean enablePreciseDivideFilter) {
      this.enablePreciseDivideFilter = enablePreciseDivideFilter;
      return this;
    }

    public Builder withObjectMapper(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
      return this;
    }

    public JinjavaConfig build() {
      return new JinjavaConfig(this);
    }
  }
}
