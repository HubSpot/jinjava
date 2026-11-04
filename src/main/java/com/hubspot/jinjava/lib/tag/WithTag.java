/**
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
 */
package com.hubspot.jinjava.lib.tag;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaHasCodeBody;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.doc.annotations.JinjavaTextMateSnippet;
import com.hubspot.jinjava.interpret.Context.Library;
import com.hubspot.jinjava.interpret.DisabledException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.JinjavaInterpreter.InterpreterScopeClosable;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorItem;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import com.hubspot.jinjava.interpret.TemplateSyntaxException;
import com.hubspot.jinjava.tree.Node;
import com.hubspot.jinjava.tree.TagNode;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.util.LengthLimitingStringBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Jinja's scoped assignment statement:
 *
 * <pre>
 * {% with a = 1, b = foo.bar %}
 *   {{ a }} {{ b }}
 * {% endwith %}
 * </pre>
 *
 * The assigned names, and anything set inside the block, are only visible until
 * {@code endwith}. As in Jinja, every value is evaluated in the enclosing scope
 * before any name is bound, so {@code {% with a = 1, b = a %}} binds {@code b} to the
 * outer {@code a}.
 */
@JinjavaDoc(
  value = "Creates a new inner scope. Variables assigned in the with tag, or set inside its body, are not visible outside of it.",
  snippets = {
    @JinjavaSnippet(
      code = "{% with name = \"my_job\", schedule = \"0 1 * * 0\" %}\n" +
      "  {% include \"cron_job.html\" %}\n" +
      "{% endwith %}"
    ),
    @JinjavaSnippet(
      desc = "A with tag without assignments just scopes the variables set inside it",
      code = "{% with %}\n" + "  {% set foo = 42 %}\n" + "  {{ foo }}\n" + "{% endwith %}"
    ),
  }
)
@JinjavaHasCodeBody
@JinjavaTextMateSnippet(code = "{% with ${1:name} = ${2:value} %}\n$0\n{% endwith %}")
public class WithTag implements Tag {

  public static final String TAG_NAME = "with";

  private static final long serialVersionUID = -2880947254236361217L;
  private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

  @Override
  public String getName() {
    return TAG_NAME;
  }

  @Override
  public boolean isRenderedInValidationMode() {
    return true;
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    Assignments assignments = parseAssignments(tagNode);
    if (!assignments.isEmpty() && isSetTagDisabled(interpreter)) {
      // Assignments go through the set tag, so with must not become a way around
      // a disabled set tag.
      DisabledException e = new DisabledException(SetTag.TAG_NAME);
      interpreter.addError(
        new TemplateError(
          ErrorType.FATAL,
          ErrorReason.DISABLED,
          ErrorItem.TAG,
          e.getMessage(),
          TAG_NAME,
          tagNode.getLineNumber(),
          tagNode.getStartPosition(),
          e
        )
      );
      return "";
    }
    try (InterpreterScopeClosable c = interpreter.enterScope()) {
      LengthLimitingStringBuilder result = new LengthLimitingStringBuilder(
        interpreter.getConfig().getMaxOutputSize()
      );
      if (!assignments.isEmpty()) {
        // Delegate to the registered set tag so that a multi-target set evaluates
        // every value before binding any name, and so that eager execution can
        // preserve deferred assignments the same way it does for set.
        result.append(
          interpreter
            .getContext()
            .getTag(SetTag.TAG_NAME)
            .interpret(buildSetNode(tagNode, assignments, interpreter), interpreter)
        );
      }
      for (Node child : tagNode.getChildren()) {
        result.append(child.render(interpreter));
      }
      return result.toString();
    }
  }

  private static boolean isSetTagDisabled(JinjavaInterpreter interpreter) {
    Map<Library, Set<String>> disabled = interpreter.getConfig().getDisabled();
    if (disabled == null) {
      return false;
    }
    Set<String> disabledTags = disabled.get(Library.TAG);
    return disabledTags != null && disabledTags.contains(SetTag.TAG_NAME);
  }

