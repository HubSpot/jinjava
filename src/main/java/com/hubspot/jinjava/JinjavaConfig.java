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
import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.el.JinjavaInterpreterResolver;
import com.hubspot.jinjava.el.JinjavaObjectUnwrapper;
import com.hubspot.jinjava.el.JinjavaProcessors;
import com.hubspot.jinjava.el.ObjectUnwrapper;
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
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import javax.el.ELResolver;
import org.immutables.value.Value;

@Value.Immutable(singleton = true)
@JinjavaImmutableStyle
public interface JinjavaConfig {
  @Value.Default
  default Charset getCharset() {
    return StandardCharsets.UTF_8;
  }

  @Value.Default
  default Locale getLocale() {
    return Locale.ENGLISH;
  }

  @Value.Default
  default ZoneId getTimeZone() {
    return ZoneOffset.UTC;
  }

  @Value.Default
  default int getMaxRenderDepth() {
    return 10;
  }

  @Value.Default
  default long getMaxOutputSize() {
    return 0;
  }

  @Value.Default
  default boolean isTrimBlocks() {
    return false;
  }

  @Value.Default
  default boolean isLstripBlocks() {
    return false;
  }

  @Value.Default
  default boolean isEnableRecursiveMacroCalls() {
    return false;
  }

  @Value.Default
  default int getMaxMacroRecursionDepth() {
    return 0;
  }

  ImmutableMap<Library, ImmutableSet<String>> getDisabled();

  ImmutableSet<String> getRestrictedMethods();

  ImmutableSet<String> getRestrictedProperties();

  @Value.Default
  default boolean isFailOnUnknownTokens() {
    return false;
  }

  @Value.Default
  default boolean isNestedInterpretationEnabled() {
    return false; // Modified from version 2.X
  }

  @Value.Default
  default RandomNumberGeneratorStrategy getRandomNumberGeneratorStrategy() {
    return RandomNumberGeneratorStrategy.THREAD_LOCAL;
  }

  @Value.Default
  default boolean isValidationMode() {
    return false;
  }

  @Value.Default
  default long getMaxStringLength() {
    return getMaxOutputSize();
  }

  @Value.Default
  default int getMaxListSize() {
    return Integer.MAX_VALUE;
  }

  @Value.Default
  default int getMaxMapSize() {
    return Integer.MAX_VALUE;
  }

  @Value.Default
  default int getRangeLimit() {
    return DEFAULT_RANGE_LIMIT;
  }

  @Value.Default
  default int getMaxNumDeferredTokens() {
    return 1000;
  }

  @Value.Default
  default InterpreterFactory getInterpreterFactory() {
    return new JinjavaInterpreterFactory();
  }

  @Value.Default
  default DateTimeProvider getDateTimeProvider() {
    return new CurrentDateTimeProvider();
  }

  @Value.Default
  default TokenScannerSymbols getTokenScannerSymbols() {
    return new DefaultTokenScannerSymbols();
  }

  @Value.Default
  default ELResolver getElResolver() {
    return JinjavaInterpreterResolver.DEFAULT_RESOLVER_READ_ONLY;
  }

  @Value.Default
  default ExecutionMode getExecutionMode() {
    return DefaultExecutionMode.instance();
  }

  @Value.Default
  default LegacyOverrides getLegacyOverrides() {
    return LegacyOverrides.THREE_POINT_0; // Modified from version 2.X
  }

  @Value.Default
  default boolean getEnablePreciseDivideFilter() {
    return false;
  }

  @Value.Default
  default boolean isEnableFilterChainOptimization() {
    return false;
  }

  @Nullable
  ObjectMapper getObjectMapperOrNull();

  @Value.Derived
  default ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = getObjectMapperOrNull();
    if (objectMapper == null) {
      objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
      if (getLegacyOverrides().isUseSnakeCasePropertyNaming()) {
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
      }
    }
    return objectMapper;
  }

  @Value.Default
  default ObjectUnwrapper getObjectUnwrapper() {
    return new JinjavaObjectUnwrapper();
  }

  @Value.Derived
  default Features getFeatures() {
    return new Features(getFeatureConfig());
  }

  @Value.Default
  default FeatureConfig getFeatureConfig() {
    return FeatureConfig.newBuilder().build();
  }

  @Value.Default
  default JinjavaProcessors getProcessors() {
    return JinjavaProcessors.newBuilder().build();
  }

  @Deprecated
  default BiConsumer<Node, JinjavaInterpreter> getNodePreProcessor() {
    return getProcessors().getNodePreProcessor();
  }

  @Deprecated
  default boolean isIterateOverMapKeys() {
    return getLegacyOverrides().isIterateOverMapKeys();
  }

  class Builder extends ImmutableJinjavaConfig.Builder {

    public Builder withReadOnlyResolver(boolean readOnlyResolver) {
      return withElResolver(
        readOnlyResolver
          ? JinjavaInterpreterResolver.DEFAULT_RESOLVER_READ_ONLY
          : JinjavaInterpreterResolver.DEFAULT_RESOLVER_READ_WRITE
      );
    }
  }

  static Builder builder() {
    return new Builder();
  }

  static Builder newBuilder() {
    return builder();
  }
}
