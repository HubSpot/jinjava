# PreserveUnknownExecutionMode

A Jinjava execution mode that preserves unknown/undefined variables as their original template syntax instead of rendering them as empty strings. This enables multi-pass rendering scenarios where templates are processed in stages with different variable contexts available at each stage.

## Usage

```java
JinjavaConfig config = JinjavaConfig
  .newBuilder()
  .withExecutionMode(PreserveUnknownExecutionMode.instance())
  .build();
Jinjava jinjava = new Jinjava(config);

String result = jinjava.render("Hello {{ name }}!", new HashMap<>());
// Result: "Hello {{ name }}!"
```

## Behavior

### Expressions

| Template | Context | Output |
|----------|---------|--------|
| `{{ unknown }}` | `{}` | `{{ unknown }}` |
| `{{ name }}` | `{name: "World"}` | `World` |
| `{{ name \| upper }}` | `{}` | `{{ name \| upper }}` |
| `{{ obj.property }}` | `{}` | `{{ obj.property }}` |

### Control Structures

| Template | Context | Output |
|----------|---------|--------|
| `{% if item %}yes{% endif %}` | `{}` | `{% if item %}yes{% endif %}` |
| `{% if item %}yes{% endif %}` | `{item: true}` | `yes` |
| `{% if item %}yes{% else %}no{% endif %}` | `{}` | `{% if item %}yes{% else %}no{% endif %}` |
| `{% for x in items %}{{ x }}{% endfor %}` | `{}` | `{% for x in items %}{{ x }}{% endfor %}` |
| `{% for x in items %}{{ x }}{% endfor %}` | `{items: ["a","b"]}` | `ab` |

### Set Tags

Set tags are preserved in output while their right-hand side expressions are evaluated when possible:

| Template | Output |
|----------|--------|
| `{% set var = unknown %}{{ var }}` | `{% set var = unknown %}{{ var }}` |
| `{% set a = 1 %}{{ a }}` | `{% set a = 1 %}1` |
| `{% set a = 1 %}{% set b = a %}{{ b }}` | `{% set a = 1 %}{% set b = 1 %}1` |

### Include Tags

| Scenario | Template | Output |
|----------|----------|--------|
| Unknown path variable | `{% include unknown_path %}` | `{% include unknown_path %}` |
| Known literal path | `{% include 'test.html' %}` | *(renders included content)* |
| Known path with unknown vars | `{% include 'test.html' %}` where test.html contains `{{ unknown }}` | `{{ unknown }}` |

## Implementation Details

`PreserveUnknownExecutionMode` configures the Jinjava context with:

1. **Eager Tag Decorators**: Registers eager versions of all tags to handle deferred values
2. **Dynamic Variable Resolver**: Returns `DeferredValue.instance()` for any unknown variable
3. **Deferred Execution Mode**: Enables preservation of set tags and other state-changing operations

```java
@Override
public void prepareContext(Context context) {
  // Register eager tag decorators for all tags
  context.getAllTags().stream()
    .filter(tag -> !(tag instanceof EagerTagDecorator))
    .map(EagerTagFactory::getEagerTagDecorator)
    .filter(Optional::isPresent)
    .forEach(maybeEagerTag -> context.registerTag(maybeEagerTag.get()));

  // Return DeferredValue for unknown variables
  context.setDynamicVariableResolver(varName -> DeferredValue.instance());

  // Preserve set tags in output
  context.setDeferredExecutionMode(true);
}
```

## Use Cases

- **Multi-pass rendering**: First pass resolves some variables, second pass resolves others
- **Template composition**: Combine templates where some placeholders are filled later
- **Partial evaluation**: Evaluate what's known now, defer what isn't
- **Template debugging**: See which variables are missing without errors
