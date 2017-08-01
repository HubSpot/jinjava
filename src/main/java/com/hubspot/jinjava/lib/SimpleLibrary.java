/**********************************************************************
Copyright (c) 2014 HubSpot Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 **********************************************************************/
package com.hubspot.jinjava.lib;

import static com.hubspot.jinjava.util.Logging.ENGINE_LOG;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.interpret.DisabledException;

public abstract class SimpleLibrary<T extends Importable> {

  private Map<String, T> lib = new HashMap<>();
  private Set<String> disabled = new HashSet<String>();

  protected SimpleLibrary(boolean registerDefaults) {
    this(registerDefaults, null);
  }

  protected SimpleLibrary(boolean registerDefaults, Set<String> disabled) {
    if (disabled != null) {
      this.disabled = ImmutableSet.copyOf(disabled);
    }
    if (registerDefaults) {
      registerDefaults();
    }
  }

  protected abstract void registerDefaults();

  public T fetch(String item) {
    if (disabled.contains(item)) {
      throw new DisabledException(item);
    }

    return lib.get(item);
  }

  @SafeVarargs
  public final List<T> registerClasses(Class<? extends T>... itemClass) {
    try {
      List<T> instances = new ArrayList<>();

      for (Class<? extends T> c : itemClass) {
        T instance = c.newInstance();
        register(instance);
        instances.add(instance);
      }

      return instances;
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public void register(T obj) {
    register(obj.getName(), obj);
  }

  public void register(String name, T obj) {
    if (!disabled.contains(obj.getName())) {
      lib.put(name, obj);
      ENGINE_LOG.debug(getClass().getSimpleName() + ": Registered " + obj.getName());
    }
  }

  public Collection<T> entries() {
    return lib.values().stream().filter(t -> !disabled.contains(t.getName())).collect(Collectors.toSet());
  }

}
