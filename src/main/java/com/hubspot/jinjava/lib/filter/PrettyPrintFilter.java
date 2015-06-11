package com.hubspot.jinjava.lib.filter;

import static com.hubspot.jinjava.util.Logging.ENGINE_LOG;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.date.PyishDate;

@JinjavaDoc(
    value = "Pretty print a variable. Useful for debugging.",
    params = @JinjavaParam(value = "value", type = "object", desc = "Object to Pretty Print"),
    snippets = {
        @JinjavaSnippet(
            code = "{% set this_var =\"Variable that I want to debug\" %}\n" +
                "{{ this_var|pprint }}")
    })
public class PrettyPrintFilter implements Filter {

  @Override
  public String getName() {
    return "pprint";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (var == null) {
      return "null";
    }

    String varStr = null;

    if (var instanceof String || var instanceof Number || var instanceof PyishDate || var instanceof Iterable || var instanceof Map) {
      varStr = Objects.toString(var);
    }
    else {
      varStr = objPropsToString(var);
    }

    return StringEscapeUtils.escapeHtml4("{% raw %}(" + var.getClass().getSimpleName() + ": " + varStr + "){% endraw %}");
  }

  private String objPropsToString(Object var) {
    List<String> props = new LinkedList<>();

    try {
      BeanInfo beanInfo = Introspector.getBeanInfo(var.getClass());

      for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
        try {
          if (pd.getPropertyType().equals(Class.class)) {
            continue;
          }

          Method readMethod = pd.getReadMethod();
          if (readMethod != null && !readMethod.getDeclaringClass().equals(Object.class)) {
            props.add(pd.getName() + "=" + readMethod.invoke(var));
          }
        } catch (Exception e) {
          ENGINE_LOG.error("Error reading bean value", e);
        }
      }

    } catch (IntrospectionException e) {
      ENGINE_LOG.error("Error inspecting bean", e);
    }

    return '{' + StringUtils.join(props, ", ") + '}';
  }

}
