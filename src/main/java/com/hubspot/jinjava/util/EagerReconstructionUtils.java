package com.hubspot.jinjava.util;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.el.ext.AbstractCallableMethod;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.Context.Library;
import com.hubspot.jinjava.interpret.DeferredLazyReferenceSource;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueShadow;
import com.hubspot.jinjava.interpret.DisabledException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.OneTimeReconstructible;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.fn.eager.EagerMacroFunction;
import com.hubspot.jinjava.lib.tag.AutoEscapeTag;
import com.hubspot.jinjava.lib.tag.DoTag;
import com.hubspot.jinjava.lib.tag.MacroTag;
import com.hubspot.jinjava.lib.tag.RawTag;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.lib.tag.eager.DeferredToken;
import com.hubspot.jinjava.lib.tag.eager.EagerExecutionResult;
import com.hubspot.jinjava.lib.tag.eager.EagerSetTagStrategy;
import com.hubspot.jinjava.lib.tag.eager.importing.AliasedEagerImportingStrategy;
import com.hubspot.jinjava.lib.tag.eager.importing.EagerImportingStrategyFactory;
import com.hubspot.jinjava.loader.RelativePathResolver;
import com.hubspot.jinjava.objects.serialization.PyishBlockSetSerializable;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.output.DynamicRenderedOutputNode;
import com.hubspot.jinjava.tree.output.OutputList;
import com.hubspot.jinjava.tree.output.RenderedOutputNode;
import com.hubspot.jinjava.tree.parse.NoteToken;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.tree.parse.TokenScannerSymbols;
import com.hubspot.jinjava.util.EagerContextWatcher.EagerChildContextConfig;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Beta
public class EagerReconstructionUtils {

  /**
   * @deprecated Use {@link EagerContextWatcher#executeInChildContext(Function, JinjavaInterpreter, EagerChildContextConfig)}
   * Execute the specified functions within a protected context.
   * Additionally, if the execution causes existing values on the context to become
   *   deferred, then their previous values will wrapped in a <code>set</code>
   *   tag that gets prepended to the returned result.
   * The <code>function</code> is run in deferredExecutionMode=true, where the context needs to
   *   be protected from having values updated or set,
   *   such as when evaluating both the positive and negative nodes in an if statement.
   * @param function Function to run within a "protected" child context
   * @param interpreter JinjavaInterpreter to create a child from.
   * @param takeNewValue If a value is updated (not replaced) either take the new value or
   *                     take the previous value and put it into the
   *                     <code>EagerExecutionResult.prefixToPreserveState</code>.
   * @param partialMacroEvaluation Allow macro functions to be partially evaluated rather than
   *                               needing an explicit result during this render.
   * @param checkForContextChanges Set this to be true if executing <code>function</code> could
   *                               cause changes to the context. Otherwise, a false value will
   *                               speed up execution.
   * @return An <code>EagerExecutionResult</code> where:
   *  <code>result</code> is the string result of <code>function</code>.
   *  <code>prefixToPreserveState</code> is either blank or a <code>set</code> tag
   *    that preserves the state within the output for a second rendering pass.
   */
  @Deprecated
  public static EagerExecutionResult executeInChildContext(
    Function<JinjavaInterpreter, EagerExpressionResult> function,
    JinjavaInterpreter interpreter,
    boolean takeNewValue,
    boolean partialMacroEvaluation,
    boolean checkForContextChanges
  ) {
    return executeInChildContext(
      function,
      interpreter,
      EagerChildContextConfig
        .newBuilder()
        .withTakeNewValue(takeNewValue)
        .withForceDeferredExecutionMode(checkForContextChanges)
        .withPartialMacroEvaluation(partialMacroEvaluation)
        .build()
    );
  }

  public static EagerExecutionResult executeInChildContext(
    Function<JinjavaInterpreter, EagerExpressionResult> function,
    JinjavaInterpreter interpreter,
    EagerChildContextConfig eagerChildContextConfig
  ) {
    return EagerContextWatcher.executeInChildContext(
      function,
      interpreter,
      eagerChildContextConfig
    );
  }

