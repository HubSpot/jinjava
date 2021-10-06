package com.hubspot.jinjava.lib.filter;

import com.google.common.base.Joiner;
import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.interpret.InvalidReason;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.Importable;
import com.hubspot.jinjava.objects.serialization.PyishObjectMapper;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.Charsets;

@JinjavaDoc(
  value = "Encode the string input into base 64.",
  input = @JinjavaParam(
    value = "input",
    type = "object",
    desc = "The string input to encode into base 64.",
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
      desc = "Encode a value with UTF-8 encoding into a Base 64 ASCII string",
      code = "{{ 'abcd'|b64encode }}"
    ),
    @JinjavaSnippet(
      desc = "Encode a value with UTF-16 Little Endian encoding into a Base 64 ASCII string",
      code = "{{ '\uD801\uDC37'|b64encode(encoding='utf-16le') }}"
    )
  }
)
public class Base64EncodeFilter implements Filter {
  public static final String NAME = "b64encode";
  public static final String AVAILABLE_CHARSETS = Joiner
    .on(", ")
    .join(
      StandardCharsets.US_ASCII.name(),
      StandardCharsets.ISO_8859_1.name(),
      StandardCharsets.UTF_8.name(),
      StandardCharsets.UTF_16BE.name(),
      StandardCharsets.UTF_16LE.name(),
      StandardCharsets.UTF_16.name()
    );

  static Charset checkCharset(
    JinjavaInterpreter interpreter,
    Importable filter,
    String... args
  ) {
    Charset charset = StandardCharsets.UTF_8;
    if (args.length > 0) {
      try {
        charset = Charsets.toCharset(StringUtils.upperCase(args[0]));
      } catch (UnsupportedCharsetException e) {
        throw new InvalidArgumentException(
          interpreter,
          filter,
          InvalidReason.ENUM,
          1,
          args[0],
          AVAILABLE_CHARSETS
        );
      }
    }
    return charset;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
    Charset charset = Base64EncodeFilter.checkCharset(interpreter, this, args);
    byte[] bytes;
    if (var instanceof byte[]) {
      bytes = (byte[]) var;
    } else {
      bytes = PyishObjectMapper.getAsUnquotedPyishString(var).getBytes(charset);
    }
    return Base64.getEncoder().encodeToString(bytes);
  }
}
