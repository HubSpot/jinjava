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
import java.util.List;
import java.util.Map;

import com.google.common.collect.HashMultimap;
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
import com.hubspot.jinjava.util.ScopeMap;

public class Context extends ScopeMap<String, Object> {
  public static final String GLOBAL_MACROS_SCOPE_KEY = "__macros__";

  private final SetMultimap<String, String> dependencies = HashMultimap.create();

  private final ExpTestLibrary expTestLibrary;
  private final FilterLibrary filterLibrary;
  private final FunctionLibrary functionLibrary;
  private final TagLibrary tagLibrary;

  private final Context parent;

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
  }

  public Context(Context parent, Map<String, ?> bindings) {
    this(parent);
    this.putAll(bindings);
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

  public void addDependency(String type, String identification) {
    Context highestParentContext = getHighestParentContext();
    highestParentContext.dependencies.get(type).add(identification);
  }

  public SetMultimap<String, String> getDependencies() {
    Context highestParentContext = getHighestParentContext();
    return highestParentContext.dependencies;
  }

  private Context getHighestParentContext() {
    Context highestParentContext = parent != null ? parent : this;
    Context currentParentContext = highestParentContext.getParent();

    while (currentParentContext != null) {
      if (currentParentContext.equals(currentParentContext.getParent())) {
        return highestParentContext;
      }

      highestParentContext = highestParentContext.getParent();
      currentParentContext = highestParentContext.getParent();

    }
    return highestParentContext;
  }

}