  /**
   * Reconstruct the macro functions and variables from the context before they
   * get deferred.
   * Those macro functions and variables found within {@code deferredWords} are
   * reconstructed with {@link MacroTag}(s) and {@link SetTag}(s), respectively to
   * preserve the context within the Jinjava template itself.
   * @param deferredWords set of words that will need to be deferred based on the
   *                      previously performed operation.
   * @param interpreter the Jinjava interpreter.
   * @return a Jinjava-syntax string of 0 or more macro tags and 0 or more set tags.
   * @deprecated use {@link #hydrateReconstructionFromContextBeforeDeferring(PrefixToPreserveState, Set, JinjavaInterpreter)}
   */
  @Deprecated
  public static String reconstructFromContextBeforeDeferring(
    Set<String> deferredWords,
    JinjavaInterpreter interpreter
  ) {
    return String.join(
      "",
      reconstructFromContextBeforeDeferringAsMap(deferredWords, interpreter).values()
    );
  }

  /**
   * Reconstruct the macro functions and variables from the context before they
   * get deferred.
   * Those macro functions and variables found within {@code deferredWords} are
   * reconstructed with {@link MacroTag}(s) and {@link SetTag}(s), respectively to
   * preserve the context within the Jinjava template itself.
   * @param deferredWords set of words that will need to be deferred based on the
   *                      previously performed operation.
   * @param interpreter the Jinjava interpreter.
   * @return a PrefixToPreserveState map of 0 or more macro tags and 0 or more set tags.
   * @deprecated use {@link #hydrateReconstructionFromContextBeforeDeferring(PrefixToPreserveState, Set, JinjavaInterpreter)}
   */
  @Deprecated
  public static PrefixToPreserveState reconstructFromContextBeforeDeferringAsMap(
    Set<String> deferredWords,
    JinjavaInterpreter interpreter
  ) {
    PrefixToPreserveState prefixToPreserveState = new PrefixToPreserveState();
    hydrateReconstructionFromContextBeforeDeferring(
      prefixToPreserveState,
      deferredWords,
      interpreter,
      0
    );
    return prefixToPreserveState;
  }

  /**
   * Reconstruct the macro functions and variables from the context before they
   * get deferred.
   * Those macro functions and variables found within {@code deferredWords} are
   * reconstructed with {@link MacroTag}(s) and {@link SetTag}(s), respectively to
   * preserve the context within the Jinjava template itself.
   * @param prefixToPreserveState This PrefixToPreserveState will be hydrated with the Macro tag images and set tag images
   * @param deferredWords set of words that will need to be deferred based on the
   *                      previously performed operation.
   * @param interpreter the Jinjava interpreter.
   * @return The PrefixToPreserveState to allow method chaining
   */
  public static PrefixToPreserveState hydrateReconstructionFromContextBeforeDeferring(
    PrefixToPreserveState prefixToPreserveState,
    Set<String> deferredWords,
    JinjavaInterpreter interpreter
  ) {
    return hydrateReconstructionFromContextBeforeDeferring(
      prefixToPreserveState,
      deferredWords,
      interpreter,
      0
    );
  }

  private static PrefixToPreserveState hydrateReconstructionFromContextBeforeDeferring(
    PrefixToPreserveState prefixToPreserveState,
    Set<String> deferredWords,
    JinjavaInterpreter interpreter,
    int depth
  ) {
    if (depth <= interpreter.getConfig().getMaxRenderDepth()) {
      hydrateReconstructionOfMacroFunctionsBeforeDeferring(
        prefixToPreserveState,
        deferredWords,
        interpreter
      );
      Set<String> deferredWordBases = filterToRelevantBases(deferredWords, interpreter);
      if (deferredWordBases.isEmpty()) {
        return prefixToPreserveState;
      }

      return hydrateReconstructionOfVariablesBeforeDeferring(
        prefixToPreserveState,
        deferredWordBases,
        interpreter,
        depth
      );
    }
    return prefixToPreserveState;
  }

