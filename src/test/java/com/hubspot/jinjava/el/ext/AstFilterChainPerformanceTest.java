package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Performance tests for the filter chain optimization.
 *
 * Run manually with: mvn test -Dtest=AstFilterChainPerformanceTest
 * Or run the main() method directly for detailed output.
 */
public class AstFilterChainPerformanceTest {

  private Jinjava jinjavaOptimized;
  private Jinjava jinjavaUnoptimized;
  private Map<String, Object> context;

  @Before
  public void setup() {
    jinjavaOptimized =
      new Jinjava(
        JinjavaConfig
          .newBuilder()
          .withMethodValidator(BaseJinjavaTest.METHOD_VALIDATOR)
          .withReturnTypeValidator(BaseJinjavaTest.RETURN_TYPE_VALIDATOR)
          .withEnableFilterChainOptimization(true)
          .build()
      );

    jinjavaUnoptimized =
      new Jinjava(
        JinjavaConfig
          .newBuilder()
          .withMethodValidator(BaseJinjavaTest.METHOD_VALIDATOR)
          .withReturnTypeValidator(BaseJinjavaTest.RETURN_TYPE_VALIDATOR)
          .withEnableFilterChainOptimization(false)
          .build()
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

    runFilterTests(jinjavaOptimized, warmupIterations);
    runFilterTests(jinjavaUnoptimized, warmupIterations);

    System.out.println(
      "Running performance tests with " + testIterations + " iterations each\n"
    );

    comparePerformance("Single filter: {{ name|trim }}", testIterations);
    comparePerformance("Two filters: {{ name|trim|lower }}", testIterations);
    comparePerformance("Three filters: {{ name|trim|lower|capitalize }}", testIterations);
    comparePerformance(
      "Five filters: {{ text|upper|replace('THE', 'a')|trim|lower|title }}",
      testIterations
    );
    comparePerformance(
      "Filters with args: {{ text|truncate(20)|upper }}",
      testIterations
    );
    comparePerformance(
      "Multiple chains: {{ name|trim|lower }} and {{ text|upper|truncate(10) }}",
      testIterations
    );
  }

  @Test
  public void optimizedVersionShouldBeFaster() {
    int warmupIterations = 100;
    int testIterations = 1000;
    String template = "{{ content.text|upper|replace('THE', 'a')|trim|lower|title }}";

    for (int i = 0; i < warmupIterations; i++) {
      jinjavaOptimized.render(template, context);
      jinjavaUnoptimized.render(template, context);
    }

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
      (1.0 * avgUnoptimizedTime) / avgOptimizedTime
    );

    assertThat(avgOptimizedTime)
      .as(
        "Optimized (%d ms) should be faster than unoptimized (%d ms)",
        avgOptimizedTime,
        avgUnoptimizedTime
      )
      .isLessThan((avgUnoptimizedTime * 95) / 100);
  }

  private void comparePerformance(String description, int iterations) {
    String template = description.substring(description.indexOf("{{"));
    if (description.contains(":")) {
      template = description.substring(description.indexOf(":") + 2);
    }

    System.out.println(description);

    long optimizedTime = timeExecution(jinjavaOptimized, template, iterations);
    long unoptimizedTime = timeExecution(jinjavaUnoptimized, template, iterations);

    double speedup = (1.0 * unoptimizedTime) / optimizedTime;
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

  private void runFilterTests(Jinjava jinjava, int iterations) {
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
}
