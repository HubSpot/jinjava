package com.hubspot.jinjava.el;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

public class ExpressionResolverPerformanceTest {

  public static void main(String[] args) {
    PerformanceTester tester = new PerformanceTester();
    tester.run();
  }

  public static class PerformanceTester {

    private JinjavaInterpreter interpreter;
    private Context context;
    private long startTime;

    public PerformanceTester() {

      interpreter = new Jinjava().newInterpreter();
      context = interpreter.getContext();
      context.put("customMap", new CustomMap());
      context.put("customObject", new CustomObject());
    }

    public void run() {

      int iterations = 1000000;

      testMapResolver(iterations);
      testMethodResolver(iterations);
    }

    public void startTimer() {
      startTime = System.currentTimeMillis();
    }

    public void stopTimer() {
      System.out.println(String.format("%d msec", System.currentTimeMillis() - startTime));
    }

    public void testMapResolver(int iterations) {
      System.out.println("map resolver with " + iterations + " iterations");
      startTimer();

      for (int i = 0; i < iterations; i++) {
        Object val = interpreter.resolveELExpression("customMap.get(\"thing\")", -1);
        assertThat(val).isEqualTo("hey");
      }

      stopTimer();
    }

    public void testMethodResolver(int iterations) {
      System.out.println("method resolver with " + iterations + " iterations");
      startTimer();

      for (int i = 0; i < iterations; i++) {
        Object val = interpreter.resolveELExpression("customObject.getThing()", -1);
        assertThat(val).isEqualTo("hey");
      }

      stopTimer();
    }

  }

  static class CustomObject {

    public CustomObject() {
    }

    public String getThing() {
      return "hey";
    }

  }

  static class CustomMap implements Map<String, String> {

    @Override
    public String get(Object key) {
      return "hey";
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public boolean containsKey(Object key) {
      return false;
    }

    @Override
    public boolean containsValue(Object value) {
      return false;
    }

    @Override
    public String put(String key, String value) {
      return null;
    }

    @Override
    public String remove(Object key) {
      return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<String> keySet() {
      return null;
    }

    @Override
    public Collection<String> values() {
      return null;
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
      return null;
    }
  }

}
