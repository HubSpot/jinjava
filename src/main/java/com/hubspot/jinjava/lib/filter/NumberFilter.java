package com.hubspot.jinjava.lib.filter;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError;
import com.hubspot.jinjava.interpret.TemplateError.ErrorItem;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

@JinjavaDoc(
  value = "Formats a given number based on the locale passed in as a parameter.",
  input = @JinjavaParam(
    value = "value",
    desc = "The number to be formatted based on locale",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = "locale",
      desc = "Locale in which to format the number. The default is the page's locale."
    ),
    @JinjavaParam(
      value = "decimal precision number",
      type = "number",
      desc = "A number input that determines the decimal precision of the formatted value. If the number of decimal digits from the input value is less than the decimal precision number, use the number of decimal digits from the input value. Otherwise, use the decimal precision number. The default is the number of decimal digits from the input value."
    )
  },
  snippets = {
    @JinjavaSnippet(code = "{{ number|format_number }}"),
    @JinjavaSnippet(code = "{{ number|format_number(\"en-US\") }}"),
    @JinjavaSnippet(code = "{{ number|format_number(\"en-US\", 3) }}")
  }
)
public class NumberFilter implements Filter {
  private static final String FORMAT_NUMBER_FILTER_NAME = "format_number";

  @Override
  public String getName() {
    return FORMAT_NUMBER_FILTER_NAME;
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    Locale locale = args.length > 0 && !Strings.isNullOrEmpty(args[0])
      ? Locale.forLanguageTag(args[0])
      : interpreter.getConfig().getLocale();

    BigDecimal number;
    try {
      number = parseInput(var);
    } catch (Exception e) {
      if (interpreter.getContext().isValidationMode()) {
        return "";
      }
      interpreter.addError(
        new TemplateError(
          ErrorType.WARNING,
          ErrorReason.INVALID_INPUT,
          ErrorItem.FILTER,
          "Input value '" + var + "' could not be parsed.",
          null,
          interpreter.getLineNumber(),
          e,
          null,
          ImmutableMap.of("value", Objects.toString(var))
        )
      );
      return var;
    }

    int noOfDecimalPlacesInInput = Math.max(0, number.scale());
    int decimalPrecisionNumber = args.length > 1
      ? Integer.parseInt(args[1])
      : noOfDecimalPlacesInInput;

    return formatNumber(locale, number, noOfDecimalPlacesInInput, decimalPrecisionNumber);
  }

  private BigDecimal parseInput(Object input) throws Exception {
    DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
    df.setParseBigDecimal(true);

    return (BigDecimal) df.parseObject(Objects.toString(input));
  }

  private String formatNumber(
    Locale locale,
    BigDecimal number,
    int noOfDecimalPlacesInInput,
    int decimalPrecisionNumber
  ) {
    NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);

    numberFormat.setMinimumFractionDigits(noOfDecimalPlacesInInput);
    numberFormat.setMaximumFractionDigits(
      Math.min(noOfDecimalPlacesInInput, decimalPrecisionNumber)
    );

    return numberFormat.format(number);
  }
}
