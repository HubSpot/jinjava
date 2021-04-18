package com.hubspot.jinjava.lib.filter;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.BaseInterpretingTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AddFilterTest extends BaseInterpretingTest {
    private Map<String, Object> bindings;

    @Before
    public void setup() {
        jinjava.getGlobalContext().registerClasses(AddFilter.class);

        bindings = ImmutableMap.of(
                "num1", 12,
                "num2", 48
        );
    }

    @Test
    public void itAddsNumber() {
        assertThat(jinjava.render("{{ 5|add(13) }}", bindings)).isEqualTo("18");
        assertThat(jinjava.render("{{ num1|add(4) }}", bindings)).isEqualTo("16");
        assertThat(jinjava.render("{{ 7|add(num2) }}", bindings)).isEqualTo("55");
        assertThat(jinjava.render("{{ num1|add(num2) }}", bindings)).isEqualTo("60");
    }
}