  private static TagNode buildSetNode(
    TagNode tagNode,
    Assignments assignments,
    JinjavaInterpreter interpreter
  ) {
    TagToken withToken = (TagToken) tagNode.getMaster();
    String image = String.format(
      "%s %s %s = %s %s",
      withToken.getSymbols().getExpressionStartWithTag(),
      SetTag.TAG_NAME,
      String.join(", ", assignments.names),
      String.join(", ", assignments.expressions),
      withToken.getSymbols().getExpressionEndWithTag()
    );
    TagToken setToken = new TagToken(
      image,
      withToken.getLineNumber(),
      withToken.getStartPosition(),
      withToken.getSymbols()
    );
    return new TagNode(
      interpreter.getContext().getTag(SetTag.TAG_NAME),
      setToken,
      withToken.getSymbols()
    );
  }

  public static Assignments parseAssignments(TagNode tagNode) {
    Assignments assignments = new Assignments();
    String helpers = tagNode.getHelpers();
    if (StringUtils.isBlank(helpers)) {
      return assignments;
    }
    for (String part : splitTopLevel(helpers, ',')) {
      int eqPos = findAssignmentOperator(part);
      if (eqPos < 0) {
        throw syntaxError(
          tagNode,
          "Tag 'with' expects assignments like 'name = value', got '" + part.trim() + "'"
        );
      }
      String name = part.substring(0, eqPos).trim();
      String expression = part.substring(eqPos + 1).trim();
      if (!IDENTIFIER.matcher(name).matches()) {
        throw syntaxError(tagNode, "Tag 'with' can not assign to '" + name + "'");
      }
      if (expression.isEmpty()) {
        throw syntaxError(tagNode, "Tag 'with' requires a value for '" + name + "'");
      }
      if (assignments.names.contains(name)) {
        throw syntaxError(tagNode, "Tag 'with' assigns '" + name + "' more than once");
      }
      assignments.names.add(name);
      assignments.expressions.add(expression);
    }
    return assignments;
  }

  private static TemplateSyntaxException syntaxError(TagNode tagNode, String message) {
    return new TemplateSyntaxException(
      tagNode.getMaster().getImage(),
      message,
      tagNode.getLineNumber(),
      tagNode.getStartPosition()
    );
  }

  /**
   * Splits on the separator when it is outside of quotes, parentheses, brackets
   * and braces.
   */
  static List<String> splitTopLevel(String input, char separator) {
    List<String> parts = new ArrayList<>();
    int depth = 0;
    char quote = 0;
    int start = 0;
    for (int i = 0; i < input.length(); i++) {
      char ch = input.charAt(i);
      if (quote != 0) {
        if (ch == '\\') {
          i++;
        } else if (ch == quote) {
          quote = 0;
        }
        continue;
      }
      if (ch == '\'' || ch == '"') {
        quote = ch;
      } else if (ch == '(' || ch == '[' || ch == '{') {
        depth++;
      } else if (ch == ')' || ch == ']' || ch == '}') {
        depth--;
      } else if (ch == separator && depth == 0) {
        parts.add(input.substring(start, i));
        start = i + 1;
      }
    }
    parts.add(input.substring(start));
    return parts;
  }

  /**
   * Index of the first '=' that is an assignment, not part of ==, !=, &lt;= or &gt;=.
   */
  private static int findAssignmentOperator(String part) {
    char quote = 0;
    for (int i = 0; i < part.length(); i++) {
      char ch = part.charAt(i);
      if (quote != 0) {
        if (ch == '\\') {
          i++;
        } else if (ch == quote) {
          quote = 0;
        }
        continue;
      }
      if (ch == '\'' || ch == '"') {
        quote = ch;
      } else if (ch == '=') {
        char prev = i > 0 ? part.charAt(i - 1) : 0;
        char next = i + 1 < part.length() ? part.charAt(i + 1) : 0;
        if (next != '=' && prev != '=' && prev != '!' && prev != '<' && prev != '>') {
          return i;
        }
        if (next == '=') {
          i++;
        }
      }
    }
    return -1;
  }

  public static class Assignments {

    private final List<String> names = new ArrayList<>();
    private final List<String> expressions = new ArrayList<>();

    public List<String> getNames() {
      return names;
    }

    public List<String> getExpressions() {
      return expressions;
    }

    public boolean isEmpty() {
      return names.isEmpty();
    }
  }
}
