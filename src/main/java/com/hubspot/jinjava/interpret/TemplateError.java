package com.hubspot.jinjava.interpret;

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.interpret.errorcategory.BasicTemplateErrorCategory;
import com.hubspot.jinjava.interpret.errorcategory.TemplateErrorCategory;

public class TemplateError {

  private static final Pattern GENERIC_TOSTRING_PATTERN = Pattern.compile("@[0-9a-z]{4,}$");
  private static final int MAX_STRING_LENGTH = 1024;

  public enum ErrorType {
    FATAL,
    WARNING
  }

  public enum ErrorReason {
    SYNTAX_ERROR,
    UNKNOWN,
    BAD_URL,
    EXCEPTION,
    MISSING,
    DISABLED,
    OTHER
  }

  public enum ErrorItem {
    TEMPLATE,
    TOKEN,
    TAG,
    FUNCTION,
    PROPERTY,
    FILTER,
    EXPRESSION_TEST,
    OTHER
  }

  private final ErrorType severity;
  private final ErrorReason reason;
  private final ErrorItem item;
  private final String message;
  private final String fieldName;
  private final int lineno;
  private final int startPosition;
  private final TemplateErrorCategory category;
  private final Map<String, String> categoryErrors;

  private int scopeDepth = 1;

  private final Exception exception;

  public TemplateError withScopeDepth(int scopeDepth) {
    return new TemplateError(getSeverity(), getReason(), getItem(), getMessage(), getFieldName(), getLineno(), getStartPosition(), getException(), getCategory(), getCategoryErrors(), scopeDepth);
  }

  public static TemplateError fromSyntaxError(InterpretException ex) {
    return new TemplateError(ErrorType.FATAL, ErrorReason.SYNTAX_ERROR, ErrorItem.OTHER, ExceptionUtils.getMessage(ex), null, ex.getLineNumber(), ex.getStartPosition(), ex);
  }

  public static TemplateError fromException(TemplateSyntaxException ex) {
    String fieldName = (ex instanceof UnknownTagException) ? ((UnknownTagException) ex).getTag() : ex.getCode();
    return new TemplateError(ErrorType.FATAL, ErrorReason.SYNTAX_ERROR, ErrorItem.OTHER, ExceptionUtils.getMessage(ex), fieldName, ex.getLineNumber(), ex.getStartPosition(), ex);
  }

  public static TemplateError fromException(Exception ex) {
    int lineNumber = -1;
    int startPosition = -1;

    if (ex instanceof InterpretException) {
      lineNumber = ((InterpretException) ex).getLineNumber();
      startPosition = ((InterpretException) ex).getStartPosition();
    }

    return new TemplateError(ErrorType.FATAL, ErrorReason.EXCEPTION, ErrorItem.OTHER, ExceptionUtils.getMessage(ex), null, lineNumber, startPosition, ex, BasicTemplateErrorCategory.UNKNOWN, ImmutableMap.of());
  }

  public static TemplateError fromException(Exception ex, int lineNumber, int startPosition) {
    return new TemplateError(ErrorType.FATAL, ErrorReason.EXCEPTION, ErrorItem.OTHER, ExceptionUtils.getMessage(ex), null, lineNumber, startPosition, ex);
  }

  public static TemplateError fromException(Exception ex, int lineNumber) {
    return new TemplateError(ErrorType.FATAL, ErrorReason.EXCEPTION, ErrorItem.OTHER, ExceptionUtils.getMessage(ex), null, lineNumber, -1, ex);
  }

  public static TemplateError fromUnknownProperty(Object base, String variable, int lineNumber) {
    return fromUnknownProperty(base, variable, lineNumber, -1);
  }

  public static TemplateError fromUnknownProperty(Object base, String variable, int lineNumber, int startPosition) {
    return new TemplateError(ErrorType.WARNING, ErrorReason.UNKNOWN, ErrorItem.PROPERTY,
        String.format("Cannot resolve property '%s' in '%s'", variable, friendlyObjectToString(base)),
        variable, lineNumber, startPosition,null, BasicTemplateErrorCategory.UNKNOWN_PROPERTY,
        ImmutableMap.of("property", variable, "lineNumber", String.valueOf(lineNumber), "startPosition", String.valueOf(startPosition)));
  }

  private static String friendlyObjectToString(Object o) {
    if (o == null) {
      return "null";
    }

    String s = o.toString();

    if (s.length() > MAX_STRING_LENGTH) {
      s = s.substring(0, MAX_STRING_LENGTH) + "...";
    }

    if (!GENERIC_TOSTRING_PATTERN.matcher(s).find()) {
      return s;
    }

    Class<?> c = o.getClass();
    return c.getSimpleName();
  }

