package com.hubspot.jinjava.objects;


import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GlobalVariablesTest {
    private GlobalVariables globalVariables;

    @Before
    public void setup(){
        globalVariables = new GlobalVariables();
    }

    @Test
    public void shouldReturnEmptyStringIfValueDoNotExists() {
        // given
        String key = "key";

        // when
        final Object result = globalVariables.getVariableFor(key);

        // then
        assertThat(result).isEqualTo("");
    }

    @Test
    public void shouldReplaceValueForKeyIfValueForKeyExists() {
        // given
        String key = "key";
        globalVariables.setVariable(key, Boolean.TRUE);
        globalVariables.setVariable(key, "second value");

        // when
        final Object result = globalVariables.getVariableFor(key);

        // then
        assertThat(result).isEqualTo("second value");
    }

    @Test
    public void shouldSetValueIfValueDoNotExists() {
        // given
        String key = "key";
        final String value = "Test";

        // when
        globalVariables.setVariable(key, value);

        // then
        assertThat(globalVariables.getVariableFor(key)).isEqualTo(value);
    }
}
