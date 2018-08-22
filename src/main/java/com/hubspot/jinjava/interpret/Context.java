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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.hubspot.jinjava.lib.Importable;
import com.hubspot.jinjava.lib.exptest.ExpTest;
import com.hubspot.jinjava.lib.exptest.ExpTestLibrary;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.lib.filter.FilterLibrary;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import com.hubspot.jinjava.lib.fn.FunctionLibrary;
import com.hubspot.jinjava.lib.fn.MacroFunction;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.lib.tag.TagLibrary;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.util.ScopeMap;

public class Context extends ScopeMap<String, Object> {
  public static final String GLOBAL_MACROS_SCOPE_KEY = "__macros__";

  private final SetMultimap<String, String> dependencies = HashMultimap.create();
  private Map<Library, Set<String>> disabled;

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

  private final Set<String> resolvedExpressions = new HashSet<>();
  private final Set<String> resolvedValues = new HashSet<>();
  private final Set<String> resolvedFunctions = new HashSet<>();

  private final ExpTestLibrary expTestLibrary;
  private final FilterLibrary filterLibrary;
  private final FunctionLibrary functionLibrary;
  private final TagLibrary tagLibrary;

  private final Context parent;

  private int renderDepth = -1;
  private Boolean autoEscape;
  private List<? extends Node> superBlock;

  private final Stack<String> renderStack = new Stack<>();

  public Context() {
    this(null, null, null);
  }

  public Context(Context parent) {
    this(parent, null, null);
  }

  public Context(Context parent, Map<String, ?> bindings) {
    this(parent, bindings, null);
  }

  public Context(Context parent, Map<String, ?> bindings, Map<Library, Set<String>> disabled) {
    super(parent);
    this.disabled = disabled;

    if (bindings != null) {
      this.putAll(bindings);
    }

    this.parent = parent;

    this.extendPathStack = new CallStack(parent == null ? null : parent.getExtendPathStack(),
                                         ExtendsTagCycleException.class);
    this.importPathStack = new CallStack(parent == null ? null : parent.getImportPathStack(),
                                         ImportTagCycleException.class);
    this.includePathStack = new CallStack(parent == null ? null : parent.getIncludePathStack(),
                                          IncludeTagCycleException.class);
    this.macroStack = new CallStack(parent == null ? null : parent.getMacroStack(), MacroTagCycleException.class);
    this.fromStack = new CallStack(parent == null ? null : parent.getFromStack(),
        FromTagCycleException.class);

    if (disabled == null) {
      disabled = new HashMap<>();
    }

    this.expTestLibrary = new ExpTestLibrary(parent == null, disabled.get(Library.EXP_TEST));
    this.filterLibrary = new FilterLibrary(parent == null, disabled.get(Library.FILTER));
    this.tagLibrary = new TagLibrary(parent == null, disabled.get(Library.TAG));
    this.functionLibrary = new FunctionLibrary(parent == null, disabled.get(Library.FUNCTION));
  }

  public void reset() {
    // clear anything that pushes up to its parent's values
    resolvedExpressions.clear();
    resolvedValues.clear();
    resolvedFunctions.clear();
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
    Map<String, MacroFunction> macros = (Map<String, MacroFunction>) getScope().get(GLOBAL_MACROS_SCOPE_KEY);

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
    return disabled != null && disabled.getOrDefault(Library.FUNCTION, Collections.emptySet()).contains(name);
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

    final Set<String> disabledFunctions = disabled == null ? new HashSet<>() : disabled.getOrDefault(Library.FUNCTION,
                                                                                                     new HashSet<>());
    return fns.stream().filter(f -> !disabledFunctions.contains(f.getName())).collect(Collectors.toList());
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
  }

  public void addDependencies(SetMultimap<String, String> dependencies) {
    this.dependencies.putAll(dependencies);
  }

  public SetMultimap<String, String> getDependencies() {
    return this.dependencies;
  }

}
