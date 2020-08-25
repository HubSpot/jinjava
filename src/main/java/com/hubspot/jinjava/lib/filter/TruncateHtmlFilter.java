package com.hubspot.jinjava.lib.filter;

import static com.hubspot.jinjava.util.Logging.ENGINE_LOG;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.fn.Functions;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.BooleanUtils;
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
      value = "length",
      type = "number",
      defaultValue = "255",
      desc = "Length at which to truncate text (HTML characters not included)"
    ),
    @JinjavaParam(
      value = "end",
      defaultValue = "...",
      desc = "The characters that will be added to indicate where the text was truncated"
    ),
    @JinjavaParam(
      value = "breakword",
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
public class TruncateHtmlFilter implements AdvancedFilter {
  private static final int DEFAULT_TRUNCATE_LENGTH = 255;
  private static final String DEFAULT_END = "...";
  private static final String LENGTH_KEY = "length";
  private static final String END_KEY = "end";
  private static final String BREAKWORD_KEY = "breakword";

  @Override
  public String getName() {
    return "truncatehtml";
  }

  @Override
  public Object filter(
    Object var,
    JinjavaInterpreter interpreter,
    Object[] args,
    Map<String, Object> kwargs
  ) {
    String length = null;
    if (kwargs.containsKey((LENGTH_KEY)) {
      length = Objects.toString(kwargs.get(LENGTH_KEY));
    }
    String end = null;
    if (kwargs.containsKey(END_KEY)) {
      end = Objects.toString(kwargs.get(END_KEY));
    }
    String breakword = null;
    if (kwargs.containsKey(BREAKWORD_KEY)) {
      breakword = Objects.toString(kwargs.get(BREAKWORD_KEY));
    }

    String[] newArgs = new String[3];
    for (int i = 0; i < args.length; i++) {
      if (i >= newArgs.length) {
        break;
      }
      newArgs[i] = Objects.toString(args[i]);
    }

    if (length != null) {
      newArgs[0] = length;
    }
    if (end != null) {
      newArgs[1] = end;
    }
    if (breakword != null) {
      newArgs[2] = breakword;
    }

    return filter(var, interpreter, newArgs);
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (var instanceof String) {
      int length = DEFAULT_TRUNCATE_LENGTH;
      String ends = DEFAULT_END;

      if (args.length > 0) {
        try {
          length = Integer.parseInt(Objects.toString(args[0]));
        } catch (Exception e) {
          ENGINE_LOG.warn(
            "truncatehtml(): error setting length for {}, using default {}",
            args[0],
            DEFAULT_TRUNCATE_LENGTH
          );
        }
      }

      if (args.length > 1 && args[1] != null) {
        ends = Objects.toString(args[1]);
      }

      boolean killwords = false;
      if (args.length > 2 && args[2] != null) {
        killwords = BooleanUtils.toBoolean(args[2]);
      }

      Document dom = Jsoup.parseBodyFragment((String) var);
      ContentTruncatingNodeVisitor visitor = new ContentTruncatingNodeVisitor(
        length,
        ends,
        killwords
      );
      dom.select("body").traverse(visitor);
      dom.select(".__deleteme").remove();

      return dom.select("body").html();
    }

    return var;
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
