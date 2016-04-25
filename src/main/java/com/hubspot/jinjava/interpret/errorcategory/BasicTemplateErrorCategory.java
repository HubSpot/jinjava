package com.hubspot.jinjava.interpret.errorcategory;

public enum BasicTemplateErrorCategory implements TemplateErrorCategory {
  IMPORT_CYCLE_DETECTED,
  INCLUDE_CYCLE_DETECTED,
  UNKNOWN
}
