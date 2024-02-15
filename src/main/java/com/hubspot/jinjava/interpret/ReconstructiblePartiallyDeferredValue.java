package com.hubspot.jinjava.interpret;

import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import java.io.IOException;

public interface ReconstructiblePartiallyDeferredValue
  extends PartiallyDeferredValue, PyishSerializable {
  /**
   * This method should not throw any RuntimeExceptions
   */
  @Override
  <T extends Appendable & CharSequence> T appendPyishString(T appendable)
    throws IOException;
}
