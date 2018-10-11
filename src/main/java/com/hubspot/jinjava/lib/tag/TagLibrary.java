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
package com.hubspot.jinjava.lib.tag;

import java.util.Set;

import com.hubspot.jinjava.lib.SimpleLibrary;

public class TagLibrary extends SimpleLibrary<Tag> {

  public TagLibrary(boolean registerDefaults, Set<String> disabled) {
    super(registerDefaults, disabled);
  }

  @Override
  protected void registerDefaults() {
    registerClasses(
        AutoEscapeTag.class,
        BlockTag.class,
        CallTag.class,
        CycleTag.class,
        ElseTag.class,
        ElseIfTag.class,
        ExtendsTag.class,
        ForTag.class,
        FromTag.class,
        IfTag.class,
        IfchangedTag.class,
        ImportTag.class,
        IncludeTag.class,
        MacroTag.class,
        PrintTag.class,
        RawTag.class,
        SetTag.class,
        UnlessTag.class,
        DoTag.class);
  }

  public Tag getTag(String tagName) {
    return fetch(tagName);
  }

  public void addTag(Tag t) {
    register(t);
  }

  @Override
  public void register(Tag t) {
    super.register(t);

    if (t.getEndTagName() != null) {
      register(t.getEndTagName(), new EndTag(t));
    }
  }

}
