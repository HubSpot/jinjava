package com.hubspot.jinjava.lib.exptest;

import java.util.Set;

import com.hubspot.jinjava.lib.SimpleLibrary;

public class ExpTestLibrary extends SimpleLibrary<ExpTest> {

  public ExpTestLibrary(boolean registerDefaults, Set<String> disabled) {
    super(registerDefaults, disabled);
  }

  @Override
  protected void registerDefaults() {
    registerClasses(
        IsDefinedExpTest.class,
        IsDivisibleByExpTest.class,
        IsEqualToExpTest.class,
        IsEvenExpTest.class,
        IsIterableExpTest.class,
        IsLowerExpTest.class,
        IsMappingExpTest.class,
        IsNoneExpTest.class,
        IsNumberExpTest.class,
        IsOddExpTest.class,
        IsSameAsExpTest.class,
        IsSequenceExpTest.class,
        IsStringExpTest.class,
        IsStringContainingExpTest.class,
        IsStringStartingWithExpTest.class,
        IsTruthyExpTest.class,
        IsUndefinedExpTest.class,
        IsUpperExpTest.class,
        IsContainingAllExpTest.class,
        IsContainingExpTest.class,
        IsWithinExpTest.class);
  }

  public ExpTest getExpTest(String name) {
    return fetch(name);
  }

  public void addExpTest(ExpTest expTest) {
    register(expTest);
  }

}
