/**********************************************************************
Copyright (c) 2014 HubSpot Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 **********************************************************************/
package com.hubspot.jinjava.lib.filter;

import static com.hubspot.jinjava.util.Logging.ENGINE_LOG;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.hubspot.jinjava.doc.annotations.JinjavaDoc;
import com.hubspot.jinjava.doc.annotations.JinjavaParam;
import com.hubspot.jinjava.doc.annotations.JinjavaSnippet;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;

@JinjavaDoc(
    value = "Calculates the md5 hash of the given object",
    params = @JinjavaParam(value = "value", desc = "Value to get MD5 hash of"),
    snippets = {
        @JinjavaSnippet(code = "{{ content.absolute_url|md5 }}")
    })
public class Md5Filter implements Filter {

  private static final String[] NOSTR = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
  private static final String MD5 = "MD5";

  private String byteToArrayString(byte bByte) {
    int temp = bByte;
    if (temp < 0) {
      temp += 256;
    }
    int iD1 = temp / 16;
    int iD2 = temp % 16;
    return NOSTR[iD1] + NOSTR[iD2];
  }

  private String byteToString(byte[] bByte) {
    StringBuilder sBuffer = new StringBuilder();
    for (int i = 0; i < bByte.length; i++) {
      sBuffer.append(byteToArrayString(bByte[i]));
    }
    return sBuffer.toString();
  }

  private String md5(String str, Charset encoding) {
    String result = null;
    MessageDigest md;
    try {
      md = MessageDigest.getInstance(MD5);
      result = byteToString(md.digest(str.getBytes(encoding)));
    } catch (NoSuchAlgorithmException ex) {
      ENGINE_LOG.error(ex.getMessage());
    }

    return result;
  }

  @Override
  public Object filter(Object object, JinjavaInterpreter interpreter, String... arg) {
    if (object instanceof String) {
      return md5((String) object, interpreter.getConfig().getCharset());
    }
    return object;
  }

  @Override
  public String getName() {
    return "md5";
  }

}
