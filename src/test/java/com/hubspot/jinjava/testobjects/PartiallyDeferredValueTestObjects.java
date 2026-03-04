package com.hubspot.jinjava.testobjects;

import com.hubspot.jinjava.interpret.DeferredValueException;
import com.hubspot.jinjava.interpret.PartiallyDeferredValue;
import com.hubspot.jinjava.objects.collections.PyMap;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.annotation.CheckForNull;

public class PartiallyDeferredValueTestObjects {

  public static class BadSerialization implements PartiallyDeferredValue {

    public String getDeferred() {
      throw new DeferredValueException("foo.deferred is deferred");
    }

    public String getResolved() {
      return "resolved";
    }

    @Override
    public Object getOriginalValue() {
      return null;
    }
  }

  public static class BadEntrySet extends PyMap implements PartiallyDeferredValue {

    public BadEntrySet(Map<String, Object> map) {
      super(map);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
      throw new DeferredValueException("entries are deferred");
    }

    @CheckForNull
    @Override
    public Object get(@CheckForNull Object key) {
      if ("deferred".equals(key)) {
        throw new DeferredValueException("deferred key");
      }
      return super.get(key);
    }

    @Override
    public Object getOriginalValue() {
      return null;
    }
  }

  public static class BadPyishSerializable
    implements PartiallyDeferredValue, PyishSerializable {

    public String getDeferred() {
      throw new DeferredValueException("foo.deferred is deferred");
    }

    public String getResolved() {
      return "resolved";
    }

    @Override
    public Object getOriginalValue() {
      return null;
    }

    @Override
    public <T extends Appendable & CharSequence> T appendPyishString(T appendable)
      throws IOException {
      throw new DeferredValueException("I'm bad");
    }
  }

  public static class GoodPyishSerializable
    implements PartiallyDeferredValue, PyishSerializable {

    public String getDeferred() {
      throw new DeferredValueException("foo.deferred is deferred");
    }

    public String getResolved() {
      return "resolved";
    }

    @Override
    public Object getOriginalValue() {
      return null;
    }

    @Override
    public <T extends Appendable & CharSequence> T appendPyishString(T appendable)
      throws IOException {
      return (T) appendable.append("good");
    }
  }

  public static class BadEntrySetButPyishSerializable
    extends PyMap
    implements PartiallyDeferredValue, PyishSerializable {

    public BadEntrySetButPyishSerializable(Map<String, Object> map) {
      super(map);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
      throw new DeferredValueException("entries are deferred");
    }

    @CheckForNull
    @Override
    public Object get(@CheckForNull Object key) {
      if ("deferred".equals(key)) {
        throw new DeferredValueException("deferred key");
      }
      return super.get(key);
    }

    @Override
    public Object getOriginalValue() {
      return null;
    }

    @Override
    public <T extends Appendable & CharSequence> T appendPyishString(T appendable)
      throws IOException {
      return (T) appendable.append("hello");
    }
  }
}
