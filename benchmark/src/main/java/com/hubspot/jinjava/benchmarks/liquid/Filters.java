package com.hubspot.jinjava.benchmarks.liquid;

import static org.apache.commons.lang3.math.NumberUtils.toDouble;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.DatetimeFilter;
import com.hubspot.jinjava.lib.filter.Filter;
import com.hubspot.jinjava.lib.fn.Functions;

/**
 * Liquid::Template.register_filter JsonFilter
 * Liquid::Template.register_filter MoneyFilter
 * Liquid::Template.register_filter WeightFilter
 * Liquid::Template.register_filter ShopFilter
 * Liquid::Template.register_filter TagFilter
 *
 * @author jstehler
 *
 */
public class Filters {

  /**
   * override date filter to match liquid functionality
   */
  public static class OverrideDateFilter extends DatetimeFilter {
    @Override
    public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
      return Functions.dateTimeFormat(ZonedDateTime.now(), arg);
    }
  }

  public static class JsonFilter implements Filter {
    @Override
    public String getName() {
      return "json";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      return null;
    }
  }

  public static class MoneyFilter implements Filter {
    @Override
    public String getName() {
      return "money";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      if(var == null) {
        return "";
      }

      double money = toDouble(Objects.toString(var));
      return String.format("$ %.2f", money / 100.0);
    }
  }

  public static class MoneyWithCurrencyFilter extends MoneyFilter {
    @Override
    public String getName() {
      return "money_with_currency";
    }
    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      Object val = super.filter(var, interpreter, args);
      if(val.toString().length() == 0) {
        return "";
      }
      return val + " USD";
    }
  }

  public static class WeightFilter implements Filter {
    @Override
    public String getName() {
      return "weight";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      double grams = toDouble(Objects.toString(var));
      return String.format("%.2f", grams / 1000);
    }
  }

  public static class WeightWithUnitFilter extends WeightFilter {
    @Override
    public String getName() {
      return "weight_with_unit";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      return super.filter(var, interpreter, args) + " kg";
    }
  }

  public static class ShopAssetUrlFilter implements Filter {
    @Override
    public String getName() {
      return "asset_url";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      return "/files/1/[shop_id]/[shop_id]/assets/" + var;
    }
  }

  public static class ShopGlobalAssetUrl implements Filter {
    @Override
    public String getName() {
      return "global_asset_url";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      return "/global/" + var;
    }
  }

  public static class ShopShopifyAssetUrl implements Filter {
    @Override
    public String getName() {
      return "global_asset_url";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      return "/shopify/" + var;
    }
  }

  public static class ShopScriptTag implements Filter {
    @Override
    public String getName() {
      return "script_tag";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      return String.format("<script src=\"%s\" type=\"text/javascript\"></script>", var);
    }
  }

  public static class ShopStylesheetTag implements Filter {
    @Override
    public String getName() {
      return "stylesheet_tag";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      String media = "all";
      if(args.length > 0) {
        media = args[0];
      }
      return String.format("<link href=\"%s\" rel=\"stylesheet\" type=\"text/css\"  media=\"%s\"  />", var, media);
    }
  }

  public static class ShopLinkTo implements Filter {
    @Override
    public String getName() {
      return "link_to";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      String url = args[0];
      String title = "";
      if(args.length > 1) {
        title = args[1];
      }
      return String.format("<a href=\"%s\" title=\"%s\">%s</a>", url, title, var);
    }
  }

  public static class ShopImgTagFilter implements Filter {
    @Override
    public String getName() {
      return "img_tag";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      String alt = "";
      if(args.length > 0) {
        alt = args[0];
      }

      return String.format("<img src=\"%s\" alt=\"%s\" />", var, alt);
    }
  }

  public static class LinkToTagFilter implements Filter {
    @Override
    public String getName() {
      return "link_to_tag";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      String label = Objects.toString(var);
      String tag = args[0];

      return String.format("<a title=\"Show tag %s\" href=\"/collections/%s/%s\">%s</a>", tag, interpreter.getContext().get("handle"), tag, label);
    }
  }

  public static class HighlightActiveTagFilter implements Filter {
    @Override
    public String getName() {
      return "highlight_active_tag";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      String tag = Objects.toString(var);
      String cssClass = "active";
      if(args.length > 0) {
        cssClass = args[0];
      }

      Collection<String> currentTags = getCurrentTags(interpreter);
      if(currentTags.contains(tag)) {
        return String.format("<span class=\"%s\">%s</span>", cssClass, tag);
      }
      else {
        return tag;
      }
    }
  }

  public static class LinkToAddTagFilter implements Filter {
    @Override
    public String getName() {
      return "link_to_add_tag";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      String label = Objects.toString(var);
      String tag = args[0];

      Set<String> tags = new TreeSet<String>(getCurrentTags(interpreter));
      tags.add(tag);

      return String.format("<a title=\"Show tag %s\" href=\"/collections/%s/%s\">%s</a>",
          tag, interpreter.getContext().get("handle"), StringUtils.join(tags, '+'), label);
    }
  }

  public static class LinkToRemoveTagFilter implements Filter {
    @Override
    public String getName() {
      return "link_to_remove_tag";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      String label = Objects.toString(var);
      String tag = args[0];

      Set<String> tags = new TreeSet<String>(getCurrentTags(interpreter));
      tags.remove(tag);

      return String.format("<a title=\"Show tag %s\" href=\"/collections/%s/%s\">%s</a>",
          tag, interpreter.getContext().get("handle"), StringUtils.join(tags, '+'), label);
    }
  }

  @SuppressWarnings("unchecked")
  private static Collection<String> getCurrentTags(JinjavaInterpreter interpreter) {
    Collection<String> currentTags = (Collection<String>) interpreter.getContext().get("current_tags", new ArrayList<String>());
    return currentTags;
  }

}
