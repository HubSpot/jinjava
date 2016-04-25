package com.hubspot.jinjava.interpret;

import org.omg.CORBA.UNKNOWN;

public enum TemplateErrorCategory {
  IMPORT_CYCLE_DETECTED,
  INVALID_CONVERT_RGB_COLOR,
  INVALID_COLOR_VARIANT_RGB_COLOR,
  MISSING_RESOURCE,
  MISSING_TEMPLATE,
  UNKNOWN
}
