package com.hubspot.jinjava.benchmarks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 5, time = 3)
public class JinjavaBenchmark {

  private Jinjava jinjava;
  private String simpleTemplate;
  private String complexTemplate;
  private Map<String, Object> simpleBindings;
  private Map<String, Object> complexBindings;

  @Setup
  public void setup() throws IOException {
    jinjava = new Jinjava();

    simpleTemplate =
      Resources.toString(
        Resources.getResource("benchmarks/simple.jinja"),
        StandardCharsets.UTF_8
      );
    complexTemplate =
      Resources.toString(
        Resources.getResource("benchmarks/complex.jinja"),
        StandardCharsets.UTF_8
      );

    List<List<String>> table = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      List<String> row = new ArrayList<>();
      for (int j = 0; j < 5; j++) {
        row.add("cell_" + i + "_" + j);
      }
      table.add(row);
    }

    simpleBindings =
      ImmutableMap.of(
        "page_title",
        "Jinjava Benchmark",
        "navigation",
        ImmutableList.of(
          ImmutableMap.of("href", "index.html", "caption", "Index"),
          ImmutableMap.of("href", "downloads.html", "caption", "Downloads"),
          ImmutableMap.of("href", "products.html", "caption", "Products")
        ),
        "table",
        table
      );

    List<Map<String, Object>> users = ImmutableList.of(
      ImmutableMap.of("href", "/user/john", "username", "John Doe"),
      ImmutableMap.of("href", "/user/jane", "username", "Jane Doe"),
      ImmutableMap.of("href", "/user/peter", "username", "Peter Somewhat")
    );

    List<Map<String, Object>> articles = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      articles.add(
        ImmutableMap
          .<String, Object>builder()
          .put("href", "/article/" + i)
          .put("title", "Article Title Number " + i)
          .put("user", users.get(i % users.size()))
          .put(
            "body",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. ".repeat(5)
          )
          .put("pub_date", "2024-01-" + String.format("%02d", (i % 28) + 1))
          .put(
            "tags",
            ImmutableList.of("tag" + (i % 3), "tag" + (i % 5), "tag" + (i % 7))
          )
          .build()
      );
    }

    complexBindings =
      ImmutableMap.of(
        "page_title",
        "Jinjava Complex Benchmark",
        "navigation",
        ImmutableList.of(
          ImmutableMap.of("href", "index.html", "caption", "Index"),
          ImmutableMap.of("href", "about.html", "caption", "About"),
          ImmutableMap.of("href", "contact.html", "caption", "Contact")
        ),
        "users",
        users,
        "articles",
        articles
      );
  }

  @Benchmark
  public String simpleTemplateBenchmark() {
    return jinjava.render(simpleTemplate, simpleBindings);
  }

  @Benchmark
  public String complexTemplateBenchmark() {
    return jinjava.render(complexTemplate, complexBindings);
  }

  @Benchmark
  public String simpleExpressionBenchmark() {
    return jinjava.render("Hello {{ name }}!", ImmutableMap.of("name", "World"));
  }

  @Benchmark
  public String filterChainBenchmark() {
    return jinjava.render(
      "{{ value|capitalize|replace('foo', 'bar')|trim }}",
      ImmutableMap.of("value", "  foo hello world  ")
    );
  }

  @Benchmark
  public Jinjava jinjavaCreationBenchmark() {
    return new Jinjava();
  }

  @Benchmark
  public Jinjava jinjavaWithConfigCreationBenchmark() {
    return new Jinjava(
      JinjavaConfig.newBuilder().withMaxRenderDepth(20).withTrimBlocks(true).build()
    );
  }

  public static void main(String[] args) throws RunnerException {
    Options options = new OptionsBuilder()
      .include(JinjavaBenchmark.class.getSimpleName())
      .build();
    new Runner(options).run();
  }
}
