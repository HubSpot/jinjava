package com.hubspot.jinjava.benchmarks.liquid;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.tag.SetTag;
import com.hubspot.jinjava.lib.tag.Tag;
import com.hubspot.jinjava.tree.TagNode;

/**
 * Liquid::Template.register_tag 'paginate', Paginate
 * Liquid::Template.register_tag 'form', CommentForm
 *
 * @author jstehler
 *
 */
public class Tags {

  public static class PaginateTag implements Tag {
    private static final long serialVersionUID = -4143036883302838710L;

    @Override
    public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
      return null;
    }

    @Override
    public String getName() {
      return "paginate";
    }

    @Override
    public String getEndTagName() {
      return "endpaginate";
    }
  }

  public static class CommentFormTag implements Tag {
    private static final long serialVersionUID = 4740110980519195813L;

    @Override
    public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {

      return null;
    }

    @Override
    public String getName() {
      return "form";
    }

    @Override
    public String getEndTagName() {
      return "endform";
    }
  }

  public static class AssignTag extends SetTag {
    private static final long serialVersionUID = -8045822376271136191L;

    @Override
    public String getName() {
      return "assign";
    }
  }

  public static class TableRowTag implements Tag {
    private static final long serialVersionUID = 7058892410901688159L;

    @Override
    public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
      return "";
    }

    @Override
    public String getName() {
      return "tablerow";
    }

    @Override
    public String getEndTagName() {
      return "endtablerow";
    }
  }

}
