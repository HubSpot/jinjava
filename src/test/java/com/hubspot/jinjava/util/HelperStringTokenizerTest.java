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
package com.hubspot.jinjava.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HelperStringTokenizerTest {

  private HelperStringTokenizer tk;

  @Test
  public void test1() {
    tk = new HelperStringTokenizer("111,222 333 '444', 		555");
    tk.next();
    tk.next();
    assertEquals("'444',", tk.next());
  }

  @Test
  public void test2() {
    tk = new HelperStringTokenizer("111,222 333 '444', 		555");
    tk.next();
    tk.next();
    tk.next();
    assertEquals("555", tk.next());
  }

  @Test
  public void test3() {
    tk = new HelperStringTokenizer("111,222 333 '444', 		555");
    tk.splitComma(true);
    tk.next();
    tk.next();
    tk.next();
    assertEquals("'444'", tk.next());
  }

  @Test
  public void test4() {
    tk = new HelperStringTokenizer("111,222 333 '444', 		555");
    tk.splitComma(true);
    tk.next();
    tk.next();
    tk.next();
    tk.next();
    assertEquals("555", tk.next());
  }

  @Test
  public void test5() {
    tk = new HelperStringTokenizer("111,', \"' \"222\"' 	;, ' 333 '444', 		555");
    tk.splitComma(true);
    tk.next();
    assertEquals("', \"'", tk.next());
  }

  @Test
  public void test6() {
    tk = new HelperStringTokenizer("111,', \"' \"222\"' 	;, ' 333 '444', 		555");
    tk.splitComma(true);
    tk.next();
    tk.next();
    tk.next();
    assertEquals("333", tk.next());
  }

  @Test
  public void test7() {
    tk = new HelperStringTokenizer("111,', \"' \"222\"' 	;, ' 333 '444', 		555");
    tk.splitComma(true);
    tk.next();
    tk.next();
    assertEquals("\"222\"' 	;, '", tk.next());
  }

  @Test
  public void test8() {
    tk = new HelperStringTokenizer("111 ', \"' \"222\"' 	;, ' 333 post.id|add:'444',\"555\",666 		555");
    tk.next();
    tk.next();
    tk.next();
    tk.next();
    assertEquals("post.id|add:'444',\"555\",666", tk.next());
  }

  @Test
  public void itDoesntReturnTrailingNull() {
    assertThat(new HelperStringTokenizer("product in collections.frontpage.products   ").splitComma(true).allTokens())
        .containsExactly("product", "in", "collections.frontpage.products")
        .doesNotContainNull();
  }

}
