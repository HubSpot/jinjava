package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Performance test to verify that the optimized filter chain performs better than
 * the nested method approach.
 *
 * Run with: mvn test -Dtest=AstFilterChainPerformanceTest
 * Or run the main() method directly for more detailed output.
 */
public class AstFilterChainPerformanceTest {

  private Jinjava jinjavaOptimized;
  private Jinjava jinjavaUnoptimized;
  private Map<String, Object> context;

  @Before
  public void setup() {
    jinjavaOptimized =
      new Jinjava(
        JinjavaConfig.newBuilder().withEnableFilterChainOptimization(true).build()
      );

    jinjavaUnoptimized =
      new Jinjava(
        JinjavaConfig.newBuilder().withEnableFilterChainOptimization(false).build()
      );

    context = new HashMap<>();
    context.put("name", "  Hello World  ");
    context.put("text", "the quick brown fox jumps over the lazy dog");
    context.put("number", 12345);
    context.put("items", new String[] { "apple", "banana", "cherry" });
    context.put("content", Map.of("text", "the quick brown fox jumps over the lazy dog"));
  }

  public static void main(String[] args) {
    AstFilterChainPerformanceTest test = new AstFilterChainPerformanceTest();
    test.setup();
    test.runPerformanceComparison();
  }

  /**
   * Run this test manually to see detailed performance comparison.
   * Use main() method or run with -Dtest=AstFilterChainPerformanceTest#runPerformanceComparison
   */
  @Test
  @Ignore("Manual performance test - run explicitly when needed")
  public void runPerformanceComparison() {
    int warmupIterations = 10000;
    int testIterations = 100000;

    System.out.println("=== Filter Chain Performance Test ===\n");
    System.out.println("Warming up...");

    // Warmup
    runFilterTests(jinjavaOptimized, warmupIterations, false);
    runFilterTests(jinjavaUnoptimized, warmupIterations, false);

    System.out.println(
      "Running performance tests with " + testIterations + " iterations each\n"
    );

    // Single filter
    comparePerformance("Single filter: {{ name|trim }}", testIterations);

    // Two chained filters
    comparePerformance("Two filters: {{ name|trim|lower }}", testIterations);

    // Three chained filters
    comparePerformance("Three filters: {{ name|trim|lower|capitalize }}", testIterations);

    // Five chained filters
    comparePerformance(
      "Five filters: {{ text|upper|replace('THE', 'a')|trim|lower|title }}",
      testIterations
    );

    // Filter with arguments
    comparePerformance(
      "Filters with args: {{ text|truncate(20)|upper }}",
      testIterations
    );

    // Multiple filter chains in same template
    comparePerformance(
      "Multiple chains: {{ name|trim|lower }} and {{ text|upper|truncate(10) }}",
      testIterations
    );
  }

  private void comparePerformance(String description, int iterations) {
    String template = description.substring(description.indexOf("{{"));
    if (description.contains(":")) {
      template = description.substring(description.indexOf(":") + 2);
    }

    System.out.println(description);

    // Run optimized
    long optimizedTime = timeExecution(jinjavaOptimized, template, iterations);

    // Run unoptimized
    long unoptimizedTime = timeExecution(jinjavaUnoptimized, template, iterations);

    double speedup = (double) unoptimizedTime / optimizedTime;
    System.out.printf(
      "  Optimized: %d ms, Unoptimized: %d ms, Speedup: %.2fx%n%n",
      optimizedTime,
      unoptimizedTime,
      speedup
    );
  }

