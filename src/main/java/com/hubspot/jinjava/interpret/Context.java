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

package com.hubspot.jinjava.interpret;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.hubspot.jinjava.lib.Importable;
import com.hubspot.jinjava.lib.expression.DefaultExpressionStrategy;
import com.hubspot.jinjava.lib.expression.ExpressionStrategy;
import com.hubspot.jinjava.lib.exptest.ExpTest;
import com.hubspot.jinjava.lib.exptest.ExpTestLibrary;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.lib.filter.FilterLibrary;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.hubspot.jinjava.lib.fn.FunctionLibrary;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.lib.tag.TagLibrary;
import com.hubspot.jinjava.lib.tag.eager.EagerToken;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.util.DeferredValueUtils;
import com.hubspot.jinjava.util.ScopeMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Context extends ScopeMap<String, Object> {
  public static final String GLOBAL_MACROS_SCOPE_KEY = "__macros__";
  public static final String IMPORT_RESOURCE_PATH_KEY = "import_resource_path";
  public static final String DEFERRED_IMPORT_RESOURCE_PATH_KEY =
    "deferred_import_resource_path";

  public static final String IMPORT_RESOURCE_ALIAS_KEY = "import_resource_alias";

  private SetMultimap<String, String> dependencies = HashMultimap.create();
  private Map<Library, Set<String>> disabled;

  public boolean isValidationMode() {
    return validationMode;
  }

  public Context setValidationMode(boolean validationMode) {
    this.validationMode = validationMode;
    return this;
  }

  public enum Library {
    EXP_TEST,
    FILTER,
    FUNCTION,
    TAG
  }

  private final CallStack extendPathStack;
  private final CallStack importPathStack;
  private final CallStack includePathStack;
  private final CallStack macroStack;
  private final CallStack fromStack;
  private final CallStack currentPathStack;

  private final Set<String> resolvedExpressions = new HashSet<>();
  private final Set<String> resolvedValues = new HashSet<>();
  private final Set<String> resolvedFunctions = new HashSet<>();

  private Set<Node> deferredNodes = new HashSet<>();
  private Set<EagerToken> eagerTokens = new HashSet<>();

  private final ExpTestLibrary expTestLibrary;
  private final FilterLibrary filterLibrary;
  private final FunctionLibrary functionLibrary;
  private final TagLibrary tagLibrary;

  private ExpressionStrategy expressionStrategy = new DefaultExpressionStrategy();

  private final Context parent;

  private int renderDepth = -1;
  private Boolean autoEscape;
  private List<? extends Node> superBlock;

  private final Stack<String> renderStack = new Stack<>();

  private boolean validationMode = false;
  private boolean deferredExecutionMode = false;
  private boolean throwInterpreterErrors = false;
  private boolean partialMacroEvaluation = false;
  private boolean unwrapRawOverride = false;

  public Context() {
    this(null, null, null, true);
  }

  public Context(Context parent) {
    this(parent, null, null, true);
  }

  public Context(Context parent, Map<String, ?> bindings) {
    this(parent, bindings, null, true);
  }

  public Context(
    Context parent,
    Map<String, ?> bindings,
    Map<Library, Set<String>> disabled
  ) {
    this(parent, bindings, disabled, true);
  }

  public Context(
    Context parent,
    Map<String, ?> bindings,
    Map<Library, Set<String>> disabled,
    boolean makeNewCallStacks
  ) {
    super(parent);
    this.disabled = disabled;

    if (bindings != null) {
      this.putAll(bindings);
    }

    this.parent = parent;

    this.extendPathStack =
      makeNewCallStacks
        ? new CallStack(
          parent == null ? null : parent.getExtendPathStack(),
          ExtendsTagCycleException.class
        )
        : parent == null ? null : parent.getExtendPathStack();
    this.importPathStack =
      makeNewCallStacks
        ? new CallStack(
          parent == null ? null : parent.getImportPathStack(),
          ImportTagCycleException.class
        )
        : parent == null ? null : parent.getImportPathStack();
    this.includePathStack =
      makeNewCallStacks
        ? new CallStack(
          parent == null ? null : parent.getIncludePathStack(),
          IncludeTagCycleException.class
        )
        : parent == null ? null : parent.getIncludePathStack();
    this.macroStack =
      makeNewCallStacks
        ? new CallStack(
          parent == null ? null : parent.getMacroStack(),
          MacroTagCycleException.class
        )
        : parent == null ? null : parent.getMacroStack();
    this.fromStack =
      makeNewCallStacks
        ? new CallStack(
          parent == null ? null : parent.getFromStack(),
          FromTagCycleException.class
        )
        : parent == null ? null : parent.getFromStack();
    this.currentPathStack =
      makeNewCallStacks
        ? new CallStack(
          parent == null ? null : parent.getCurrentPathStack(),
          TagCycleException.class
        )
        : parent == null ? null : parent.getCurrentPathStack();

    if (disabled == null) {
      disabled = new HashMap<>();
    }

    this.expTestLibrary =
      new ExpTestLibrary(parent == null, disabled.get(Library.EXP_TEST));
    this.filterLibrary = new FilterLibrary(parent == null, disabled.get(Library.FILTER));
    this.tagLibrary = new TagLibrary(parent == null, disabled.get(Library.TAG));
    this.functionLibrary =
      new FunctionLibrary(parent == null, disabled.get(Library.FUNCTION));
    if (parent != null) {
      this.expressionStrategy = parent.expressionStrategy;
      this.partialMacroEvaluation = parent.partialMacroEvaluation;
      this.unwrapRawOverride = parent.unwrapRawOverride;
    }
  }

  public void reset() {
    // clear anything that pushes up to its parent's values
    resolvedExpressions.clear();
    resolvedValues.clear();
    resolvedFunctions.clear();
    dependencies = HashMultimap.create();
    deferredNodes = new HashSet<>();
    eagerTokens = new HashSet<>();
  }

  @Override
  public Context getParent() {
    return parent;
  }

  public Map<String, Object> getSessionBindings() {
    return this.getScope();
  }

  @SuppressWarnings("unchecked")
  public Map<String, MacroFunction> getGlobalMacros() {
    Map<String, MacroFunction> macros = (Map<String, MacroFunction>) getScope()
      .get(GLOBAL_MACROS_SCOPE_KEY);

    if (macros == null) {
      macros = new HashMap<>();
      getScope().put(GLOBAL_MACROS_SCOPE_KEY, macros);
    }

    return macros;
  }

  public void addGlobalMacro(MacroFunction macro) {
    getGlobalMacros().put(macro.getName(), macro);
  }

  public MacroFunction getGlobalMacro(String identifier) {
    MacroFunction fn = getGlobalMacros().get(identifier);

    if (fn == null && parent != null) {
      fn = parent.getGlobalMacro(identifier);
    }

    return fn;
  }

  public boolean isGlobalMacro(String identifier) {
    return getGlobalMacro(identifier) != null;
  }

  public Optional<MacroFunction> getLocalMacro(String fullName) {
    String[] nameArray = fullName.split("\\.", 2);
    if (nameArray.length != 2) {
      return Optional.empty();
    }
    String localKey = nameArray[0];
    String macroName = nameArray[1];
    Object localValue = get(localKey);
    if (localValue instanceof DeferredValue) {
      localValue = ((DeferredValue) localValue).getOriginalValue();
    }
    if (!(localValue instanceof Map)) {
      return Optional.empty();
    }
    Object possibleMacroFunction = ((Map<String, Object>) localValue).get(macroName);
    if (possibleMacroFunction instanceof MacroFunction) {
      return Optional.of((MacroFunction) possibleMacroFunction);
    }
    return Optional.empty();
  }

  public boolean isAutoEscape() {
    if (autoEscape != null) {
      return autoEscape;
    }

    if (parent != null) {
      return parent.isAutoEscape();
    }

    return false;
  }

  public void setAutoEscape(Boolean autoEscape) {
    this.autoEscape = autoEscape;
  }

  public void addResolvedExpression(String expression) {
    resolvedExpressions.add(expression);
    if (getParent() != null) {
      getParent().addResolvedExpression(expression);
    }
  }

  public Set<String> getResolvedExpressions() {
    return ImmutableSet.copyOf(resolvedExpressions);
  }

  public boolean wasExpressionResolved(String expression) {
    return resolvedExpressions.contains(expression);
  }

  public void addResolvedValue(String value) {
    resolvedValues.add(value);
    if (getParent() != null) {
      getParent().addResolvedValue(value);
    }
  }

  public Set<String> getResolvedValues() {
    return ImmutableSet.copyOf(resolvedValues);
  }

  public boolean wasValueResolved(String value) {
    return resolvedValues.contains(value);
  }

  public Set<String> getResolvedFunctions() {
    return ImmutableSet.copyOf(resolvedFunctions);
  }

  public void addResolvedFunction(String function) {
    resolvedFunctions.add(function);
    if (getParent() != null) {
      getParent().addResolvedFunction(function);
    }
  }

  public void handleDeferredNode(Node node) {
    deferredNodes.add(node);
    Set<String> deferredProps = DeferredValueUtils.findAndMarkDeferredProperties(this);
    if (getParent() != null) {
      Context parent = getParent();
      //Ignore global context
      if (parent.getParent() != null) {
        //Place deferred values on the parent context
        deferredProps
          .stream()
          .filter(key -> !parent.containsKey(key))
          .forEach(key -> parent.put(key, this.get(key)));
        getParent().handleDeferredNode(node);
      }
    }
  }

  public Set<Node> getDeferredNodes() {
    return ImmutableSet.copyOf(deferredNodes);
  }

  public void handleEagerToken(EagerToken eagerToken) {
    eagerTokens.add(eagerToken);
    DeferredValueUtils.findAndMarkDeferredProperties(this, eagerToken);
    if (getParent() != null) {
      Context parent = getParent();
      //Ignore global context
      if (parent.getParent() != null) {
        parent.handleEagerToken(eagerToken);
      }
    }
  }

  public Set<EagerToken> getEagerTokens() {
    return eagerTokens;
  }

  public List<? extends Node> getSuperBlock() {
    if (superBlock != null) {
      return superBlock;
    }

    if (parent != null) {
      return parent.getSuperBlock();
    }

    return null;
  }

  public void setSuperBlock(List<? extends Node> superBlock) {
    this.superBlock = superBlock;
  }

  public void removeSuperBlock() {
    this.superBlock = null;
  }

  /**
   * Take all resolved strings from a context object and apply them to this context.
   * Useful for passing resolved values up a tag hierarchy.
   *
   * @param context - context object to apply resolved values from.
   */
  public void addResolvedFrom(Context context) {
    context.getResolvedExpressions().forEach(this::addResolvedExpression);
    context.getResolvedFunctions().forEach(this::addResolvedFunction);
    context.getResolvedValues().forEach(this::addResolvedValue);
  }

  @SafeVarargs
  @SuppressWarnings("unchecked")
  public final void registerClasses(Class<? extends Importable>... classes) {
    for (Class<? extends Importable> c : classes) {
      if (ExpTest.class.isAssignableFrom(c)) {
        expTestLibrary.registerClasses((Class<? extends ExpTest>) c);
      } else if (Filter.class.isAssignableFrom(c)) {
        filterLibrary.registerClasses((Class<? extends Filter>) c);
      } else if (Tag.class.isAssignableFrom(c)) {
        tagLibrary.registerClasses((Class<? extends Tag>) c);
      }
    }
  }

  public Collection<ExpTest> getAllExpTests() {
    List<ExpTest> expTests = new ArrayList<>(expTestLibrary.entries());

    if (parent != null) {
      expTests.addAll(parent.getAllExpTests());
    }

    return expTests;
  }

  public ExpTest getExpTest(String name) {
    ExpTest t = expTestLibrary.getExpTest(name);
    if (t != null) {
      return t;
    }
    if (parent != null) {
      return parent.getExpTest(name);
    }
    return null;
  }

  public void registerExpTest(ExpTest t) {
    expTestLibrary.addExpTest(t);
  }

  public Collection<Filter> getAllFilters() {
    List<Filter> filters = new ArrayList<>(filterLibrary.entries());

    if (parent != null) {
      filters.addAll(parent.getAllFilters());
    }

    return filters;
  }

  public Filter getFilter(String name) {
    Filter f = filterLibrary.getFilter(name);
    if (f != null) {
      return f;
    }
    if (parent != null) {
      return parent.getFilter(name);
    }
    return null;
  }

  public void registerFilter(Filter f) {
    filterLibrary.addFilter(f);
  }

  public boolean isFunctionDisabled(String name) {
    return (
      disabled != null &&
      disabled.getOrDefault(Library.FUNCTION, Collections.emptySet()).contains(name)
    );
  }

  public ELFunctionDefinition getFunction(String name) {
    ELFunctionDefinition f = functionLibrary.getFunction(name);
    if (f != null) {
      return f;
    }
    if (parent != null) {
      return parent.getFunction(name);
    }
    return null;
  }

  public Collection<ELFunctionDefinition> getAllFunctions() {
    List<ELFunctionDefinition> fns = new ArrayList<>(functionLibrary.entries());

    if (parent != null) {
      fns.addAll(parent.getAllFunctions());
    }

    final Set<String> disabledFunctions = disabled == null
      ? new HashSet<>()
      : disabled.getOrDefault(Library.FUNCTION, new HashSet<>());
    return fns
      .stream()
      .filter(f -> !disabledFunctions.contains(f.getName()))
      .collect(Collectors.toList());
  }

  public void registerFunction(ELFunctionDefinition f) {
    functionLibrary.addFunction(f);
  }

  public Collection<Tag> getAllTags() {
    List<Tag> tags = new ArrayList<>(tagLibrary.entries());

    if (parent != null) {
      tags.addAll(parent.getAllTags());
    }

    return tags;
  }

  public Tag getTag(String name) {
    Tag t = tagLibrary.getTag(name);
    if (t != null) {
      return t;
    }
    if (parent != null) {
      return parent.getTag(name);
    }
    return null;
  }

  public void registerTag(Tag t) {
    tagLibrary.addTag(t);
  }

  public ExpressionStrategy getExpressionStrategy() {
    return expressionStrategy;
  }

  public void setExpressionStrategy(ExpressionStrategy expressionStrategy) {
    this.expressionStrategy = expressionStrategy;
  }

  public Optional<String> getImportResourceAlias() {
    return Optional.ofNullable(get(IMPORT_RESOURCE_ALIAS_KEY)).map(Object::toString);
  }

  public CallStack getExtendPathStack() {
    return extendPathStack;
  }

  public CallStack getImportPathStack() {
    return importPathStack;
  }

  public CallStack getIncludePathStack() {
    return includePathStack;
  }

  private CallStack getFromStack() {
    return fromStack;
  }

  public CallStack getMacroStack() {
    return macroStack;
  }

  public CallStack getCurrentPathStack() {
    return currentPathStack;
  }

  public void pushFromStack(String path, int lineNumber, int startPosition) {
    fromStack.push(path, lineNumber, startPosition);
  }

  public void popFromStack() {
    fromStack.pop();
  }

  public int getRenderDepth() {
    if (renderDepth != -1) {
      return renderDepth;
    }

    if (parent != null) {
      return parent.getRenderDepth();
    }

    return 0;
  }

  public void setRenderDepth(int renderDepth) {
    this.renderDepth = renderDepth;
  }

  public void pushRenderStack(String template) {
    renderStack.push(template);
  }

  public String popRenderStack() {
    return renderStack.pop();
  }

  public boolean doesRenderStackContain(String template) {
    return renderStack.contains(template);
  }

  public void addDependency(String type, String identification) {
    this.dependencies.get(type).add(identification);
    if (parent != null) {
      parent.addDependency(type, identification);
    }
  }

  public void addDependencies(SetMultimap<String, String> dependencies) {
    this.dependencies.putAll(dependencies);
    if (parent != null) {
      parent.addDependencies(dependencies);
    }
  }

  public SetMultimap<String, String> getDependencies() {
    return this.dependencies;
  }

  public boolean isDeferredExecutionMode() {
    return deferredExecutionMode;
  }

  public Context setDeferredExecutionMode(boolean deferredExecutionMode) {
    this.deferredExecutionMode = deferredExecutionMode;
    return this;
  }

  public boolean getThrowInterpreterErrors() {
    return throwInterpreterErrors;
  }

  public void setThrowInterpreterErrors(boolean throwInterpreterErrors) {
    this.throwInterpreterErrors = throwInterpreterErrors;
  }

  public boolean isPartialMacroEvaluation() {
    return partialMacroEvaluation;
  }

  public void setPartialMacroEvaluation(boolean partialMacroEvaluation) {
    this.partialMacroEvaluation = partialMacroEvaluation;
  }

  public TemporaryValueClosable<Boolean> withPartialMacroEvaluation() {
    TemporaryValueClosable<Boolean> temporaryValueClosable = new TemporaryValueClosable<>(
      this.partialMacroEvaluation,
      this::setPartialMacroEvaluation
    );
    this.partialMacroEvaluation = true;
    return temporaryValueClosable;
  }

  public boolean isUnwrapRawOverride() {
    return unwrapRawOverride;
  }

  public void setUnwrapRawOverride(boolean unwrapRawOverride) {
    this.unwrapRawOverride = unwrapRawOverride;
  }

  public TemporaryValueClosable<Boolean> withUnwrapRawOverride() {
    TemporaryValueClosable<Boolean> temporaryValueClosable = new TemporaryValueClosable<>(
      this.unwrapRawOverride,
      this::setUnwrapRawOverride
    );
    this.unwrapRawOverride = true;
    return temporaryValueClosable;
  }

  public static class TemporaryValueClosable<T> implements AutoCloseable {
    private final T previousValue;
    private final Consumer<T> resetValueConsumer;

    private TemporaryValueClosable(T previousValue, Consumer<T> resetValueConsumer) {
      this.previousValue = previousValue;
      this.resetValueConsumer = resetValueConsumer;
    }

    @Override
    public void close() {
      resetValueConsumer.accept(previousValue);
    }
  }
}
