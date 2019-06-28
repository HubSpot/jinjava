package com.hubspot.jinjava.lib.exptest;

import com.hubspot.jinjava.el.TruthyTypeConverter;
import com.hubspot.jinjava.el.ext.CollectionMembershipOperator;

public abstract class CollectionExpTest implements ExpTest {

  protected static final TruthyTypeConverter TYPE_CONVERTER = new TruthyTypeConverter();
  protected static final CollectionMembershipOperator COLLECTION_MEMBERSHIP_OPERATOR = new CollectionMembershipOperator();

}
