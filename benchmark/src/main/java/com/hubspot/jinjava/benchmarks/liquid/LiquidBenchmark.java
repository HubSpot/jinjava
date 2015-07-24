package com.hubspot.jinjava.benchmarks.liquid;

import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.FileUtils.readFileToString;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.Node;

import ch.qos.logback.classic.Level;

@State(Scope.Benchmark)
public class LiquidBenchmark {

  public List<String> templates;
  public Map<String, ?> bindings;

  public Jinjava jinjava;

  @SuppressWarnings("unchecked")
  @Setup
  public void setup() throws IOException {
    ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    logger.setLevel(Level.WARN);

    jinjava = new Jinjava();

    jinjava.getGlobalContext().registerClasses(
        Filters.OverrideDateFilter.class,

    Filters.JsonFilter.class,
        Filters.LinkToAddTagFilter.class,
        Filters.LinkToRemoveTagFilter.class,
        Filters.LinkToTagFilter.class,
        Filters.HighlightActiveTagFilter.class,
        Filters.MoneyFilter.class,
        Filters.MoneyWithCurrencyFilter.class,
        Filters.ShopAssetUrlFilter.class,
        Filters.ShopGlobalAssetUrl.class,
        Filters.ShopImgTagFilter.class,
        Filters.ShopLinkTo.class,
        Filters.ShopScriptTag.class,
        Filters.ShopShopifyAssetUrl.class,
        Filters.ShopStylesheetTag.class,
        Filters.WeightFilter.class,
        Filters.WeightWithUnitFilter.class,

    Tags.AssignTag.class,
        Tags.CommentFormTag.class,
        Tags.PaginateTag.class,
        Tags.TableRowTag.class);

    templates = new ArrayList<>();

    Map<String, Object> db = (Map<String, Object>) new Yaml().load(readFileToString(new File("liquid/performance/shopify/vision.database.yml"), StandardCharsets.UTF_8));
    bindings = new HashMap<>(initDb(db));

    File baseDir = new File("liquid/performance/tests");
    for (File tmpl : listFiles(baseDir, new String[] { "liquid" }, true)) {

      String template = readFileToString(tmpl, StandardCharsets.UTF_8);
      // convert filter syntax from ':' to '()'
      template = template.replaceAll("\\| ([\\w_]+): (.*?)(\\||})", "| $1($2)$3");
      // jinjava doesn't have the '?' postfix binary operator
      template = template.replaceAll("if (.*?)\\?", "if $1");
      // no support for offset:n
      template = template.replaceAll("offset:\\s*\\d*", "");
      // no support for limit:n
      template = template.replaceAll("limit:\\s*\\d*", "");
      // no support for cols:n
      template = template.replaceAll("cols:\\s*\\d*", "");
      // no support for for reversal
      template = template.replaceAll(" reversed", "");

      // System.out.println("Adding template: " + tmpl.getAbsolutePath());
      // System.out.println(template);

      templates.add(template);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> initDb(Map<String, Object> db) {
    for (Map.Entry<String, ?> entry : db.entrySet()) {
      if (entry.getValue() instanceof List) {
        List<?> values = (List<?>) entry.getValue();

        if (values.size() > 0 && values.get(0) instanceof Map) {
          Map<String, Object> byHandle = new HashMap<>();

          for (Map<String, Object> val : (List<Map<String, Object>>) values) {
            String handle = val.getOrDefault("handle", "").toString();

            if (handle.trim().length() > 0) {
              byHandle.put(handle, val);
            }
          }

          if (byHandle.size() > 0) {
            db.put(entry.getKey(), byHandle);
          }
        }
      }
    }

    return db;
  }

  @Benchmark
  public void parse(Blackhole blackhole) {
    JinjavaInterpreter interpreter = jinjava.newInterpreter();

    for (String template : templates) {
      Node parsed = interpreter.parse(template);
      if (blackhole != null) {
        blackhole.consume(parsed);
      }
    }
  }

  @Benchmark
  public void parseAndRender(Blackhole blackhole) {
    for (String template : templates) {
      String result = jinjava.render(template, bindings);
      if (blackhole != null) {
        blackhole.consume(result);
      }
    }
  }

  public static void main(String[] args) throws Exception {
    LiquidBenchmark b = new LiquidBenchmark();
    b.setup();
    b.parse(null);
    b.parseAndRender(null);
  }

}
