# PreserveUndefinedExecutionMode

A new execution mode for Jinjava that preserves unknown/undefined variables as their original template syntax instead of rendering them as empty strings. This enables multi-pass rendering scenarios where templates are processed in stages with different variable contexts available at each stage.

## Use Case

Multi-pass template rendering is useful when:
- Some variables are known at compile/build time (static values)
- Other variables are only known at runtime (dynamic values)
- You want to pre-render static parts while preserving dynamic placeholders

## Usage

```java
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.mode.PreserveUndefinedExecutionMode;

Jinjava jinjava = new Jinjava();
JinjavaConfig config = JinjavaConfig.newBuilder()
    .withExecutionMode(PreserveUndefinedExecutionMode.instance())
    .build();

Map<String, Object> context = new HashMap<>();
context.put("staticValue", "STATIC");

String template = "{{ staticValue }} - {{ dynamicValue }}";
String result = jinjava.render(template, context, config);
// Result: "STATIC - {{ dynamicValue }}"
```

## Behavior Summary

| Feature | Input | Context | Output |
|---------|-------|---------|--------|
| Undefined expression | `{{ unknown }}` | `{}` | `{{ unknown }}` |
| Defined expression | `{{ name }}` | `{name: "World"}` | `World` |
| Expression with filter | `{{ name \| upper }}` | `{}` | `{{ name \| upper }}` |
| Property access | `{{ obj.property }}` | `{}` | `{{ obj.property }}` |
| Null value | `{{ nullVar }}` | `{nullVar: null}` | `{{ nullVar }}` |
| Mixed | `Hello {{ name }}, {{ unknown }}!` | `{name: "World"}` | `Hello World, {{ unknown }}!` |

### Control Structures

| Feature | Input | Context | Output |
|---------|-------|---------|--------|
| If with known condition | `{% if true %}Hello{% endif %}` | `{}` | `Hello` |
| If with unknown condition | `{% if unknown %}Hello{% endif %}` | `{}` | `{% if unknown %}Hello{% endif %}` |
| If-else with unknown | `{% if unknown %}A{% else %}B{% endif %}` | `{}` | `{% if unknown %}A{% else %}B{% endif %}` |
| For with known iterable | `{% for x in items %}{{ x }}{% endfor %}` | `{items: ["a","b"]}` | `ab` |
| For with unknown iterable | `{% for x in items %}{{ x }}{% endfor %}` | `{}` | `{% for x in items %}{{ x }}{% endfor %}` |

### Set Tags

Set tags are preserved with their evaluated RHS values, enabling the variable to be set in subsequent rendering passes:

| Feature | Input | Context | Output |
|---------|-------|---------|--------|
| Set with known RHS | `{% set x = name %}{{ x }}` | `{name: "World"}` | `{% set x = 'World' %}World` |
| Set with unknown RHS | `{% set x = unknown %}{{ x }}` | `{}` | `{% set x = unknown %}{{ x }}` |

### Macros

Macros are executed and their output is rendered, with only undefined variables within the macro output being preserved:

```jinja
{# macros.jinja #}
{% macro greet(name) %}Hello {{ name }}, {{ title }}!{% endmacro %}
```

| Feature | Input | Context | Output |
|---------|-------|---------|--------|
| Macro with undefined var | `{{ m.greet('World') }}` | `{}` | `Hello World, {{ title }}!` |
| Macro fully defined | `{{ m.greet('World') }}` | `{title: "Mr"}` | `Hello World, Mr!` |

## Multi-Pass Rendering Example

```java
// First pass: render static values
Map<String, Object> staticContext = new HashMap<>();
staticContext.put("appName", "MyApp");
staticContext.put("version", "1.0");

JinjavaConfig preserveConfig = JinjavaConfig.newBuilder()
    .withExecutionMode(PreserveUndefinedExecutionMode.instance())
    .build();

String template = "{{ appName }} v{{ version }} - Welcome {{ userName }}!";
String firstPass = jinjava.render(template, staticContext, preserveConfig);
// Result: "MyApp v1.0 - Welcome {{ userName }}!"

// Second pass: render dynamic values
Map<String, Object> dynamicContext = new HashMap<>();
dynamicContext.put("userName", "Alice");

JinjavaConfig defaultConfig = JinjavaConfig.newBuilder()
    .withExecutionMode(DefaultExecutionMode.instance())
    .build();

String secondPass = jinjava.render(firstPass, dynamicContext, defaultConfig);
// Result: "MyApp v1.0 - Welcome Alice!"
```

## Implementation Details

`PreserveUndefinedExecutionMode` extends `EagerExecutionMode` and configures the context with:

1. **PreserveUndefinedExpressionStrategy** - Returns original expression syntax when variables are undefined, instead of internal representations
2. **DynamicVariableResolver** - Returns `DeferredValue.instance()` for undefined variables, triggering preservation
3. **PartialMacroEvaluation** - Allows macros to execute and return partial results with undefined parts preserved
4. **PreserveResolvedSetTags** - Preserves set tags even when RHS is fully resolved, enabling multi-pass variable binding

### New Context Flag: `isPreserveResolvedSetTags`

A new context configuration flag was added to allow independent control over set tag preservation:

```java
// In ContextConfigurationIF
default boolean isPreserveResolvedSetTags() {
    return false;
}

// Usage in Context
context.setPreserveResolvedSetTags(true);
```

This flag is checked in `EagerSetTagStrategy` to determine whether fully resolved set tags should be preserved in output or consumed during rendering.

## Files Changed

- `PreserveUndefinedExecutionMode.java` - Main execution mode implementation
- `PreserveUndefinedExpressionStrategy.java` - Expression strategy for preserving original syntax
- `ContextConfigurationIF.java` - Added `isPreserveResolvedSetTags` flag
- `Context.java` - Added getter/setter for new flag
- `EagerSetTagStrategy.java` - Modified to check new flag
- `PreserveUndefinedExecutionModeTest.java` - Comprehensive test coverage
