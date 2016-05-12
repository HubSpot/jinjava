/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hubspot.jinjava.el.ext;

import com.hubspot.jinjava.Jinjava;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by anev on 11/05/16.
 */
public class RangeStringTest {

    Jinjava jinjava;

    @Before
    public void setup() {
        jinjava = new Jinjava();
    }

    @Test
    public void testStringRangeSimple() {
        Map<String, Object> context = new HashMap<>();
        context.put("theString", "theSimpleString");

        assertThat(jinjava.render("{{ theString[0:4] }}", context)).isEqualTo("theS");
    }

    @Test
    public void testStringRangeOutOfRange() {
        Map<String, Object> context = new HashMap<>();
        context.put("theString", "theSimpleString");

        assertThat(jinjava.render("{{ theString[0:400] }}", context)).isEqualTo("theSimpleString");
    }

    @Test
    public void testStringRangeOutNegative() {
        Map<String, Object> context = new HashMap<>();
        context.put("theString", "theSimpleString");

        assertThat(jinjava.render("{{ theString[-4:4] }}", context)).isEqualTo("theS");
    }

    @Test
    public void testStringRangeInvalidRange() {
        Map<String, Object> context = new HashMap<>();
        context.put("theString", "theSimpleString");

        assertThat(jinjava.render("{{ theString[4:2] }}", context)).isEmpty();
    }

    @Test
    public void testStringRangeMultiLine() {
        Map<String, Object> context = new HashMap<>();
        context.put("theString", "multi\nline\nstring");

        assertThat(jinjava.render("{{ theString[3:8] }}", context)).isEqualTo("ti\nli");
    }

    @Test
    public void testStringRangeCyrillic() {
        Map<String, Object> context = new HashMap<>();
        context.put("theString", "Строка с non ascii символами");

        assertThat(jinjava.render("{{ theString[1:4] }}", context)).isEqualTo("тро");
    }
}
