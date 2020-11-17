package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

@JinjavaDoc(
  value = "Truncates a given string, respecting html markup (i.e. will properly close all nested tags)",
  input = @JinjavaParam(value = "html", desc = "HTML to truncate", required = true),
  params = {
    @JinjavaParam(
      value = TruncateHtmlFilter.LENGTH_KEY,
      type = "int",
      defaultValue = "255",
      desc = "Length at which to truncate text (HTML characters not included)"
    ),
    @JinjavaParam(
      value = TruncateHtmlFilter.END_KEY,
      defaultValue = "...",
      desc = "The characters that will be added to indicate where the text was truncated"
    ),
    @JinjavaParam(
      value = TruncateHtmlFilter.BREAKWORD_KEY,
      type = "boolean",
      defaultValue = "false",
      desc = "If set to true, text will be truncated in the middle of words"
    )
  },
  snippets = {
    @JinjavaSnippet(
      code = "{{ \"<p>I want to truncate this text without breaking my HTML<p>\"|truncatehtml(28, '..', false) }}",
      output = "<p>I want to truncate this text without breaking my HTML</p>"
    )
  }
)
public class TruncateHtmlFilter extends AbstractFilter implements AdvancedFilter {
  public static final String LENGTH_KEY = "length";
  public static final String END_KEY = "end";
  public static final String BREAKWORD_KEY = "breakword";

  @Override
  public String getName() {
    return "truncatehtml";
  }

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Map<String, Object> parsedArgs
  ) {
    int length = ((int) parsedArgs.get(LENGTH_KEY));
    String end = (String) parsedArgs.get(END_KEY);
    boolean breakword = (boolean) parsedArgs.get(BREAKWORD_KEY);
    Document dom = Jsoup.parseBodyFragment((String) var);

    ContentTruncatingNodeVisitor visitor = new ContentTruncatingNodeVisitor(
      length,
      end,
      breakword
    );
    dom.select("body").traverse(visitor);
    dom.select(".__deleteme").remove();
    return dom.select("body").html();
  }

  @Override
  protected Object parseArg(
    JinjavaInterpreter interpreter,
    JinjavaParam jinjavaParamMetadata,
    Object value
  ) {
    if (jinjavaParamMetadata.value().equals(LENGTH_KEY) && interpreter != null) {
      try {
        return super.parseArg(interpreter, jinjavaParamMetadata, value);
      } catch (Exception e) {
        return getDefaultValue(LENGTH_KEY);
      }
    }
    return super.parseArg(interpreter, jinjavaParamMetadata, value);
  }

  private static class ContentTruncatingNodeVisitor implements NodeVisitor {
    private int maxTextLen;
    private int textLen;
    private String ending;
    private boolean killwords;

    ContentTruncatingNodeVisitor(int maxTextLen, String ending, boolean killwords) {
      this.maxTextLen = maxTextLen;
      this.ending = ending;
      this.killwords = killwords;
    }

    @Override
    public void head(Node node, int depth) {
      if (node instanceof TextNode) {
        TextNode text = (TextNode) node;
        String textContent = text.text();

        if (textLen >= maxTextLen) {
          text.text("");
        } else if (textLen + textContent.length() > maxTextLen) {
          int ptr = maxTextLen - textLen;
          if (!killwords) {
            ptr = Functions.movePointerToJustBeforeLastWord(ptr, textContent) - 1;
          }

          text.text(textContent.substring(0, ptr) + ending);
          textLen = maxTextLen;
        } else {
          textLen += textContent.length();
        }
      }
    }

    @Override
    public void tail(Node node, int depth) {
      if (node instanceof Element) {
        Element el = (Element) node;
        if (StringUtils.isBlank(el.text())) {
          el.addClass("__deleteme");
        }
      }
    }
  }
}