  public TemplateError(ErrorType severity,
                       ErrorReason reason,
                       ErrorItem item,
                       String message,
                       String fieldName,
                       int lineno,
                       Exception exception) {
    this.severity = severity;
    this.reason = reason;
    this.item = item;
    this.message = message;
    this.fieldName = fieldName;
    this.lineno = lineno;
    this.startPosition = -1;
    this.exception = exception;
    this.category = BasicTemplateErrorCategory.UNKNOWN;
    this.categoryErrors = null;
  }

  public TemplateError(ErrorType severity,
                       ErrorReason reason,
                       ErrorItem item,
                       String message,
                       String fieldName,
                       int lineno,
                       int startPosition,
                       Exception exception) {
    this.severity = severity;
    this.reason = reason;
    this.item = item;
    this.message = message;
    this.fieldName = fieldName;
    this.lineno = lineno;
    this.startPosition = startPosition;
    this.exception = exception;
    this.category = BasicTemplateErrorCategory.UNKNOWN;
    this.categoryErrors = null;
  }


  public TemplateError(ErrorType severity,
                       ErrorReason reason,
                       ErrorItem item,
                       String message,
                       String fieldName,
                       int lineno,
                       int startPosition,
                       Exception exception,
                       TemplateErrorCategory category,
                       Map<String, String> categoryErrors,
                       int scopeDepth) {
    this.severity = severity;
    this.reason = reason;
    this.item = item;
    this.message = message;
    this.fieldName = fieldName;
    this.lineno = lineno;
    this.startPosition = startPosition;
    this.exception = exception;
    this.category = category;
    this.categoryErrors = categoryErrors;
    this.scopeDepth = scopeDepth;
  }


  public TemplateError(ErrorType severity,
                       ErrorReason reason,
                       ErrorItem item,
                       String message,
                       String fieldName,
                       int lineno,
                       Exception exception,
                       TemplateErrorCategory category,
                       Map<String, String> categoryErrors) {
    this.severity = severity;
    this.reason = reason;
    this.item = item;
    this.message = message;
    this.fieldName = fieldName;
    this.lineno = lineno;
    this.startPosition = -1;
    this.exception = exception;
    this.category = category;
    this.categoryErrors = categoryErrors;
  }

  public TemplateError(ErrorType severity,
                       ErrorReason reason,
                       ErrorItem item,
                       String message,
                       String fieldName,
                       int lineno,
                       int startPosition,
                       Exception exception,
                       TemplateErrorCategory category,
                       Map<String, String> categoryErrors) {
    this.severity = severity;
    this.reason = reason;
    this.item = item;
    this.message = message;
    this.fieldName = fieldName;
    this.lineno = lineno;
    this.startPosition = startPosition;
    this.exception = exception;
    this.category = category;
    this.categoryErrors = categoryErrors;
  }

  public TemplateError(ErrorType severity,
                       ErrorReason reason,
                       String message,
                       String fieldName,
                       int lineno,
                       int startPosition,
                       Exception exception) {
    this.severity = severity;
    this.reason = reason;
    this.item = ErrorItem.OTHER;
    this.message = message;
    this.fieldName = fieldName;
    this.lineno = lineno;
    this.startPosition = startPosition;
    this.exception = exception;
    this.category = BasicTemplateErrorCategory.UNKNOWN;
    this.categoryErrors = null;
  }

  public ErrorType getSeverity() {
    return severity;
  }

  public ErrorReason getReason() {
    return reason;
  }

  public ErrorItem getItem() {
    return item;
  }

  public String getMessage() {
    return message;
  }

  public String getFieldName() {
    return fieldName;
  }

  public int getLineno() {
    return lineno;
  }

  public int getStartPosition() {
    return startPosition;
  }

  public Exception getException() {
    return exception;
  }

  public TemplateErrorCategory getCategory() {
    return category;
  }

  public Map<String, String> getCategoryErrors() {
    return categoryErrors;
  }

  public int getScopeDepth() {
    return scopeDepth;
  }

  public TemplateError serializable() {
    return new TemplateError(severity, reason, item, message, fieldName, lineno, startPosition, null, category, categoryErrors, scopeDepth);
  }

  @Override
  public String toString() {
    return "TemplateError{" +
        "severity=" + severity +
        ", reason=" + reason +
        ", item=" + item +
        ", message='" + message + '\'' +
        ", fieldName='" + fieldName + '\'' +
        ", lineno=" + lineno +
        ", startPosition=" + startPosition +
        ", scopeDepth=" + scopeDepth +
        ", category=" + category +
        ", categoryErrors=" + categoryErrors +
        '}';
  }
}
