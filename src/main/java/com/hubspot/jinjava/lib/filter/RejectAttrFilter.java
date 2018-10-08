package com.hubspot.jinjava.lib.filter;

import java.util.Map;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Filters a sequence of objects by applying a test to an attribute of an object or the attribute and "
        + "rejecting the ones with the test succeeding.",
    params = {
        @JinjavaParam(value = "seq", type = "sequence", desc = "Sequence to test"),
        @JinjavaParam(value = "attribute", desc = "Attribute to test for and reject items that contain it"),
        @JinjavaParam(value = "exp_test", type = "name of expression test", defaultValue = "truthy", desc = "Specify which expression test to run for making the rejection")
    },
    snippets = {
        @JinjavaSnippet(
            desc = "This loop would reject any post containing content.post_list_summary_featured_image",
            code = "{% for content in contents|rejectattr('post_list_summary_featured_image') %}\n" +
                "    <div class=\"post-item\">Post in listing markup</div>\n" +
                "{% endfor %}")
    })
public class RejectAttrFilter extends SelectAttrFilter implements AdvancedFilter {

  @Override
  public String getName() {
    return "rejectattr";
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, Object[] args, Map<String, Object> kwargs) {
    return applyFilter(var, interpreter, args, kwargs, false);
  }
}
