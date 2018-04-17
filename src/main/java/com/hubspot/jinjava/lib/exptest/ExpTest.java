package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.Importable;

/**
 * Beside filters there are also so called "tests" available. Tests can be used to test a variable against a common expression. To test a variable or expression you add is plus the name of the test after the variable. For example to find
 * out if a variable is defined you can do name is defined which will then return true or false depending on if name is defined.
 *
 * Tests can accept arguments too. If the test only takes one argument you can leave out the parentheses to group them. For example the following two expressions do the same:
 *
 * {% if loop.index is divisibleby 3 %} {% if loop.index is divisibleby(3) %}
 *
 * @author jstehler
 *
 */
public interface ExpTest extends Importable {

  boolean evaluate(Object var, JinjavaInterpreter interpreter, Object... args);

  default boolean evaluateNegated(Object var, JinjavaInterpreter interpreter, Object... args) {
    return !evaluate(var, interpreter, args);
  }

}
