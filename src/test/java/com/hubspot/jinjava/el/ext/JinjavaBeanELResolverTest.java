package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.el.JinjavaELContext;
import com.hubspot.jinjava.interpret.AutoCloseableSupplier;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.testobjects.JinjavaBeanELResolverTestObjects;
import java.beans.FeatureDescriptor;
import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import org.junit.Before;
import org.junit.Test;

public class JinjavaBeanELResolverTest {

  private JinjavaBeanELResolver jinjavaBeanELResolver;
  private ELContext elContext;
  private Jinjava jinjava;

  @Before
  public void setUp() throws Exception {
    jinjavaBeanELResolver = new JinjavaBeanELResolver();
    elContext = new JinjavaELContext();
    jinjava = new Jinjava(BaseJinjavaTest.newConfigBuilder().build());
  }

  @Test
  public void itReadsRecordComponentsAsReadOnlyProperties() {
    var person = new JinjavaBeanELResolverTestObjects.Person("Ada", 36);
    try (
      var a = JinjavaInterpreter.closeablePushCurrent(jinjava.newInterpreter()).get()
    ) {
      for (boolean readOnly : new boolean[] { false, true }) {
        var resolver = new JinjavaBeanELResolver(readOnly);
        elContext.setPropertyResolved(false);
        assertThat(resolver.getValue(elContext, person, "name")).isEqualTo("Ada");
        assertThat(elContext.isPropertyResolved()).isTrue();
        assertThat(resolver.getValue(elContext, person, "age")).isEqualTo(36);

        elContext.setPropertyResolved(false);
        assertThat(resolver.getType(elContext, person, "name")).isEqualTo(String.class);
        assertThat(elContext.isPropertyResolved()).isTrue();
        assertThat(resolver.getType(elContext, person, "age")).isEqualTo(int.class);

        elContext.setPropertyResolved(false);
        assertThat(resolver.isReadOnly(elContext, person, "name")).isTrue();
        assertThat(elContext.isPropertyResolved()).isTrue();
        assertThatThrownBy(() -> resolver.setValue(elContext, person, "name", "Grace"))
          .isInstanceOf(PropertyNotWritableException.class);
        assertThat(person.name()).isEqualTo("Ada");

        assertThatThrownBy(() -> resolver.getValue(elContext, person, "missing"))
          .isInstanceOf(PropertyNotFoundException.class);
        assertThatThrownBy(() -> resolver.getType(elContext, person, "missing"))
          .isInstanceOf(PropertyNotFoundException.class);
      }
    }
  }

  @Test
  public void itUsesCustomAndPrivateRecordAccessors() {
    var custom = new JinjavaBeanELResolverTestObjects.CustomAccessor("Ada");
    Object privatePerson = JinjavaBeanELResolverTestObjects.privatePerson("Grace");
    try (
      var a = JinjavaInterpreter.closeablePushCurrent(jinjava.newInterpreter()).get()
    ) {
      assertThat(jinjavaBeanELResolver.getValue(elContext, custom, "name"))
        .isEqualTo("accessor: Ada");
      assertThat(jinjavaBeanELResolver.getValue(elContext, privatePerson, "name"))
        .isEqualTo("Grace");
    }
  }