  private static Set<String> filterToRelevantBases(
    Set<String> deferredWords,
    JinjavaInterpreter interpreter
  ) {
    Map<String, Object> combinedScope = interpreter.getContext().getCombinedScope();
    Set<String> deferredWordBases = deferredWords
      .stream()
      .map(w -> w.split("\\.", 2)[0])
      .filter(combinedScope::containsKey)
      .collect(Collectors.toSet());
    if (interpreter.getContext().isDeferredExecutionMode()) {
      Context parent = interpreter.getContext().getParent();
      while (parent.isDeferredExecutionMode()) {
        parent = parent.getParent();
      }
      final Context finalParent = parent;
      deferredWordBases =
        deferredWordBases
          .stream()
          .filter(word -> {
            Object parentValue = finalParent.get(word);
            return (
              !(parentValue instanceof DeferredValue) &&
              interpreter.getContext().get(word) != finalParent.get(word)
            );
          })
          .collect(Collectors.toSet());
    }
    return deferredWordBases;
  }

  /**
   * Build macro tag images for any macro functions that are included in deferredWords
   * and remove those macro functions from the deferredWords set.
   * These macro functions are either global or local macro functions, with local
   * meaning they've been imported under an alias such as "simple.multiply()".
   * @param prefixToPreserveState This PrefixToPreserveState will be hydrated with the Macro tag images
   * @param deferredWords Set of words that were encountered and their evaluation has
   *                      to be deferred for a later render.
   * @param interpreter The Jinjava interpreter.
   * @return The PrefixToPreserveState to allow method chaining
   */
  private static PrefixToPreserveState hydrateReconstructionOfMacroFunctionsBeforeDeferring(
    PrefixToPreserveState prefixToPreserveState,
    Set<String> deferredWords,
    JinjavaInterpreter interpreter
  ) {
    Set<String> toRemove = new HashSet<>();
    Map<String, MacroFunction> macroFunctions = deferredWords
      .stream()
      .filter(w -> !prefixToPreserveState.containsKey(w))
      .filter(w -> !interpreter.getContext().containsKey(w))
      .map(w -> interpreter.getContext().getGlobalMacro(w))
      .filter(Objects::nonNull)
      .filter(macroFunction -> !macroFunction.isCaller())
      .collect(Collectors.toMap(AbstractCallableMethod::getName, Function.identity()));
    for (String word : deferredWords) {
      if (word.contains(".")) {
        interpreter
          .getContext()
          .getLocalMacro(word)
          .ifPresent(macroFunction -> macroFunctions.put(word, macroFunction));
      }
    }

    Map<String, String> reconstructedMacros = macroFunctions
      .entrySet()
      .stream()
      .peek(entry -> toRemove.add(entry.getKey()))
      .peek(entry -> entry.getValue().setDeferred(true))
      .map(entry ->
        new AbstractMap.SimpleImmutableEntry<>(
          entry.getKey(),
          EagerContextWatcher.executeInChildContext(
            eagerInterpreter ->
              EagerExpressionResult.fromString(
                ((EagerMacroFunction) entry.getValue()).reconstructImage(entry.getKey())
              ),
            interpreter,
            EagerContextWatcher.EagerChildContextConfig
              .newBuilder()
              .withForceDeferredExecutionMode(true)
              .build()
          )
        )
      )
      .collect(
        Collectors.toMap(Entry::getKey, entry -> entry.getValue().asTemplateString())
      );
    prefixToPreserveState.withAll(reconstructedMacros);
    // Remove macro functions from the set because they've been fully processed now.
    deferredWords.removeAll(toRemove);
    return prefixToPreserveState;
  }

  private static PrefixToPreserveState hydrateReconstructionOfVariablesBeforeDeferring(
    PrefixToPreserveState prefixToPreserveState,
    Set<String> deferredWords,
    JinjavaInterpreter interpreter,
    int depth
  ) {
    Set<String> metaContextVariables = interpreter
      .getContext()
      .getComputedMetaContextVariables();
    deferredWords
      .stream()
      .filter(w -> !metaContextVariables.contains(w))
      .filter(w -> !prefixToPreserveState.containsKey(w))
      .map(word ->
        new AbstractMap.SimpleImmutableEntry<>(word, interpreter.getContext().get(word))
      )
      .filter(entry ->
        entry.getValue() != null && !(entry.getValue() instanceof DeferredValue)
      )
      .forEach(entry ->
        hydrateBlockOrInlineSetTagRecursively(
          prefixToPreserveState,
          entry.getKey(),
          entry.getValue(),
          interpreter,
          depth
        )
      );
    return prefixToPreserveState;
  }

