package com.hubspot.jinjava.interpret;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Foo {

    private final String bar;

    public Foo(String bar) {
        this.bar = bar;
    }

    public String getBar() {
        return bar;
    }

    public String getBarFoo() {
        return bar;
    }

    public String getBarFoo1() {
        return bar;
    }

    @JsonIgnore
    public String getBarHidden() {
        return bar;
    }

}
