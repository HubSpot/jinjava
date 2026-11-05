/**********************************************************************
 * Copyright (c) 2026 HubSpot Inc.
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
package com.hubspot.jinjava.lib.filter;

import com.google.common.base.Splitter;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.el.TruthyTypeConverter;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.PartiallyDeferredValue;
import com.hubspot.jinjava.util.ObjectIterator;
import de.odysseus.el.misc.BooleanOperations;
import de.odysseus.el.misc.TypeConverter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.el.ELException;

@JinjavaDoc(
  value = "Return the largest item of an iterable, or null if it is empty. Ties return the first matching item.",
  input = @JinjavaParam(value = "value", type = "iterable", required = true),
  params = {
    @JinjavaParam(
      value = "case_sensitive",
      type = "boolean",
      defaultValue = "False",
      desc = "Treat upper and lower case strings as distinct"
    ),
    @JinjavaParam(
      value = "attribute",
      desc = "Compare an attribute or dotted attribute path instead of the items themselves"
    ),
  },
  snippets = {
    @JinjavaSnippet(code = "{{ [1, 2, 3]|max }}", output = "3"),
    @JinjavaSnippet(code = "{{ users|max(attribute='age') }}"),
  }
)
public class MaxFilter extends AbstractFilter implements AdvancedFilter {

  private static final TypeConverter TYPE_CONVERTER = new TruthyTypeConverter();
  private static final Splitter DOT_SPLITTER = Splitter.on('.').omitEmptyStrings();

  @Override
  public String getName() {
    return "max";
  }

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Map<String, Object> parsedArgs
  ) {
    checkDeferred(var, true);
    boolean caseSensitive = Boolean.TRUE.equals(parsedArgs.get("case_sensitive"));
    String attribute = (String) parsedArgs.get("attribute");
    List<String> path = attribute == null
      ? List.of()
      : DOT_SPLITTER.splitToList(attribute);
    Iterator<?> items = var instanceof String
      ? ((String) var).chars().mapToObj(c -> String.valueOf((char) c)).iterator()
      : ObjectIterator.getLoop(var);
    Object maximum = null;
    Object maximumKey = null;
    while (items.hasNext()) {
      Object item = items.next();
      checkDeferred(item, !path.isEmpty());
      if (item == null) {
        throw new InvalidInputException(interpreter, this, InvalidReason.NULL_IN_LIST);
      }
      Object key = path.isEmpty() ? item : interpreter.resolveProperty(item, path);
      checkDeferred(key, false);
      if (key == null) {
        throw new InvalidArgumentException(
          interpreter,
          this,
          InvalidReason.NULL_ATTRIBUTE_IN_LIST,
          "attribute",
          attribute
        );
      }
      if (maximum == null || isGreater(key, maximumKey, caseSensitive, interpreter)) {
        maximum = item;
        maximumKey = key;
      }
    }
    return maximum;
  }

  private boolean isGreater(
    Object candidate,
    Object maximum,
    boolean caseSensitive,
    JinjavaInterpreter interpreter
  ) {
    if (!caseSensitive && candidate instanceof String && maximum instanceof String) {
      return ((String) candidate).compareToIgnoreCase((String) maximum) > 0;
    }
    try {
      return BooleanOperations.gt(TYPE_CONVERTER, candidate, maximum);
    } catch (ELException | ClassCastException e) {
      throw new InvalidInputException(
        interpreter,
        getName(),
        "Invalid input for 'max': items must have comparable values"
      );
    }
  }

  private void checkDeferred(Object value, boolean allowPartial) {
    if (
      value instanceof DeferredValue &&
      !(allowPartial && value instanceof PartiallyDeferredValue)
    ) {
      throw new DeferredValueException("max");
    }
  }
}
