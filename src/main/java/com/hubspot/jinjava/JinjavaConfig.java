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

import com.hubspot.jinjava.el.JinjavaInterpreterResolver;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.Context.Library;
import com.hubspot.jinjava.interpret.InterpreterFactory;
import com.hubspot.jinjava.interpret.JinjavaInterpreterFactory;
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

  private Map<Context.Library, Set<String>> disabled;
  private final boolean failOnUnknownTokens;
  private final boolean nestedInterpretationEnabled;
  private final RandomNumberGeneratorStrategy randomNumberGenerator;
  private final boolean validationMode;
  private final long maxStringLength;
  private InterpreterFactory interpreterFactory;
  private TokenScannerSymbols tokenScannerSymbols;
  private ELResolver elResolver;
  private final boolean iterateOverMapKeys;

  public static Builder newBuilder() {
    return new Builder();
  }

  public JinjavaConfig() {
    this(new JinjavaInterpreterFactory());
  }

  public JinjavaConfig(InterpreterFactory interpreterFactory) {
    this(
      StandardCharsets.UTF_8,
      Locale.ENGLISH,
      ZoneOffset.UTC,
      10,
      new HashMap<>(),
      false,
      false,
      false,
      0,
      false,
      0,
      true,
      RandomNumberGeneratorStrategy.THREAD_LOCAL,
      false,
      0,
      interpreterFactory,
      new DefaultTokenScannerSymbols(),
      JinjavaInterpreterResolver.DEFAULT_RESOLVER_READ_ONLY,
      false
    );
  }

  public JinjavaConfig(
    Charset charset,
    Locale locale,
    ZoneId timeZone,
    int maxRenderDepth
  ) {
    this(
      charset,
      locale,
      timeZone,
      maxRenderDepth,
      new HashMap<>(),
      false,
      false,
      false,
      0,
      false,
      0,
      true,
      RandomNumberGeneratorStrategy.THREAD_LOCAL,
      false,
      0,
      new JinjavaInterpreterFactory(),
      new DefaultTokenScannerSymbols(),
      JinjavaInterpreterResolver.DEFAULT_RESOLVER_READ_ONLY,
      false
    );
  }

  private JinjavaConfig(
    Charset charset,
    Locale locale,
    ZoneId timeZone,
    int maxRenderDepth,
    Map<Context.Library, Set<String>> disabled,
    boolean trimBlocks,
    boolean lstripBlocks,
    boolean enableRecursiveMacroCalls,
    int maxMacroRecursionDepth,
    boolean failOnUnknownTokens,
    long maxOutputSize,
    boolean nestedInterpretationEnabled,
    RandomNumberGeneratorStrategy randomNumberGenerator,
    boolean validationMode,
    long maxStringLength,
    InterpreterFactory interpreterFactory,
    TokenScannerSymbols tokenScannerSymbols,
    ELResolver elResolver,
    boolean iterateOverMapKeys
  ) {
    this.charset = charset;
    this.locale = locale;
    this.timeZone = timeZone;
    this.maxRenderDepth = maxRenderDepth;
    this.disabled = disabled;
    this.trimBlocks = trimBlocks;
    this.lstripBlocks = lstripBlocks;
    this.enableRecursiveMacroCalls = enableRecursiveMacroCalls;
    this.maxMacroRecursionDepth = maxMacroRecursionDepth;
    this.failOnUnknownTokens = failOnUnknownTokens;
    this.maxOutputSize = maxOutputSize;
    this.nestedInterpretationEnabled = nestedInterpretationEnabled;
    this.randomNumberGenerator = randomNumberGenerator;
    this.validationMode = validationMode;
    this.maxStringLength = maxStringLength;
    this.interpreterFactory = interpreterFactory;
    this.tokenScannerSymbols = tokenScannerSymbols;
    this.elResolver = elResolver;
    this.iterateOverMapKeys = iterateOverMapKeys;
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

  public boolean isIterateOverMapKeys() {
    return iterateOverMapKeys;
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
    private InterpreterFactory interpreterFactory = new JinjavaInterpreterFactory();
    private TokenScannerSymbols tokenScannerSymbols = new DefaultTokenScannerSymbols();
    private ELResolver elResolver = JinjavaInterpreterResolver.DEFAULT_RESOLVER_READ_ONLY;
    private boolean iterateOverMapKeys;

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

    public Builder withInterperterFactory(InterpreterFactory interperterFactory) {
      this.interpreterFactory = interperterFactory;
      return this;
    }

    public Builder withTokenScannerSymbols(TokenScannerSymbols tokenScannerSymbols) {
      this.tokenScannerSymbols = tokenScannerSymbols;
      return this;
    }

    public Builder withIterateOverMapKeys(boolean iterateOverMapKeys) {
      this.iterateOverMapKeys = iterateOverMapKeys;
      return this;
    }

    public JinjavaConfig build() {
      return new JinjavaConfig(
        charset,
        locale,
        timeZone,
        maxRenderDepth,
        disabled,
        trimBlocks,
        lstripBlocks,
        enableRecursiveMacroCalls,
        maxMacroRecursionDepth,
        failOnUnknownTokens,
        maxOutputSize,
        nestedInterpretationEnabled,
        randomNumberGeneratorStrategy,
        validationMode,
        maxStringLength,
        interpreterFactory,
        tokenScannerSymbols,
        elResolver,
        iterateOverMapKeys
      );
    }
  }
}
