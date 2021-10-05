package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@JinjavaDoc(
  value = "Decode a base 64 input into a string.",
  input = @JinjavaParam(
    value = "input",
    type = "string",
    desc = "The base 64 input to decode.",
    required = true
  ),
  params = {
    @JinjavaParam(
      value = "encoding",
      type = "string",
      desc = "The string encoding charset to use.",
      defaultValue = "UTF-8"
    )
  },
  snippets = {
    @JinjavaSnippet(
      desc = "Decode a Base 64-encoded ASCII string into a UTF-8 string",
      code = "{{ 'eydmb28nOiBbJ2JhciddfQ=='|b64decode }}"
    ),
    @JinjavaSnippet(
      desc = "Decode a Base 64-encoded ASCII string into a UTF-16 Little Endian string",
      code = "{{ 'Adg33A=='|b64decode(encoding='utf-16le') }}"
    )
  }
)
public class Base64DecodeFilter implements Filter {
  public static final String NAME = "b64decode";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    if (!(var instanceof String)) {
      throw new InvalidArgumentException(interpreter, this, InvalidReason.STRING, 0, var);
    }
    Charset charset = Base64EncodeFilter.checkCharset(interpreter, this, args);
    return new String(
      Base64.getDecoder().decode((var.toString()).getBytes(StandardCharsets.US_ASCII)),
      charset
    );
  }
}
