package com.hubspot.jinjava.testobjects;

import com.hubspot.jinjava.interpret.OutputTooBigException;
import com.hubspot.jinjava.objects.collections.PyList;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import java.io.IOException;
import java.util.List;

public class EagerTagDecoratorTestObjects {

  public static class TooBig extends PyList implements PyishSerializable {

    public TooBig(List<Object> list) {
      super(list);
    }

    @Override
    public <T extends Appendable & CharSequence> T appendPyishString(T appendable)
      throws IOException {
      throw new OutputTooBigException(1, 1);
    }
  }
}
