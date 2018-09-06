package com.hubspot.jinjava.el.ext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.FatalTemplateErrorsException;

public class PowerOfTest {

    @Before
    public void setUp() {
        jinja = new Jinjava();
    }

    @Test
    public void testPowerOfInteger() {
        Map<String, Object> context = Maps.newHashMap();
        context.put("base",     2);
        context.put("evenExponent", 8);
        context.put("oddExponent", 7);
        context.put("negativeBase", -2);
        context.put("negativeExponent", -8);

        String[][] testCases = {
            { "{% set x = base ** evenExponent %}{{x}}", "256" },
            { "{% set x = 2 ** 8 %}{{x}}", "256" },
            { "{% set x = base ** 8 %}{{x}}", "256" },
            { "{% set x = 2 ** evenExponent %}{{x}}", "256" },
            { "{% set x = negativeBase ** evenExponent %}{{x}}", "256" },
            { "{% set x = -2 ** 8 %}{{x}}", "256" },
            { "{% set x = base ** negativeExponent %}{{x}}", "0" },
            { "{% set x = 2 ** -8 %}{{x}}", "0" },
            { "{% set x = negativeBase ** oddExponent %}{{x}}", "-128"},
            { "{% set x = -2 ** 7 %}{{x}}", "-128" }
        };

        for (String[] testCase : testCases ) {
            String template = testCase[0];
            String expected = testCase[1];
            String rendered = jinja.render(template, context);
            assertEquals(expected, rendered);
        }
    }

    @Test
    public void testPowerOfFractional() {
        Map<String, Object> context = Maps.newHashMap();
        context.put("base",     2);
        context.put("evenExponent", 8.0);
        context.put("oddExponent", 7.0);
        context.put("negativeBase", -2);
        context.put("negativeExponent", -8.0);

        String[][] testCases = {
            { "{% set x = base ** evenExponent %}{{x}}", "256.0" },
            { "{% set x = 2 ** 8.0 %}{{x}}", "256.0" },
            { "{% set x = base ** 8.0 %}{{x}}", "256.0" },
            { "{% set x = 2 ** evenExponent %}{{x}}", "256.0" },
            { "{% set x = negativeBase ** evenExponent %}{{x}}", "256.0" },
            { "{% set x = -2 ** 8.0 %}{{x}}", "256.0" },
            { "{% set x = base ** negativeExponent %}{{x}}", "0.00390625" },
            { "{% set x = 2 ** -8.0 %}{{x}}", "0.00390625" },
            { "{% set x = negativeBase ** oddExponent %}{{x}}", "-128.0"},
            { "{% set x = -2 ** 7.0 %}{{x}}", "-128.0" }
        };

        for (String[] testCase : testCases ) {
            String template = testCase[0];
            String expected = testCase[1];
            String rendered = jinja.render(template, context);
            assertEquals(expected, rendered);
        }
    }

    @Test
    public void testPowerOfStringFails() {
        Map<String, Object> context = Maps.newHashMap();
        context.put("base",     "2");
        context.put("exponent", "8");

        String template = "{% set x = base ** exponent %}{{x}}";
        try {
            jinja.render(template, context);
        } catch (FatalTemplateErrorsException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("Unsupported operand type(s)"));
        }
    }

    private Jinjava jinja;
}
