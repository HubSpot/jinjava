package com.hubspot.jinjava.objects.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.TestStringListGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.ListFeature;

@RunWith(AllTests.class)
public class LazyListTest {

  public static junit.framework.TestSuite suite() {
    return ListTestSuiteBuilder.using(new TestStringListGenerator() {
      @Override
      protected List<String> create(String[] elements) {
        // Fill here your collection with the given elements
        return new LazyList<>(new ArrayList<>(Arrays.asList(elements)).iterator());
      }
    }).withFeatures(ListFeature.GENERAL_PURPOSE,
        CollectionFeature.ALLOWS_NULL_VALUES,
        CollectionSize.ANY)
        .named("Lazy List test")
        .createTestSuite();
  }
}
