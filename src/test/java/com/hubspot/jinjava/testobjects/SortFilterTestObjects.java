package com.hubspot.jinjava.testobjects;

import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import java.io.IOException;
import java.util.Date;

public class SortFilterTestObjects {

  public static class MyFoo implements PyishSerializable {

    private Date date;

    public MyFoo(Date date) {
      this.date = date;
    }

    public Date getDate() {
      return date;
    }

    @Override
    public String toString() {
      return "" + date.getTime();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Appendable & CharSequence> T appendPyishString(T appendable)
      throws IOException {
      return (T) appendable.append(toString());
    }
  }

  public static class MyBar implements PyishSerializable {

    private MyFoo foo;

    public MyBar(MyFoo foo) {
      this.foo = foo;
    }

    public MyFoo getFoo() {
      return foo;
    }

    @Override
    public String toString() {
      return foo.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Appendable & CharSequence> T appendPyishString(T appendable)
      throws IOException {
      return (T) appendable.append(toString());
    }
  }
}