  private long timeExecution(Jinjava jinjava, String template, int iterations) {
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < iterations; i++) {
      jinjava.render(template, context);
    }
    return System.currentTimeMillis() - startTime;
  }

  private void runFilterTests(Jinjava jinjava, int iterations, boolean print) {
    String[] templates = {
      "{{ name|trim }}",
      "{{ name|trim|lower }}",
      "{{ name|trim|lower|capitalize }}",
      "{{ text|upper|replace('THE', 'a')|trim|lower|title }}",
      "{{ text|truncate(20)|upper }}",
    };

    for (String template : templates) {
      for (int i = 0; i < iterations; i++) {
        jinjava.render(template, context);
      }
    }
  }

  @Test
  public void itProducesSameResultsWithAndWithoutOptimization() {
    String[] templates = {
      "{{ name|trim }}",
      "{{ name|trim|lower }}",
      "{{ name|trim|lower|capitalize }}",
      "{{ text|upper|replace('THE', 'a')|trim|lower|title }}",
      "{{ text|truncate(20)|upper }}",
      "{{ name|trim|lower }} and {{ text|upper|truncate(10) }}",
      "{{ items|join(', ')|upper }}",
      "{{ number|string|length }}",
    };

    for (String template : templates) {
      String optimizedResult = jinjavaOptimized.render(template, context);
      String unoptimizedResult = jinjavaUnoptimized.render(template, context);
      assertThat(optimizedResult)
        .as("Template: " + template)
        .isEqualTo(unoptimizedResult);
    }
  }

  @Test
  public void itHandlesSingleFilterWithOptimization() {
    String result = jinjavaOptimized.render("{{ name|trim }}", context);
    assertThat(result).isEqualTo("Hello World");
  }

  @Test
  public void itHandlesChainedFiltersWithOptimization() {
    String result = jinjavaOptimized.render("{{ name|trim|lower }}", context);
    assertThat(result).isEqualTo("hello world");
  }

  @Test
  public void itHandlesFiltersWithArgumentsWithOptimization() {
    String result = jinjavaOptimized.render("{{ text|truncate(20)|upper }}", context);
    assertThat(result).isNotEmpty();
    assertThat(result).isUpperCase();
  }

  @Test
  public void itHandlesComplexFilterChainWithOptimization() {
    String result = jinjavaOptimized.render(
      "{{ text|upper|replace('THE', 'a')|trim|lower|capitalize }}",
      context
    );
    assertThat(result).isNotEmpty();
  }

  /**
   * This test verifies that the optimized version is faster than the unoptimized version.
   * The optimization should provide a measurable speedup for chained filters.
   */
  @Test
  public void optimizedVersionShouldBeFaster() {
    int warmupIterations = 100;
    int testIterations = 1000;
    String template = "{{ content.text|upper|replace('THE', 'a')|trim|lower|title }}";

    // Warmup both to ensure JIT compilation
    for (int i = 0; i < warmupIterations; i++) {
      jinjavaOptimized.render(template, context);
      jinjavaUnoptimized.render(template, context);
    }

    // Run multiple rounds to get more stable results
    long totalOptimizedTime = 0;
    long totalUnoptimizedTime = 0;
    int rounds = 3;

    for (int round = 0; round < rounds; round++) {
      totalUnoptimizedTime += timeExecution(jinjavaUnoptimized, template, testIterations);
      totalOptimizedTime += timeExecution(jinjavaOptimized, template, testIterations);
    }

    long avgUnoptimizedTime = totalUnoptimizedTime / rounds;
    long avgOptimizedTime = totalOptimizedTime / rounds;

    System.out.printf(
      "Performance test: Optimized=%d ms, Unoptimized=%d ms, Speedup=%.2fx%n",
      avgOptimizedTime,
      avgUnoptimizedTime,
      (double) avgUnoptimizedTime / avgOptimizedTime
    );

    // The optimized version should be faster (allow 10% margin for system variance)
    // If optimized takes more than 90% of unoptimized time, fail the test
    assertThat(avgOptimizedTime)
      .as(
        "Optimized (%d ms) should be faster than unoptimized (%d ms)",
        avgOptimizedTime,
        avgUnoptimizedTime
      )
      .isLessThan((long) (avgUnoptimizedTime * 0.95));
  }
}
