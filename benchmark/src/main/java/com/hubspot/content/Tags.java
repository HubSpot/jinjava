package com.hubspot.content;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
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
  
}
