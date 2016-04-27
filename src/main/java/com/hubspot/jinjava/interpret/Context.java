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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  private final CallStack extendPathStack;
  private final CallStack importPathStack;
  private final CallStack includePathStack;
  private final CallStack macroStack;

  private final Set<String> resolvedExpressions = new HashSet<>();
  private final Set<String> resolvedValues = new HashSet<>();

  private final ExpTestLibrary expTestLibrary;
  private final FilterLibrary filterLibrary;
  private final FunctionLibrary functionLibrary;
  private final TagLibrary tagLibrary;

  private final Context parent;

  private int renderDepth = -1;
  private Boolean autoEscape;
  private List<? extends Node> superBlock;

  public Context() {
    this(null);
  }

  public Context(Context parent) {
    super(parent);

    this.parent = parent;
    this.expTestLibrary = new ExpTestLibrary(parent == null);
    this.filterLibrary = new FilterLibrary(parent == null);
    this.functionLibrary = new FunctionLibrary(parent == null);
    this.tagLibrary = new TagLibrary(parent == null);
    this.extendPathStack = new CallStack(parent == null ? null : parent.getExtendPathStack(), ExtendsTagCycleException.class);
    this.importPathStack = new CallStack(parent == null ? null : parent.getImportPathStack(), ImportTagCycleException.class);
    this.includePathStack = new CallStack(parent == null ? null : parent.getIncludePathStack(), IncludeTagCycleException.class);
    this.macroStack = new CallStack(parent == null ? null : parent.getMacroStack(), MacroTagCycleException.class);
  }

  public Context(Context parent, Map<String, ?> bindings) {
    this(parent);
    if (bindings != null) {
      this.putAll(bindings);
    }
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
      return autoEscape.booleanValue();
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
  }

  public Set<String> getResolvedExpressions() {
    return ImmutableSet.copyOf(resolvedExpressions);
  }

  public boolean wasExpressionResolved(String expression) {
    return resolvedExpressions.contains(expression);
  }

  public void addResolvedValue(String value) {
    resolvedValues.add(value);
  }

  public Set<String> getResolvedValues() {
    return ImmutableSet.copyOf(resolvedValues);
  }

  public boolean wasValueResolved(String value) {
    return resolvedValues.contains(value);
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

    return fns;
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

  public CallStack getMacroStack() {
    return macroStack;
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

  public void addDependency(String type, String identification) {
    this.dependencies.get(type).add(identification);
  }

  public SetMultimap<String, String> getDependencies() {
    return this.dependencies;
  }

}
