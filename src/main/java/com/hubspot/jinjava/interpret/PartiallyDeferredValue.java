package com.hubspot.jinjava.interpret;

/**
 * An interface for a type of DeferredValue that as a whole is not deferred,
 * but certain attributes or methods within it are deferred.
 */
public interface PartiallyDeferredValue extends DeferredValue {}
