package com.hubspot.jinjava.interpret;

import com.google.common.annotations.Beta;

/**
 * An interface for a type of DeferredValue that as a whole is not deferred,
 * but certain attributes or methods within it are deferred.
 */
@Beta
public interface PartiallyDeferredValue extends DeferredValue {}