  public static String buildBlockOrInlineSetTag(
    String name,
    Object value,
    JinjavaInterpreter interpreter
  ) {
    return buildBlockOrInlineSetTag(name, value, interpreter, false);
  }

  public static String buildBlockOrInlineSetTagAndRegisterDeferredToken(
    String name,
    Object value,
    JinjavaInterpreter interpreter
  ) {
    return buildBlockOrInlineSetTag(name, value, interpreter, true);
  }

  public static PrefixToPreserveState hydrateBlockOrInlineSetTagRecursively(
    PrefixToPreserveState prefixToPreserveState,
    String name,
    Object value,
    JinjavaInterpreter interpreter
  ) {
    return hydrateBlockOrInlineSetTagRecursively(
      prefixToPreserveState,
      name,
      value,
      interpreter,
      0
    );
  }

  private static PrefixToPreserveState hydrateBlockOrInlineSetTagRecursively(
    PrefixToPreserveState prefixToPreserveState,
    String name,
    Object value,
    JinjavaInterpreter interpreter,
    int depth
  ) {
    if (
      value instanceof DeferredValue &&
      !(value instanceof PyishBlockSetSerializable || value instanceof PyishSerializable)
    ) {
      value = ((DeferredValue) value).getOriginalValue();
    }
    if (value instanceof PyishBlockSetSerializable) {
      prefixToPreserveState.put(
        name,
        buildBlockSetTag(
          name,
          ((PyishBlockSetSerializable) value).getBlockSetBody(),
          interpreter,
          false
        )
      );
      return prefixToPreserveState;
    }
    String pyishStringRepresentation = PyishObjectMapper.getAsPyishString(value);

    if (
      depth < interpreter.getConfig().getMaxRenderDepth() &&
      interpreter.getConfig().isNestedInterpretationEnabled()
    ) {
      Set<String> dependentWords = EagerExpressionResolver.findDeferredWords(
        pyishStringRepresentation,
        interpreter
      );
      if (!dependentWords.isEmpty()) {
        hydrateReconstructionFromContextBeforeDeferring(
          prefixToPreserveState,
          dependentWords,
          interpreter,
          depth + 1
        );
      }
    }
    prefixToPreserveState.put(
      name,
      buildSetTag(ImmutableMap.of(name, pyishStringRepresentation), interpreter, false)
    );
    return prefixToPreserveState;
  }

  public static String buildBlockOrInlineSetTag(
    String name,
    Object value,
    JinjavaInterpreter interpreter,
    boolean registerDeferredToken
  ) {
    if (
      value instanceof DeferredValue &&
      !(value instanceof PyishBlockSetSerializable || value instanceof PyishSerializable)
    ) {
      value = ((DeferredValue) value).getOriginalValue();
    }
    if (value instanceof PyishBlockSetSerializable) {
      return buildBlockSetTag(
        name,
        ((PyishBlockSetSerializable) value).getBlockSetBody(),
        interpreter,
        registerDeferredToken
      );
    }
    return buildSetTag(
      ImmutableMap.of(name, PyishObjectMapper.getAsPyishString(value)),
      interpreter,
      registerDeferredToken
    );
  }

