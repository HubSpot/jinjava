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
    @Override
    public String getName() {
      return "assign";
    }
  }
  
  public static class TableRowTag implements Tag {
    @Override
    public String interpret(TagNode tagNode, JinjavaInterpreter interpreter) {
      // TODO Auto-generated method stub
      return null;
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
