package com.hubspot.jinjava.interpret;

import java.util.regex.Pattern;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.common.base.Objects;

public class TemplateError {
  public enum ErrorType {
    FATAL, WARNING
  }

  public enum ErrorReason {
    SYNTAX_ERROR, UNKNOWN, BAD_URL, EXCEPTION, MISSING, OTHER
  }

  private final ErrorType severity;
  private final ErrorReason reason;
  private final String message;
  private final String fieldName;
  private final Integer lineno;

  private final Exception exception;

  public static TemplateError fromSyntaxError(InterpretException ex) {
    return new TemplateError(ErrorType.FATAL, ErrorReason.SYNTAX_ERROR, ExceptionUtils.getMessage(ex), null, ex.getLineNumber(), ex);
  }

  public static TemplateError fromException(TemplateSyntaxException ex) {
    return new TemplateError(ErrorType.FATAL, ErrorReason.SYNTAX_ERROR, ExceptionUtils.getMessage(ex), null, ex.getLineNumber(), ex);
  }

  public static TemplateError fromException(Exception ex) {
    int lineNumber = -1;

    if (ex instanceof InterpretException) {
      lineNumber = ((InterpretException) ex).getLineNumber();
    }

    return new TemplateError(ErrorType.FATAL, ErrorReason.EXCEPTION, ExceptionUtils.getMessage(ex), null, lineNumber, ex);
  }

  public static TemplateError fromException(Exception ex, int lineNumber) {
    return new TemplateError(ErrorType.FATAL, ErrorReason.EXCEPTION, ExceptionUtils.getMessage(ex), null, lineNumber, ex);
  }

  public static TemplateError fromUnknownProperty(Object base, String variable, int lineNumber) {
    return new TemplateError(ErrorType.WARNING, ErrorReason.UNKNOWN, String.format("Cannot resolve property '%s' in '%s'", variable, friendlyObjectToString(base)),
        variable, lineNumber, null);
  }

  private static String friendlyObjectToString(Object o) {
    if (o == null) {
      return "null";
    }

    String s = o.toString();

    if (!GENERIC_TOSTRING_PATTERN.matcher(s).find()) {
      return s;
    }

    Class<?> c = o.getClass();
    return c.getSimpleName();
  }

  // java.lang.Object@7852e922
  private static final Pattern GENERIC_TOSTRING_PATTERN = Pattern.compile("@[0-9a-z]{4,}$");

  public TemplateError(ErrorType severity, ErrorReason reason, String message,
      String fieldName, Integer lineno, Exception exception) {
    this.severity = severity;
    this.reason = reason;
    this.message = message;
    this.fieldName = fieldName;
    this.lineno = lineno;
    this.exception = exception;
  }

  public ErrorType getSeverity() {
    return severity;
  }

  public ErrorReason getReason() {
    return reason;
  }

  public String getMessage() {
    return message;
  }

  public String getFieldName() {
    return fieldName;
  }

  public Integer getLineno() {
    return lineno;
  }

  public Exception getException() {
    return exception;
  }

  public TemplateError serializable() {
    return new TemplateError(severity, reason, message, fieldName, lineno, null);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("severity", severity)
        .add("reason", reason)
        .add("message", message)
        .add("fieldName", fieldName)
        .add("lineno", lineno)
        .toString();
  }

}
