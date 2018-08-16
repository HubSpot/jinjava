package com.hubspot.jinjava.interpret.errorcategory;

public enum BasicTemplateErrorCategory implements TemplateErrorCategory {
  CYCLE_DETECTED,
  IMPORT_CYCLE_DETECTED,
  INCLUDE_CYCLE_DETECTED,
  FROM_CYCLE_DETECTED,
  UNKNOWN,
  UNKNOWN_DATE,
  UNKNOWN_LOCALE,
  UNKNOWN_PROPERTY
}
