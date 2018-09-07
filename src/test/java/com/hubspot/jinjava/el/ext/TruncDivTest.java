package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.FatalTemplateErrorsException;

public class TruncDivTest {

    private Jinjava jinja;

    @Before
    public void setUp() {
        jinja = new Jinjava();
    }

    /**
     * Test the truncated division operator "//" with integer values
     */
    @Test
    public void testTruncDivInteger() {
        Map<String, Object> context = Maps.newHashMap();
        context.put("dividend", 5);
        context.put("divisor",  2);
        context.put("negativeDividend", -5);
        context.put("negativeDivisor", -2);

        String[][] testCases = {
            { "{% set x = dividend // divisor %}{{x}}", "2" },
            { "{% set x = 5 // 2 %}{{x}}", "2" },
            { "{% set x = dividend // 2 %}{{x}}", "2" },
            { "{% set x = 5 // divisor %}{{x}}", "2" },
            { "{% set x = negativeDividend // divisor %}{{x}}", "-3" },
            { "{% set x = -5 // 2 %}{{x}}", "-3" },
            { "{% set x = dividend // negativeDivisor %}{{x}}", "-3" },
            { "{% set x = 5 // -2 %}{{x}}", "-3" },
            { "{% set x = negativeDividend // negativeDivisor %}{{x}}", "2" },
            { "{% set x = -5 // -2 %}{{x}}", "2" }
        };

        for (String[] testCase : testCases) {
            String template = testCase[0];
            String expected = testCase[1];
            String rendered = jinja.render(template, context);
            assertEquals(expected, rendered);
        }
    }

    /**
     * Test the truncated division operator "//" with fractional values
     */
    @Test
    public void testTruncDivFractional() {
        Map<String, Object> context = Maps.newHashMap();
        context.put("dividend", 5.0);
        context.put("divisor",  2);
        context.put("negativeDividend", -5.0);
        context.put("negativeDivisor", -2);

        String[][] testCases = {
            { "{% set x = dividend // divisor %}{{x}}", "2.0" },
            { "{% set x = 5.0 // 2 %}{{x}}", "2.0" },
            { "{% set x = dividend // 2 %}{{x}}", "2.0" },
            { "{% set x = 5.0 // divisor %}{{x}}", "2.0" },
            { "{% set x = negativeDividend // divisor %}{{x}}", "-3.0" },
            { "{% set x = -5.0 // 2 %}{{x}}", "-3.0" },
            { "{% set x = dividend // negativeDivisor %}{{x}}", "-3.0" },
            { "{% set x = 5.0 // -2 %}{{x}}", "-3.0" },
            { "{% set x = negativeDividend // negativeDivisor %}{{x}}", "2.0" },
            { "{% set x = -5.0 // -2 %}{{x}}", "2.0"}
        };

        for (String[] testCase : testCases) {
            String template = testCase[0];
            String expected = testCase[1];
            String rendered = jinja.render(template, context);
            assertEquals(expected, rendered);
        }
    }

    /**
     * Test the truncated division operator "//" with strings
     */
    @Test
    public void testTruncDivStringFails() {
        Map<String, Object> context = Maps.newHashMap();
        context.put("dividend", "5");
        context.put("divisor",  "2");

        String template = "{% set x = dividend // divisor %}";
        try {
            jinja.render(template, context);
        } catch (FatalTemplateErrorsException e) {
            assertThat(e.getMessage()).contains("Unsupported operand type(s) for //: '5' (String) and '2' (String)");
        }
    }
}