  /**
   * Build the image for a {@link SetTag} which preserves the values of objects on the context
   * for a later rendering pass. The set tag will set the keys to the values within
   * the {@code deferredValuesToSet} Map.
   * @param deferredValuesToSet Map that specifies what the context objects should be set
   *                            to in the returned image.
   * @param interpreter The Jinjava interpreter.
   * @param registerDeferredToken Whether or not to register the returned {@link SetTag}
   *                           image as an {@link DeferredToken}.
   * @return A jinjava-syntax string that is the image of a set tag that will
   *  be executed at a later time.
   */
  public static String buildSetTag(
    Map<String, String> deferredValuesToSet,
    JinjavaInterpreter interpreter,
    boolean registerDeferredToken
  ) {
    if (deferredValuesToSet.isEmpty()) {
      return "";
    }
    Map<Library, Set<String>> disabled = interpreter.getConfig().getDisabled();
    if (
      disabled != null &&
      disabled.containsKey(Library.TAG) &&
      disabled.get(Library.TAG).contains(SetTag.TAG_NAME)
    ) {
      throw new DisabledException("set tag disabled");
    }

    StringJoiner vars = new StringJoiner(",");
    StringJoiner values = new StringJoiner(",");
    StringJoiner varsRequiringSuffix = new StringJoiner(",");
    deferredValuesToSet.forEach((key, value) -> {
      // This ensures they are properly aligned to each other.
      vars.add(key);
      values.add(value);
      if (!AliasedEagerImportingStrategy.isTemporaryImportAlias(value)) {
        varsRequiringSuffix.add(key);
      }
    });
    LengthLimitingStringJoiner result = new LengthLimitingStringJoiner(
      interpreter.getConfig().getMaxOutputSize(),
      " "
    );
    result
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionStartWithTag())
      .add(SetTag.TAG_NAME)
      .add(vars.toString())
      .add("=")
      .add(values.toString())
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionEndWithTag());
    String image = result.toString();
    String suffix = EagerSetTagStrategy.getSuffixToPreserveState(
      varsRequiringSuffix.toString(),
      interpreter
    );
    // Don't defer if we're sticking with the new value
    if (registerDeferredToken) {
      return (
        new PrefixToPreserveState(
          EagerReconstructionUtils.handleDeferredTokenAndReconstructReferences(
            interpreter,
            DeferredToken
              .builderFromImage(image, TagToken.class, interpreter)
              .addSetDeferredWords(deferredValuesToSet.keySet())
              .build()
          )
        ) +
        image +
        suffix
      );
    }
    return (image + suffix);
  }

  /**
   * Build the image for a block {@link SetTag} and body to preserve the values of an object
   * on the context for a later rendering pass.
   * @param name The name of the variable to set.
   * @param value The string value, potentially containing jinja code to put in the set tag block.
   * @param interpreter The Jinjava interpreter.
   * @param registerDeferredToken Whether to register the returned {@link SetTag}
   *                           token as an {@link DeferredToken}.
   * @return A jinjava-syntax string that is the image of a block set tag that will
   *  be executed at a later time.
   */
  public static String buildBlockSetTag(
    String name,
    String value,
    JinjavaInterpreter interpreter,
    boolean registerDeferredToken
  ) {
    Map<Library, Set<String>> disabled = interpreter.getConfig().getDisabled();
    if (
      disabled != null &&
      disabled.containsKey(Library.TAG) &&
      disabled.get(Library.TAG).contains(SetTag.TAG_NAME)
    ) {
      throw new DisabledException("set tag disabled");
    }

    LengthLimitingStringJoiner blockSetTokenBuilder = new LengthLimitingStringJoiner(
      interpreter.getConfig().getMaxOutputSize(),
      " "
    );
    StringJoiner endTokenBuilder = new StringJoiner(" ");
    blockSetTokenBuilder
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionStartWithTag())
      .add(SetTag.TAG_NAME)
      .add(name)
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionEndWithTag());
    endTokenBuilder
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionStartWithTag())
      .add("end" + SetTag.TAG_NAME)
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionEndWithTag());
    String image = blockSetTokenBuilder + value + endTokenBuilder;
    String suffix = EagerSetTagStrategy.getSuffixToPreserveState(name, interpreter);
    if (registerDeferredToken) {
      return (
        new PrefixToPreserveState(
          EagerReconstructionUtils.handleDeferredTokenAndReconstructReferences(
            interpreter,
            DeferredToken
              .builderFromImage(
                blockSetTokenBuilder.toString(),
                TagToken.class,
                interpreter
              )
              .addSetDeferredWords(Stream.of(name))
              .build()
          )
        ) +
        image +
        suffix
      );
    }
    return image + suffix;
  }

  public static String buildDoUpdateTag(
    String name,
    String updateString,
    JinjavaInterpreter interpreter
  ) {
    Map<Library, Set<String>> disabled = interpreter.getConfig().getDisabled();
    if (
      disabled != null &&
      disabled.containsKey(Library.TAG) &&
      disabled.get(Library.TAG).contains(DoTag.TAG_NAME)
    ) {
      throw new DisabledException("do tag disabled");
    }
    return new LengthLimitingStringJoiner(interpreter.getConfig().getMaxOutputSize(), " ")
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionStartWithTag())
      .add(DoTag.TAG_NAME)
      .add(String.format("%s.update(%s)", name, updateString))
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionEndWithTag())
      .toString();
  }

  public static String reconstructEnd(TagNode tagNode) {
    return String.format(
      "%s %s %s",
      tagNode.getSymbols().getExpressionStartWithTag(),
      tagNode.getEndName(),
      tagNode.getSymbols().getExpressionEndWithTag()
    );
  }

  public static String wrapInRawIfNeeded(String output, JinjavaInterpreter interpreter) {
    if (
      interpreter.getConfig().getExecutionMode().isPreserveRawTags() &&
      !interpreter.getContext().isUnwrapRawOverride()
    ) {
      if (
        output.contains(
          interpreter.getConfig().getTokenScannerSymbols().getExpressionStart()
        ) ||
        output.contains(
          interpreter.getConfig().getTokenScannerSymbols().getExpressionStartWithTag()
        )
      ) {
        output = wrapInTag(output, RawTag.TAG_NAME, interpreter, false);
      }
    }
    return output;
  }

  public static String wrapInAutoEscapeIfNeeded(
    String output,
    JinjavaInterpreter interpreter
  ) {
    if (
      interpreter.getContext().isAutoEscape() &&
      (
        interpreter.getContext().getParent() == null ||
        !interpreter.getContext().getParent().isAutoEscape()
      )
    ) {
      output = wrapInTag(output, AutoEscapeTag.TAG_NAME, interpreter, false);
    }
    return output;
  }

  /**
   * Wrap the string output in a specified block-type tag.
   * @param body The string body to wrap.
   * @param tagNameToWrap The name of the tag which will wrap around the {@param body}.
   * @param interpreter The Jinjava interpreter.
   * @param registerDeferredToken Whether to register the returned Tag
   *                           token as an {@link DeferredToken}.
   * @return A jinjava-syntax string that is the image of a block set tag that will
   *  be executed at a later time.
   */
  public static String wrapInTag(
    String body,
    String tagNameToWrap,
    JinjavaInterpreter interpreter,
    boolean registerDeferredToken
  ) {
    Map<Library, Set<String>> disabled = interpreter.getConfig().getDisabled();
    if (
      disabled != null &&
      disabled.containsKey(Library.TAG) &&
      disabled.get(Library.TAG).contains(tagNameToWrap)
    ) {
      throw new DisabledException(String.format("%s tag disabled", tagNameToWrap));
    }
    StringJoiner startTokenBuilder = new StringJoiner(" ");
    StringJoiner endTokenBuilder = new StringJoiner(" ");
    startTokenBuilder
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionStartWithTag())
      .add(tagNameToWrap)
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionEndWithTag());
    endTokenBuilder
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionStartWithTag())
      .add("end" + tagNameToWrap)
      .add(interpreter.getConfig().getTokenScannerSymbols().getExpressionEndWithTag());
    String image = startTokenBuilder + body + endTokenBuilder;
    if (registerDeferredToken) {
      EagerReconstructionUtils.handleDeferredTokenAndReconstructReferences(
        interpreter,
        DeferredToken
          .builderFromImage(startTokenBuilder.toString(), TagToken.class, interpreter)
          .build()
      );
    }
    return image;
  }

  /**
   * Surround the {@param body} with notes to provide identifying information on what {@param body} is.
   * If {@param noteIdentifier} is {@code foo} and {@param body} is {@code {{ bar }}}, the result will be:
   * <p>
   * {@code {# foo #}{{ bar }}{# endfoo #}}
   * @param body The string body to wrap.
   * @param noteIdentifier The identifier for the note.
   * @param interpreter The Jinjava interpreter.
   * @return A block surrounded with labelled notes
   */
  public static String labelWithNotes(
    String body,
    String noteIdentifier,
    JinjavaInterpreter interpreter
  ) {
    return (
      getStartLabel(noteIdentifier, interpreter.getConfig().getTokenScannerSymbols()) +
      body +
      getEndLabel(noteIdentifier, interpreter.getConfig().getTokenScannerSymbols())
    );
  }

  public static String getStartLabel(String noteIdentifier, TokenScannerSymbols symbols) {
    StringJoiner stringJoiner = new StringJoiner(" ");
    return stringJoiner
      .add(symbols.getOpeningComment())
      .add("Start Label: ")
      .add(noteIdentifier)
      .add(symbols.getClosingComment())
      .toString();
  }

  public static String getEndLabel(String noteIdentifier, TokenScannerSymbols symbols) {
    StringJoiner stringJoiner = new StringJoiner(" ");
    return stringJoiner
      .add(symbols.getOpeningComment())
      .add("End Label: ")
      .add(noteIdentifier)
      .add(symbols.getClosingComment())
      .toString();
  }

  public static String wrapInChildScope(String toWrap, JinjavaInterpreter interpreter) {
    return (
      String.format(
        "%s for __ignored__ in [0] %s",
        interpreter.getConfig().getTokenScannerSymbols().getExpressionStartWithTag(),
        interpreter.getConfig().getTokenScannerSymbols().getExpressionEndWithTag()
      ) +
      toWrap +
      String.format(
        "%s endfor %s",
        interpreter.getConfig().getTokenScannerSymbols().getExpressionStartWithTag(),
        interpreter.getConfig().getTokenScannerSymbols().getExpressionEndWithTag()
      )
    );
  }

  public static Boolean isDeferredExecutionMode() {
    return JinjavaInterpreter
      .getCurrentMaybe()
      .map(interpreter -> interpreter.getContext().isDeferredExecutionMode())
      .orElse(false);
  }

  public static PrefixToPreserveState deferWordsAndReconstructReferences(
    JinjavaInterpreter interpreter,
    Set<String> wordsToDefer
  ) {
    if (!wordsToDefer.isEmpty()) {
      wordsToDefer =
        wordsToDefer
          .stream()
          .filter(key -> !(interpreter.getContext().get(key) instanceof DeferredValue))
          .collect(Collectors.toSet());
      PrefixToPreserveState prefixToPreserveState = new PrefixToPreserveState();
      if (!wordsToDefer.isEmpty()) {
        prefixToPreserveState.withAllInFront(
          handleDeferredTokenAndReconstructReferences(
            interpreter,
            DeferredToken
              .builderFromImage("", NoteToken.class, interpreter)
              .addUsedDeferredWords(wordsToDefer)
              .build()
          )
        );
      }
      return prefixToPreserveState;
    }
    return new PrefixToPreserveState();
  }

  public static Map<String, String> handleDeferredTokenAndReconstructReferences(
    JinjavaInterpreter interpreter,
    DeferredToken deferredToken
  ) {
    deferredToken.addTo(interpreter.getContext());
    return reconstructDeferredReferences(
      interpreter,
      deferredToken.getUsedDeferredWords()
    );
  }

  public static Map<String, String> reconstructDeferredReferences(
    JinjavaInterpreter interpreter,
    Set<String> usedDeferredWords
  ) {
    return interpreter
      .getContext()
      .getScope()
      .entrySet()
      .stream()
      .filter(entry ->
        entry.getValue() instanceof OneTimeReconstructible &&
        !((OneTimeReconstructible) entry.getValue()).isReconstructed()
      )
      .filter(entry ->
        // Always reconstruct the DeferredLazyReferenceSource, but only reconstruct DeferredLazyReferences when they are used
        entry.getValue() instanceof DeferredLazyReferenceSource ||
        usedDeferredWords.contains(entry.getKey())
      )
      .peek(entry -> ((OneTimeReconstructible) entry.getValue()).setReconstructed(true))
      .map(entry ->
        new AbstractMap.SimpleImmutableEntry<>(
          entry.getKey(),
          PyishObjectMapper.getAsPyishString(
            ((DeferredValue) entry.getValue()).getOriginalValue()
          )
        )
      )
      .sorted((a, b) ->
        a.getValue().equals(b.getKey()) ? 1 : b.getValue().equals(a.getKey()) ? -1 : 0
      )
      .collect(
        Collectors.toMap(
          Entry::getKey,
          entry ->
            buildSetTag(
              Collections.singletonMap(entry.getKey(), entry.getValue()),
              interpreter,
              false
            ),
          (a, b) -> b,
          LinkedHashMap::new
        )
      );
  }

  /**
   * Reset variables to what they were before running the latest execution represented by {@param eagerExecutionResult}.
   * Then re-defer those variables and reconstruct deferred lazy references to them.
   * This method is needed in 2 circumstances:
   * <p>
   *   * When doing some eager execution and then needing to repeat the same execution in deferred execution mode.
   *   <p>
   *   * When rendering logic which takes place in its own child scope (for tag, macro function, set block) and there
   *   are speculative bindings.
   *   These must be deferred and the execution must run again, so they don't get reconstructed
   *   within the child scope, and can instead be reconstructed in their original scopes.
   * @param interpreter The JinjavaInterpreter
   * @param eagerExecutionResult The execution result which contains information about which bindings were modified
   *                             during the execution.
   * @return
   */
  public static PrefixToPreserveState resetAndDeferSpeculativeBindings(
    JinjavaInterpreter interpreter,
    EagerExecutionResult eagerExecutionResult
  ) {
    return deferWordsAndReconstructReferences(
      interpreter,
      resetSpeculativeBindings(interpreter, eagerExecutionResult)
    );
  }

  public static Set<String> resetSpeculativeBindings(
    JinjavaInterpreter interpreter,
    EagerExecutionResult result
  ) {
    result
      .getSpeculativeBindings()
      .forEach((k, v) -> {
        if (v instanceof DeferredValue) {
          v = ((DeferredValue) v).getOriginalValue();
        }
        replace(interpreter.getContext(), k, v);
      });
    return result.getSpeculativeBindings().keySet();
  }

  private static void replace(Context context, String k, Object v) {
    if (context == null) {
      return;
    }
    Object replaced = context.getScope().replace(k, v);
    if (replaced == null) {
      replace(context.getParent(), k, v);
    } else if (replaced instanceof DeferredValueShadow) {
      context.getScope().remove(k);
      replace(context.getParent(), k, v);
    }
  }

  public static void commitSpeculativeBindings(
    JinjavaInterpreter interpreter,
    EagerExecutionResult result
  ) {
    result
      .getSpeculativeBindings()
      .entrySet()
      .stream()
      // Filter DeferredValueShadow because these are just used to mark that a value became deferred within this scope
      // The original key will be a DeferredValueImpl already on its original scope
      .filter(entry -> !(entry.getValue() instanceof DeferredValueShadow))
      .forEach(entry -> interpreter.getContext().put(entry.getKey(), entry.getValue()));
  }

  public static void reconstructPathAroundBlock(
    DynamicRenderedOutputNode prefix,
    OutputList blockValueBuilder,
    JinjavaInterpreter interpreter
  ) {
    String blockPathSetter = EagerImportingStrategyFactory.getSetTagForCurrentPath(
      interpreter
    );
    String tempVarName = "temp_current_path_" + Math.abs(blockPathSetter.hashCode() >> 1);
    prefix.setValue(
      buildSetTag(
        ImmutableMap.of(tempVarName, RelativePathResolver.CURRENT_PATH_CONTEXT_KEY),
        interpreter,
        false
      ) +
      EagerImportingStrategyFactory.getSetTagForCurrentPath(interpreter)
    );
    blockValueBuilder.addNode(
      new RenderedOutputNode(
        buildSetTag(
          ImmutableMap.of(RelativePathResolver.CURRENT_PATH_CONTEXT_KEY, tempVarName),
          interpreter,
          false
        )
      )
    );
  }
}
