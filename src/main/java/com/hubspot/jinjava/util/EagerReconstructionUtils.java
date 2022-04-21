package com.hubspot.jinjava.util;

import com.hubspot.jinjava.el.ext.AbstractCallableMethod;
import com.hubspot.jinjava.interpret.Context.Library;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.DisabledException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.fn.eager.EagerMacroFunction;
import com.hubspot.jinjava.lib.tag.AutoEscapeTag;
import com.hubspot.jinjava.lib.tag.DoTag;
import com.hubspot.jinjava.lib.tag.MacroTag;
import com.hubspot.jinjava.lib.tag.RawTag;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.lib.tag.eager.EagerExecutionResult;
import com.hubspot.jinjava.lib.tag.eager.EagerToken;
import com.hubspot.jinjava.objects.Namespace;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.EagerExpressionResolver.EagerExpressionResult;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EagerReconstructionUtils {

  /**
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
  public static EagerExecutionResult executeInChildContext(
    Function<JinjavaInterpreter, EagerExpressionResult> function,
    JinjavaInterpreter interpreter,
    boolean takeNewValue,
    boolean partialMacroEvaluation,
    boolean checkForContextChanges
  ) {
    EagerExpressionResult result;
    Set<String> metaContextVariables = interpreter.getContext().getMetaContextVariables();
    final Map<String, Object> initiallyResolvedHashes;
    final Map<String, String> initiallyResolvedAsStrings;
    if (checkForContextChanges) {
      initiallyResolvedHashes =
        interpreter
          .getContext()
          .entrySet()
          .stream()
          .filter(e -> !metaContextVariables.contains(e.getKey()))
          .filter(
            entry ->
              !(entry.getValue() instanceof DeferredValue) && entry.getValue() != null
          )
          .collect(
            Collectors.toMap(
              Entry::getKey,
              entry -> getObjectOrHashCode(entry.getValue())
            )
          );
      initiallyResolvedAsStrings = new HashMap<>();
      // This creates a stringified snapshot of the context
      // so it can be disabled via the config because it may cause performance issues.
      Stream<Entry<String, Object>> entryStream;
      if (!interpreter.getConfig().getExecutionMode().useEagerContextReverting()) {
        entryStream =
          interpreter
            .getContext()
            .getCombinedScope()
            .entrySet()
            .stream()
            .filter(entry -> initiallyResolvedHashes.containsKey(entry.getKey()))
            .filter(
              entry -> EagerExpressionResolver.isResolvableObject(entry.getValue(), 2, 10) // TODO make this configurable
            );
      } else {
        entryStream =
          interpreter
            .getContext()
            .entrySet()
            .stream()
            .filter(entry -> initiallyResolvedHashes.containsKey(entry.getKey()))
            .filter(
              entry -> EagerExpressionResolver.isResolvableObject(entry.getValue())
            );
      }
      entryStream.forEach(
        entry -> {
          try {
            initiallyResolvedAsStrings.put(
              entry.getKey(),
              PyishObjectMapper.getAsUnquotedPyishString(entry.getValue())
            );
          } catch (Exception ignored) {}
        }
      );
    } else {
      initiallyResolvedHashes = Collections.emptyMap();
      initiallyResolvedAsStrings = Collections.emptyMap();
    }

    // Don't create new call stacks to prevent hitting max recursion with this silent new scope
    Map<String, Object> sessionBindings;
    try (InterpreterScopeClosable c = interpreter.enterNonStackingScope()) {
      if (checkForContextChanges) {
        interpreter.getContext().setDeferredExecutionMode(true);
      }
      interpreter.getContext().setPartialMacroEvaluation(partialMacroEvaluation);
      result = function.apply(interpreter);
      sessionBindings = interpreter.getContext().getSessionBindings();
    }
    sessionBindings =
      sessionBindings
        .entrySet()
        .stream()
        .filter(
          entry ->
            entry.getValue() != null &&
            !entry.getValue().equals(interpreter.getContext().get(entry.getKey()))
        )
        .filter(
          entry ->
            !(interpreter.getContext().get(entry.getKey()) instanceof DeferredValue)
        )
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    if (checkForContextChanges) {
      sessionBindings.putAll(
        interpreter
          .getContext()
          .entrySet()
          .stream()
          .filter(e -> initiallyResolvedHashes.containsKey(e.getKey()))
          .filter(
            e ->
              !initiallyResolvedHashes
                .get(e.getKey())
                .equals(getObjectOrHashCode(e.getValue()))
          )
          .collect(
            Collectors.toMap(
              Entry::getKey,
              e -> {
                if (e.getValue() instanceof DeferredValue) {
                  return ((DeferredValue) e.getValue()).getOriginalValue();
                }
                if (takeNewValue) {
                  return e.getValue();
                }

                // This is necessary if a state-changing function, such as .update()
                // or .append() is run against a variable in the context.
                // It will revert the effects when takeNewValue is false.
                if (initiallyResolvedAsStrings.containsKey(e.getKey())) {
                  // convert to new list or map
                  return interpreter.resolveELExpression(
                    initiallyResolvedAsStrings.get(e.getKey()),
                    interpreter.getLineNumber()
                  );
                }

                // Previous value could not be mapped to a string
                throw new DeferredValueException(e.getKey());
              }
            )
          )
      );
    }
    sessionBindings =
      sessionBindings
        .entrySet()
        .stream()
        .filter(entry -> !metaContextVariables.contains(entry.getKey()))
        .filter(
          entry ->
            !(entry.getValue() instanceof DeferredValue) && entry.getValue() != null
        ) // these are already set recursively
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    return new EagerExecutionResult(result, sessionBindings);
  }

  private static Object getObjectOrHashCode(Object o) {
    if (o instanceof PyList && !((PyList) o).toList().contains(o)) {
      return o.hashCode();
    }
    if (o instanceof PyMap && !((PyMap) o).toMap().containsValue(o)) {
      return o.hashCode() + ((PyMap) o).keySet().hashCode();
    }
    return o;
  }

  /**
   * Reconstruct the macro functions and variables from the context before they
   * get deferred.
   * Those macro functions and variables found within {@code deferredWords} are
   * reconstructed with {@link MacroTag}(s) and a {@link SetTag}, respectively to
   * preserve the context within the Jinjava template itself.
   * @param deferredWords set of words that will need to be deferred based on the
   *                      previously performed operation.
   * @param interpreter the Jinjava interpreter.
   * @return a Jinjava-syntax string of 0 or more macro tags and 0 or 1 set tags.
   */
  public static String reconstructFromContextBeforeDeferring(
    Set<String> deferredWords,
    JinjavaInterpreter interpreter
  ) {
    return (
      reconstructMacroFunctionsBeforeDeferring(deferredWords, interpreter) +
      reconstructVariablesBeforeDeferring(deferredWords, interpreter)
    );
  }

  /**
   * Build macro tag images for any macro functions that are included in deferredWords
   * and remove those macro functions from the deferredWords set.
   * These macro functions are either global or local macro functions, with local
   * meaning they've been imported under an alias such as "simple.multiply()".
   * @param deferredWords Set of words that were encountered and their evaluation has
   *                      to be deferred for a later render.
   * @param interpreter The Jinjava interpreter.
   * @return A jinjava-syntax string that is the images of any macro functions that must
   *  be evaluated at a later time.
   */
  private static String reconstructMacroFunctionsBeforeDeferring(
    Set<String> deferredWords,
    JinjavaInterpreter interpreter
  ) {
    Set<String> toRemove = new HashSet<>();
    Map<String, MacroFunction> macroFunctions = deferredWords
      .stream()
      .filter(w -> !interpreter.getContext().containsKey(w))
      .map(w -> interpreter.getContext().getGlobalMacro(w))
      .filter(Objects::nonNull)
      .collect(Collectors.toMap(AbstractCallableMethod::getName, Function.identity()));
    for (String word : deferredWords) {
      if (word.contains(".")) {
        interpreter
          .getContext()
          .getLocalMacro(word)
          .ifPresent(macroFunction -> macroFunctions.put(word, macroFunction));
      }
    }

    String result = macroFunctions
      .entrySet()
      .stream()
      .peek(entry -> toRemove.add(entry.getKey()))
      .peek(entry -> entry.getValue().setDeferred(true))
      .map(
        entry ->
          executeInChildContext(
            eagerInterpreter ->
              EagerExpressionResult.fromString(
                new EagerMacroFunction(entry.getKey(), entry.getValue(), interpreter)
                .reconstructImage()
              ),
            interpreter,
            false,
            false,
            true
          )
      )
      .map(EagerExecutionResult::asTemplateString)
      .collect(Collectors.joining());
    // Remove macro functions from the set because they've been fully processed now.
    deferredWords.removeAll(toRemove);
    return result;
  }

  private static String reconstructVariablesBeforeDeferring(
    Set<String> deferredWords,
    JinjavaInterpreter interpreter
  ) {
    if (interpreter.getContext().isDeferredExecutionMode()) {
      return ""; // This will be handled outside of the deferred execution mode.
    }
    Set<String> metaContextVariables = interpreter.getContext().getMetaContextVariables();
    Map<String, String> deferredMap = new HashMap<>();
    deferredWords
      .stream()
      .map(w -> w.split("\\.", 2)[0]) // get base prop
      .filter(
        w ->
          interpreter.getContext().containsKey(w) &&
          !(interpreter.getContext().get(w) instanceof DeferredValue)
      )
      .filter(w -> !metaContextVariables.contains(w))
      .forEach(
        w -> {
          Object value = interpreter.getContext().get(w);
          deferredMap.put(
            w,
            String.format(
              value instanceof Namespace ? "namespace(%s)" : "%s",
              PyishObjectMapper.getAsPyishString(value)
            )
          );
        }
      );
    return buildSetTag(deferredMap, interpreter, true);
  }

  /**
   * Build the image for a {@link SetTag} which preserves the values of objects on the context
   * for a later rendering pass. The set tag will set the keys to the values within
   * the {@code deferredValuesToSet} Map.
   * @param deferredValuesToSet Map that specifies what the context objects should be set
   *                            to in the returned image.
   * @param interpreter The Jinjava interpreter.
   * @param registerEagerToken Whether or not to register the returned {@link SetTag}
   *                           image as an {@link EagerToken}.
   * @return A jinjava-syntax string that is the image of a set tag that will
   *  be executed at a later time.
   */
  public static String buildSetTag(
    Map<String, String> deferredValuesToSet,
    JinjavaInterpreter interpreter,
    boolean registerEagerToken
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
    deferredValuesToSet.forEach(
      (key, value) -> {
        // This ensures they are properly aligned to each other.
        vars.add(key);
        values.add(value);
      }
    );
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
    // Don't defer if we're sticking with the new value
    if (registerEagerToken) {
      interpreter
        .getContext()
        .handleEagerToken(
          new EagerToken(
            new TagToken(
              image,
              // TODO this line number won't be accurate, currently doesn't matter.
              interpreter.getLineNumber(),
              interpreter.getPosition(),
              interpreter.getConfig().getTokenScannerSymbols()
            ),
            Collections.emptySet(),
            deferredValuesToSet.keySet()
          )
        );
    }
    return image;
  }

  /**
   * Build the image for a block {@link SetTag} and body to preserve the values of an object
   * on the context for a later rendering pass.
   * @param name The name of the variable to set.
   * @param value The string value, potentially containing jinja code to put in the set tag block.
   * @param interpreter The Jinjava interpreter.
   * @param registerEagerToken Whether or not to register the returned {@link SetTag}
   *                           token as an {@link EagerToken}.
   * @return A jinjava-syntax string that is the image of a block set tag that will
   *  be executed at a later time.
   */
  public static String buildBlockSetTag(
    String name,
    String value,
    JinjavaInterpreter interpreter,
    boolean registerEagerToken
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
    if (registerEagerToken) {
      interpreter
        .getContext()
        .handleEagerToken(
          new EagerToken(
            new TagToken(
              blockSetTokenBuilder.toString(),
              interpreter.getLineNumber(),
              interpreter.getPosition(),
              interpreter.getConfig().getTokenScannerSymbols()
            ),
            Collections.emptySet(),
            Collections.singleton(name)
          )
        );
    }
    return image;
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
        output = wrapInTag(output, RawTag.TAG_NAME, interpreter);
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
      output = wrapInTag(output, AutoEscapeTag.TAG_NAME, interpreter);
    }
    return output;
  }

  public static String wrapInTag(
    String s,
    String tagNameToWrap,
    JinjavaInterpreter interpreter
  ) {
    return (
      String.format(
        "%s %s %s",
        interpreter.getConfig().getTokenScannerSymbols().getExpressionStartWithTag(),
        tagNameToWrap,
        interpreter.getConfig().getTokenScannerSymbols().getExpressionEndWithTag()
      ) +
      s +
      String.format(
        "%s end%s %s",
        interpreter.getConfig().getTokenScannerSymbols().getExpressionStartWithTag(),
        tagNameToWrap,
        interpreter.getConfig().getTokenScannerSymbols().getExpressionEndWithTag()
      )
    );
  }
}
