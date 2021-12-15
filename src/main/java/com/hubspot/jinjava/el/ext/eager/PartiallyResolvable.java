package com.hubspot.jinjava.el.ext.eager;

import com.hubspot.jinjava.el.ext.DeferredParsingException;
import de.odysseus.el.tree.Bindings;
import javax.el.ELContext;

/**
 * Interface for those Eager AstNode classes that in case a modifying
 * method is run on them and needs to be deferred, then the original identifier
 * can be preserved.
 */
public interface PartiallyResolvable {
  String getPartiallyResolved(
    Bindings bindings,
    ELContext context,
    DeferredParsingException deferredParsingException,
    boolean preserveIdentifier
  );
}
