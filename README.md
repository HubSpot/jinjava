jinjava [![Build Status](https://travis-ci.org/HubSpot/jinjava.svg?branch=master)](https://travis-ci.org/HubSpot/jinjava) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.hubspot.jinjava/jinjava/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.hubspot.jinjava/jinjava)
=======

<img src="https://github.com/HubSpot/jinjava/raw/master/jinjava.png" width="250" height="250" alt="jinjava">

Java-based template engine based on django template syntax, adapted to render jinja templates (at least the subset of jinja in use in HubSpot content). Currently used in production to render thousands of websites with hundreds of millions of page views per month on the [HubSpot COS](http://www.hubspot.com/products/sites).
*Note*: Requires Java >= 8. Originally forked from [jangod](https://code.google.com/p/jangod/).

Get it:
-------

```xml
  <dependency>
    <groupId>com.hubspot.jinjava</groupId>
    <artifactId>jinjava</artifactId>
    <version>2.0.7</version>
  </dependency>
```

Example usage:
--------------

my-template.html:
```html
<div>Hello, {{ name }}!</div>
```

java code:
```java
Jinjava jinjava = new Jinjava();
Map<String, Object> context = Maps.newHashMap();
context.put("name", "Jared");

String template = Resources.toString(Resources.getResource("my-template.html"), Charsets.UTF_8);

String renderedTemplate = jinjava.render(template, context);
```

result:
```html
<div>Hello, Handsome!</div>
```

Voila! Hey, wait a minute...

Advanced Topics
---------------

### Template loading

Jinjava needs to know how to interpret template paths, so it can properly handle tags like:
```
{% extends "foo/bar/base.html" %}
```

By default, it will load a ```FileLocator```; you will likely want to provide your own implementation of 
```ResourceLoader``` to hook into your application's template repository, and then tell jinjava about it:

```java
JinjavaConfig config = new JinjavaConfig();
config.setResourceLocator(new MyCustomResourceLocator());

Jinjava jinjava = new Jinjava(config);
```

### Custom tags, filters and functions

You can provide custom jinja tags, filters, and static functions to the template engine.

```java
// define a custom tag implementing com.hubspot.jinjava.lib.Tag
jinjava.getTagLibrary().addTag(new MyCustomTag());
// define a custom filter implementing com.hubspot.jinjava.lib.Filter
jinjava.getFilterLibrary().addFilter(new MyAwesomeFilter());
// define a custom public static function (this one will bind to myfn.my_func('foo', 42))
jinjava.getFunctionLibrary().addFunction(new ELFunctionDefinition("myfn", "my_func", 
    MyFuncsClass.class, "myFunc", String.class, Integer.class);
```

### See also

 - [Javadocs](http://www.javadoc.io/doc/com.hubspot.jinjava/jinjava)