  @Test
  public void itDescribesRecordComponentsWithoutDuplicates() {
    var person = new JinjavaBeanELResolverTestObjects.Person("Ada", 36);
    List<FeatureDescriptor> descriptors = new ArrayList<>();
    jinjavaBeanELResolver
      .getFeatureDescriptors(elContext, person)
      .forEachRemaining(descriptors::add);

    assertThat(descriptors)
      .extracting(FeatureDescriptor::getName)
      .containsExactlyInAnyOrder("class", "name", "age");
    for (FeatureDescriptor descriptor : descriptors) {
      if (descriptor.getName().equals("name")) {
        assertThat(descriptor.getValue(ELResolver.TYPE)).isEqualTo(String.class);
      } else if (descriptor.getName().equals("age")) {
        assertThat(descriptor.getValue(ELResolver.TYPE)).isEqualTo(int.class);
      }
      assertThat(descriptor.getValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME))
        .isEqualTo(true);
    }
  }

  @Test
  public void itPreservesExistingBeanGettersOnRecords() throws Exception {
    var record = new JinjavaBeanELResolverTestObjects.BeanGetter("Ada");
    var original = Arrays
      .stream(Introspector.getBeanInfo(record.getClass()).getPropertyDescriptors())
      .filter(descriptor -> descriptor.getName().equals("name"))
      .findFirst()
      .orElseThrow();
    var originalReadMethod = original.getReadMethod();
    try (
      var a = JinjavaInterpreter.closeablePushCurrent(jinjava.newInterpreter()).get()
    ) {
      for (boolean readOnly : new boolean[] { false, true }) {
        var resolver = new JinjavaBeanELResolver(readOnly);
        assertThat(resolver.getValue(elContext, record, "name")).isEqualTo(3);
        assertThat(resolver.getType(elContext, record, "name")).isEqualTo(Integer.class);
        List<FeatureDescriptor> descriptors = new ArrayList<>();
        resolver
          .getFeatureDescriptors(elContext, record)
          .forEachRemaining(descriptors::add);
        assertThat(descriptors)
          .filteredOn(descriptor -> descriptor.getName().equals("name"))
          .extracting(descriptor -> descriptor.getValue(ELResolver.TYPE))
          .containsExactly(Integer.class);
      }
    }
    assertThat(original.getReadMethod()).isEqualTo(originalReadMethod);
    assertThat(original.getPropertyType()).isEqualTo(Integer.class);
  }

  @Test
  public void itPreservesExistingWriteOnlyPropertiesOnRecords() throws Exception {
    var writtenValue = new AtomicReference<String>();
    var record = new JinjavaBeanELResolverTestObjects.WriteOnlyProperty(
      "Ada",
      writtenValue
    );
    var original = Arrays
      .stream(Introspector.getBeanInfo(record.getClass()).getPropertyDescriptors())
      .filter(descriptor -> descriptor.getName().equals("name"))
      .findFirst()
      .orElseThrow();
    var originalWriteMethod = original.getWriteMethod();
    assertThat(original.getReadMethod()).isNull();
    try (
      var a = JinjavaInterpreter.closeablePushCurrent(jinjava.newInterpreter()).get()
    ) {
      for (boolean readOnly : new boolean[] { false, true }) {
        var resolver = new JinjavaBeanELResolver(readOnly);
        assertThatThrownBy(() -> resolver.getValue(elContext, record, "name"))
          .isInstanceOf(PropertyNotFoundException.class);
        assertThat(resolver.getType(elContext, record, "name")).isEqualTo(String.class);
        assertThat(resolver.isReadOnly(elContext, record, "name")).isEqualTo(readOnly);
        if (readOnly) {
          assertThatThrownBy(() -> resolver.setValue(elContext, record, "name", "no"))
            .isInstanceOf(PropertyNotWritableException.class);
        } else {
          resolver.setValue(elContext, record, "name", "Grace");
        }
        assertThat(writtenValue.get()).isEqualTo("Grace");
        assertThat(record.name()).isEqualTo("Ada");
      }
    }
    assertThat(original.getReadMethod()).isNull();
    assertThat(original.getWriteMethod()).isEqualTo(originalWriteMethod);
  }

  @Test
  public void itInvokesProperStringReplace() {
    try (
      var a = JinjavaInterpreter.closeablePushCurrent(jinjava.newInterpreter()).get()
    ) {
      assertThat(
        jinjavaBeanELResolver.invoke(
          elContext,
          "abcd",
          "replace",
          null,
          new Object[] { "abcd", "efgh" }
        )
      )
        .isEqualTo("efgh");
      assertThat(
        jinjavaBeanELResolver.invoke(
          elContext,
          "abcd",
          "replace",
          null,
          new Object[] { 'a', 'e' }
        )
      )
        .isEqualTo("ebcd");
    }
  }

  @Test
  public void itInvokesBestMethodWithSingleParam() {
    try (
      var a = JinjavaInterpreter.closeablePushCurrent(jinjava.newInterpreter()).get()
    ) {
      JinjavaBeanELResolverTestObjects.TempItInvokesBestMethodWithSingleParam var =
        new JinjavaBeanELResolverTestObjects.TempItInvokesBestMethodWithSingleParam();
      assertThat(
        jinjavaBeanELResolver.invoke(
          elContext,
          var,
          "getResult",
          null,
          new Object[] { 1 }
        )
      )
        .isEqualTo("int");
      assertThat(
        jinjavaBeanELResolver.invoke(
          elContext,
          var,
          "getResult",
          null,
          new Object[] { "1" }
        )
      )
        .isEqualTo("String");
      assertThat(
        jinjavaBeanELResolver.invoke(
          elContext,
          var,
          "getResult",
          null,
          new Object[] { new Object() }
        )
      )
        .isEqualTo("Object");
    }
  }

  @Test
  public void itPrefersPrimitives() {
    try (
      var a = JinjavaInterpreter.closeablePushCurrent(jinjava.newInterpreter()).get()
    ) {
      JinjavaBeanELResolverTestObjects.TempItPrefersPrimitives var =
        new JinjavaBeanELResolverTestObjects.TempItPrefersPrimitives();
      assertThat(
        jinjavaBeanELResolver.invoke(
          elContext,
          var,
          "getResult",
          null,
          new Object[] { 1, 2 }
        )
      )
        .isEqualTo("int Integer");
      assertThat(
        jinjavaBeanELResolver.invoke(
          elContext,
          var,
          "getResult",
          null,
          new Object[] { 1, Integer.valueOf(2) }
        )
      )
        .isEqualTo("int Integer"); // should be "int object", but we can't figure that out
      assertThat(
        jinjavaBeanELResolver.invoke(
          elContext,
          var,
          "getResult",
          null,
          new Object[] { Integer.valueOf(1), 2 }
        )
      )
        .isEqualTo("int Integer"); // should be "Number int", but we can't figure that out
    }
  }

  @Test
  public void itDoesNotAllowAccessingPropertiesOfInterpreter() {
    try (
      AutoCloseableSupplier.AutoCloseableImpl<JinjavaInterpreter> a = JinjavaInterpreter
        .closeablePushCurrent(jinjava.newInterpreter())
        .get()
    ) {
      assertThatThrownBy(() ->
          jinjavaBeanELResolver.getValue(elContext, a.value(), "config")
        )
        .isInstanceOf(PropertyNotFoundException.class);
    }
  }
}
