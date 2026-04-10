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
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.el.JinjavaInterpreterResolver;
import com.hubspot.jinjava.el.JinjavaObjectUnwrapper;
import com.hubspot.jinjava.el.JinjavaProcessors;
import com.hubspot.jinjava.el.ObjectUnwrapper;
import com.hubspot.jinjava.el.ext.AllowlistMethodValidator;
import com.hubspot.jinjava.el.ext.AllowlistReturnTypeValidator;
import com.hubspot.jinjava.features.FeatureConfig;
import com.hubspot.jinjava.features.Features;
import com.hubspot.jinjava.interpret.Context.Library;
import com.hubspot.jinjava.interpret.InterpreterFactory;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreterFactory;
import com.hubspot.jinjava.mode.DefaultExecutionMode;
import com.hubspot.jinjava.mode.ExecutionMode;
import com.hubspot.jinjava.objects.date.CurrentDateTimeProvider;
import com.hubspot.jinjava.objects.date.DateTimeProvider;
import com.hubspot.jinjava.random.RandomNumberGeneratorStrategy;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.parse.DefaultTokenScannerSymbols;
import com.hubspot.jinjava.tree.parse.TokenScannerSymbols;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.el.ELResolver;
import org.immutables.value.Value;

@Value.Immutable(singleton = true)
@JinjavaImmutableStyle.WithStyle
public class JinjavaConfig {

  public JinjavaConfig() {}

  @Value.Default
  public Charset getCharset() {
    return StandardCharsets.UTF_8;
  }

  @Value.Default
  public Locale getLocale() {
    return Locale.ENGLISH;
  }

  @Value.Default
  public ZoneId getTimeZone() {
    return ZoneOffset.UTC;
  }

  @Value.Default
  public int getMaxRenderDepth() {
    return 10;
  }

  @Value.Default
  public long getMaxOutputSize() {
    return 0;
  }

  @Value.Default
  public boolean isTrimBlocks() {
    return false;
  }

  @Value.Default
  public boolean isLstripBlocks() {
    return false;
  }

  @Value.Default
  public boolean isEnableRecursiveMacroCalls() {
    return false;
  }

  @Value.Default
  public int getMaxMacroRecursionDepth() {
    return 0;
  }

  @Value.Default
  public Map<Library, Set<String>> getDisabled() {
    return ImmutableMap.of();
  }

  @Value.Default
  public boolean isFailOnUnknownTokens() {
    return false;
  }

  @Value.Default
  public boolean isNestedInterpretationEnabled() {
    return false; // Default changed to false in 3.0
  }

  @Value.Default
  public RandomNumberGeneratorStrategy getRandomNumberGeneratorStrategy() {
    return RandomNumberGeneratorStrategy.THREAD_LOCAL;
  }

  @Value.Default
  public boolean isValidationMode() {
    return false;
  }

  @Value.Default
  public long getMaxStringLength() {
    return getMaxOutputSize();
  }

  @Value.Default
  public int getMaxListSize() {
    return Integer.MAX_VALUE;
  }

  @Value.Default
  public int getMaxMapSize() {
    return Integer.MAX_VALUE;
  }

  @Value.Default
  public int getRangeLimit() {
    return DEFAULT_RANGE_LIMIT;
  }

  @Value.Default
  public int getMaxNumDeferredTokens() {
    return 1000;
  }

  @Value.Default
  public InterpreterFactory getInterpreterFactory() {
    return new JinjavaInterpreterFactory();
  }

  @Value.Default
  public DateTimeProvider getDateTimeProvider() {
    return new CurrentDateTimeProvider();
  }

  @Value.Default
  public TokenScannerSymbols getTokenScannerSymbols() {
    return new DefaultTokenScannerSymbols();
  }

  @Value.Default
  public AllowlistMethodValidator getMethodValidator() {
    return AllowlistMethodValidator.DEFAULT;
  }

  @Value.Default
  public AllowlistReturnTypeValidator getReturnTypeValidator() {
    return AllowlistReturnTypeValidator.DEFAULT;
  }

  @Value.Default
  public ELResolver getElResolver() {
    return isDefaultReadOnlyResolver()
      ? JinjavaInterpreterResolver.DEFAULT_RESOLVER_READ_ONLY
      : JinjavaInterpreterResolver.DEFAULT_RESOLVER_READ_WRITE;
  }

  @Value.Default
  public boolean isDefaultReadOnlyResolver() {
    return true;
  }

  @Value.Default
  public ExecutionMode getExecutionMode() {
    return DefaultExecutionMode.instance();
  }

  @Value.Default
  public LegacyOverrides getLegacyOverrides() {
    return LegacyOverrides.THREE_POINT_0;
  }

  @Value.Default
  public boolean getEnablePreciseDivideFilter() {
    return false;
  }

  @Value.Default
  public boolean isEnableFilterChainOptimization() {
    return false;
  }

  @Value.Default
  public ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
    if (getLegacyOverrides().isUseSnakeCasePropertyNaming()) {
      objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }
    return objectMapper;
  }

  @Value.Default
  public ObjectUnwrapper getObjectUnwrapper() {
    return new JinjavaObjectUnwrapper();
  }

  @Value.Derived
  public Features getFeatures() {
    return new Features(getFeatureConfig());
  }

  @Value.Default
  public FeatureConfig getFeatureConfig() {
    return FeatureConfig.newBuilder().build();
  }

  @Value.Default
  public JinjavaProcessors getProcessors() {
    return JinjavaProcessors.newBuilder().build();
  }

  @Deprecated
  public BiConsumer<Node, JinjavaInterpreter> getNodePreProcessor() {
    return getProcessors().getNodePreProcessor();
  }

  @Deprecated
  public boolean isIterateOverMapKeys() {
    return getLegacyOverrides().isIterateOverMapKeys();
  }

  public static class Builder extends ImmutableJinjavaConfig.Builder {}

  public static Builder builder() {
    return new Builder();
  }

  public static Builder newBuilder() {
    return builder();
  }
}
