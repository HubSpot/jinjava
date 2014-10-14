package com.hubspot.jinjava.lib.tag;

import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.TagNode;

/**
 * TODO Note: this tag is not currently implemented!
 * 
 * In some cases it can be useful to pass a macro to another macro. For this purpose you can use the 
 * special call block. The following example shows a macro that takes advantage of the call 
 * functionality and how it can be used:
 * 
 * <pre>
 * {% macro render_dialog(title, class='dialog') -%}
 *  &lt;div class="{{ class }}"&gt;
 *      &lt;h2&gt;{{ title }}&lt;/h2&gt;
 *      &lt;div class="contents"&gt;
 *          {{ caller() }}
 *      &lt;/div&gt;
 *  &lt;/div&gt;
 * {%- endmacro %}
 * 
 * {% call render_dialog('Hello World') %}
 *     This is a simple dialog rendered by using a macro and
 *     a call block.
 * {% endcall %}
 * </pre>
 * 
 * It’s also possible to pass arguments back to the call block. This makes it useful as replacement 
 * for loops. Generally speaking a call block works exactly like an macro, just that it doesn’t have a name.
 * 
 * Here an example of how a call block can be used with arguments:
 * 
 * <pre>
 * {% macro dump_users(users) -%}
 *   &lt;ul&gt;
 *     {%- for user in users %}
 *       &lt;li&gt;&lt;p&gt;{{ user.username|e }}&lt;/p&gt;{{ caller(user) }}&lt;/li&gt;
 *     {%- endfor %}
 *   &lt;/ul&gt;
 * {%- endmacro %}
 * 
 * {% call(user) dump_users(list_of_user) %}
 *  &lt;dl&gt;
 *       &lt;dl&gt;Realname&lt;/dl&gt;
 *       &lt;dd&gt;{{ user.realname|e }}&lt;/dd&gt;
 *       &lt;dl&gt;Description&lt;/dl&gt;
 *       &lt;dd&gt;{{ user.description }}&lt;/dd&gt;
 *  &lt;/dl&gt;
 * {% endcall %}
 * </pre>
 * 
 * @author jstehler
 *
 */
public class CallTag implements Tag {

  @Override
  public String getName() {
    return "call";
  }
  
  @Override
  public String getEndTagName() {
    return "endcall";
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
	  throw new InterpretException("Tag " + getName() + " is not implemented.", interpreter.getLineNumber());
  }

}
