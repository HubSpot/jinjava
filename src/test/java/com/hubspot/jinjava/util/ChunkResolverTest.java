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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.DeferredValue;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.tree.parse.DefaultTokenScannerSymbols;
import com.hubspot.jinjava.tree.parse.TagToken;
import com.hubspot.jinjava.tree.parse.TokenScannerSymbols;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ChunkResolverTest {
  private static final TokenScannerSymbols SYMBOLS = new DefaultTokenScannerSymbols();

  private JinjavaInterpreter interpreter;
  private TagToken tagToken;
  private Context context;

  @Before
  public void setUp() {
    interpreter = new JinjavaInterpreter(new Jinjava().newInterpreter());
    context = interpreter.getContext();
    context.put("deferred", DeferredValue.instance());
    tagToken = new TagToken("{% foo %}", 1, 2, SYMBOLS);
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  private ChunkResolver makeChunkResolver(String string) {
    return new ChunkResolver(string, tagToken, interpreter).useMiniChunks(true);
  }

  @Test
  public void itResolvesDeferredBoolean() {
    context.put("foo", "foo_val");
    ChunkResolver chunkResolver = makeChunkResolver("(111 == 112) || (foo == deferred)");
    String partiallyResolved = chunkResolver.resolveChunks();
    assertThat(partiallyResolved).isEqualTo("false || ('foo_val' == deferred)");
    context.put("deferred", "foo_val");
    assertThat(makeChunkResolver(partiallyResolved).resolveChunks()).isEqualTo("true");
    assertThat(interpreter.resolveELExpression(partiallyResolved, 1)).isEqualTo(true);
  }

  @Test
  public void itResolvesDeferredList() {
    context.put("foo", "foo_val");
    context.put("bar", "bar_val");
    ChunkResolver chunkResolver = makeChunkResolver("[foo == bar, deferred, bar]");
    assertThat(chunkResolver.resolveChunks()).isEqualTo("[false,deferred,'bar_val']");
    assertThat(chunkResolver.getDeferredVariables()).containsExactly("deferred");
    context.put("bar", "foo_val");
    assertThat(chunkResolver.resolveChunks()).isEqualTo("[true,deferred,'foo_val']");
  }

  @Test
  public void itResolvesSimpleBoolean() {
    context.put("foo", true);
    ChunkResolver chunkResolver = makeChunkResolver("false || (foo), 'bar'");
    String partiallyResolved = chunkResolver.resolveChunks();
    assertThat(partiallyResolved).isEqualTo("true,'bar'");
    assertThat(chunkResolver.getDeferredVariables()).isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void itResolvesRange() {
    ChunkResolver chunkResolver = makeChunkResolver("range(0,2)");
    String partiallyResolved = chunkResolver.resolveChunks();
    assertThat(partiallyResolved).isEqualTo("[0,1]");
    assertThat(chunkResolver.getDeferredVariables()).isEmpty();
    assertThat((List<Long>) interpreter.resolveELExpression(partiallyResolved, 1))
      .contains(0L, 1L);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void itResolvesDictionary() {
    Map<String, Object> dict = ImmutableMap.of("foo", "one", "bar", 2L);
    context.put("the_dictionary", dict);

    ChunkResolver chunkResolver = makeChunkResolver("[the_dictionary, 1]");
    String partiallyResolved = chunkResolver.resolveChunks();
    assertThat(chunkResolver.getDeferredVariables()).isEmpty();
    assertThat(interpreter.resolveELExpression(partiallyResolved, 1))
      .isEqualTo(ImmutableList.of(dict, 1L));
  }
}
