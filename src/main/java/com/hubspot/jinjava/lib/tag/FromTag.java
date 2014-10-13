package com.hubspot.jinjava.lib.tag;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.TagNode;

/**
 * Alternatively you can import names from the template into the current namespace:
 * 
 * {% from 'forms.html' import input as input_field, textarea %}
 * <dl>
 *   <dt>Username</dt>
 *   <dd>{{ input_field('username') }}</dd>
 *   <dt>Password</dt>
 *   <dd>{{ input_field('password', type='password') }}</dd>
 * </dl>
 * <p>{{ textarea('comment') }}</p>
 *   
 * @author jstehler
 */
public class FromTag implements Tag {

  @Override
  public String getName() {
    return "from";
  }

  @Override
  public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getEndTagName() {
    return null;
  }

}
