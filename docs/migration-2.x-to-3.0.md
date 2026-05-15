# Migrating from Jinjava 2.x to 3.0

Jinjava 3.0 is a major release that changes defaults to align more closely with Python/Jinja2 semantics and introduces a deny-by-default method sandbox. Most templates will continue to work, but **your custom Java objects will not be accessible from templates unless you explicitly allowlist them**.

Three things to know up front:

1. **Python-aligned defaults are on.** Jinjava 3.0 ships with `LegacyOverrides.THREE_POINT_0` (all 10 behavior flags enabled). If your templates depend on 2.x quirks, set `LegacyOverrides.NONE` to restore the old behavior.
2. **Nested interpretation is off.** `isNestedInterpretationEnabled()` changed from `true` to `false`. If your templates rely on evaluating expressions inside already-rendered content, opt back in explicitly.
3. **Method and return-type allowlists are enforced.** Jinjava's built-in types (primitives, collections, filters, dates) are allowed by default. Your domain classes are not — you must add them to the allowlist or method calls will silently return `null`.

---

## Table of Contents

- [Dependency Update](#dependency-update)
- [What Changed: Themes](#what-changed-themes)
- [Breaking Changes](#breaking-changes)
- [LegacyOverrides: The Compatibility Knob](#legacyoverrides-the-compatibility-knob)
- [Method and Return-Type Allowlists](#method-and-return-type-allowlists)
  - [Why This Exists](#why-this-exists)
  - [What's Allowed by Default](#whats-allowed-by-default)
  - [Building a Custom Allowlist](#building-a-custom-allowlist)
  - [Debugging "My Method Returns Null Now"](#debugging-my-method-returns-null-now)
- [Migration Recipes](#migration-recipes)
- [Reference: LegacyOverrides Flags](#reference-legacyoverrides-flags)
- [Reference: JinjavaConfig Changes](#reference-jinjavaconfig-changes)
- [Appendix: Notable Pull Requests](#appendix-notable-pull-requests)

---

## Dependency Update

```xml
<!-- 2.x -->
<dependency>
  <groupId>com.hubspot.jinjava</groupId>
  <artifactId>jinjava</artifactId>
  <version>2.8.x</version>
</dependency>

<!-- 3.0 -->
<dependency>
  <groupId>com.hubspot.jinjava</groupId>
  <artifactId>jinjava</artifactId>
  <version>3.0.0</version>
</dependency>
```

Jinjava 3.0 requires Java 17+. The groupId and artifactId are unchanged.

---

## What Changed: Themes

### Python-Semantic Alignment

Jinjava 3.0 defaults to behavior that matches Python/Jinja2 more closely: natural operator precedence, snake_case property naming, map iteration over keys, strict whitespace control parsing, and more. Each of these is individually controllable via `LegacyOverrides` (see below).

### Immutable Configuration

`JinjavaConfig` is now backed by the [Immutables](https://immutables.github.io/) annotation processor. The builder API (`JinjavaConfig.builder()...build()`) is unchanged, but the class is now truly immutable. Direct field mutation is no longer possible.

### Sandbox Security

Method calls and return types from template expressions are validated against an allowlist. This prevents templates from accessing arbitrary Java methods (reflection, class loading, Jackson deserialization gadgets, etc.).

### PyishDate Enhancements

`PyishDate` now delegates all public `ZonedDateTime` instance methods, so templates can call date methods like `withYear()`, `toLocalDate()`, `getZone()`, etc. without workarounds.

### Performance

Allowlist validators use `ConcurrentHashMap`-based caching for method and class lookups. Primitives (`String`, `Number`, `Boolean`) short-circuit the return-type validator entirely.

---

## Breaking Changes

### Default Behavior Changes

All 10 `LegacyOverrides` flags default to `true` in 3.0 (vs. `false` in 2.x). See the [full reference table](#reference-legacyoverrides-flags) for what each flag controls. The most impactful:

| Change | Impact |
|---|---|
| `iterateOverMapKeys` = `true` | `{% for item in dict %}` iterates over keys, not values |
| `useSnakeCasePropertyNaming` = `true` | `{{ obj.myField }}` must be written as `{{ obj.my_field }}` |
| `useNaturalOperatorPrecedence` = `true` | `2 + 3 * 4` evaluates to `14` (not `20`) |
| `usePyishObjectMapper` = `true` | JSON serialization uses snake_case naming strategy |

Additionally, `isNestedInterpretationEnabled()` flipped from `true` to `false`.

### Removed/Replaced API

| 2.x API | 3.0 Replacement |
|---|---|
| `restrictedMethods` / `restrictedProperties` on JinjavaConfig | `AllowlistMethodValidator` / `AllowlistReturnTypeValidator` |
| Mutable `setTokenScannerSymbols()` | Use the builder: `.withTokenScannerSymbols(...)` |

### Deprecated (Still Working)

| Deprecated | Use Instead |
|---|---|
| `JinjavaConfig.getNodePreProcessor()` | `getProcessors().getNodePreProcessor()` |
| `JinjavaConfig.isIterateOverMapKeys()` | `getLegacyOverrides().isIterateOverMapKeys()` |

---

## LegacyOverrides: The Compatibility Knob

`LegacyOverrides` is the primary mechanism for controlling migration pace. Each flag opts into a 3.0 behavior when set to `true`, or preserves 2.x behavior when `false`.

Three preset constants are provided:

| Constant | Meaning |
|---|---|
| `LegacyOverrides.NONE` | All flags `false` — full 2.x behavior |
| `LegacyOverrides.THREE_POINT_0` | All flags `true` — full 3.0 behavior (the default) |
| `LegacyOverrides.ALL` | Same as `THREE_POINT_0` |

### Using a Preset

```java
// Full 2.x compatibility
JinjavaConfig config = JinjavaConfig.builder()
    .withLegacyOverrides(LegacyOverrides.NONE)
    .build();
```

### Selective Override

Use `Builder.from()` to start from a preset and flip individual flags:

```java
LegacyOverrides overrides = LegacyOverrides.newBuilder()
    .from(LegacyOverrides.THREE_POINT_0)
    .withUseSnakeCasePropertyNaming(false)  // keep camelCase for now
    .build();

JinjavaConfig config = JinjavaConfig.builder()
    .withLegacyOverrides(overrides)
    .build();
```

### Gradual Adoption Strategy

The recommended migration path is incremental:

1. Start with `LegacyOverrides.NONE` to restore 2.x behavior.
2. Flip one flag to `true` at a time.
3. Run your template test suite after each flip.
4. Each flag has a narrow, well-defined scope, so failures are easy to attribute.

This is the same approach that was used for the largest known Jinjava deployment (rendering hundreds of millions of page views per month). Services adopted overrides incrementally over weeks, not all at once.

---

## Method and Return-Type Allowlists

### Why This Exists

Jinjava 2.x used a blocklist approach to prevent dangerous method calls from templates. While the latest 2.8.3 release addresses known sandbox escapes, the blocklist model proved fragile over time — a history of multiple bypasses demonstrated that it's too easy to miss a dangerous class or method. Each new gadget chain required a reactive patch.

Jinjava 3.0 inverts the model with a **deny-by-default** allowlist: only explicitly permitted methods and return types are accessible from templates. Calls to non-allowlisted methods silently return `null`. This means new classes are safe by default, and the attack surface only grows when you explicitly opt in.

A set of banned classes is enforced at configuration time to prevent accidental sandbox bypass:

- `java.lang.Object`
- `java.lang.Class`
- `java.lang.reflect.*`
- `com.fasterxml.jackson.databind.*`

Attempting to add any of these to an allowlist throws `IllegalStateException` at startup.

### What's Allowed by Default

`AllowlistMethodValidator.DEFAULT` and `AllowlistReturnTypeValidator.DEFAULT` include all 7 built-in `AllowlistGroup` values:

| Group | What It Covers |
|---|---|
| `JavaPrimitives` | `String`, `Long`, `Integer`, `Double`, `Float`, `Boolean`, `BigDecimal`, `BigInteger`, and their primitive counterparts |
| `JinjavaObjects` | `PyList`, `PyMap`, `PySet`, `SizeLimitingPyList/Map/Set`, `SnakeCaseAccessibleMap`, `FormattedDate`, `PyishDate`, `DummyObject`, `Namespace`, `SafeString`, `NullValue` |
| `Collections` | `Map.Entry`, `ArrayList`, `LinkedHashMap`, Guava `ForwardingList/Map/Set/Collection`; also enables arrays |
| `JinjavaTagConstructs` | `ForLoop`, `MacroFunction`, `EagerMacroFunction` |
| `JinjavaFilters` | All classes under `com.hubspot.jinjava.lib.filter.*` (prefix match) |
| `JinjavaFunctions` | `ZonedDateTime` |
| `JinjavaExpTests` | All classes under `com.hubspot.jinjava.lib.exptest.*` (prefix match) |

If your templates only use Jinjava's built-in types and your own primitive/string-valued context variables, **the defaults will work with no configuration**. If you pass custom domain objects into template context, you need a custom allowlist.

### Building a Custom Allowlist

Suppose your application passes `User` and `Order` objects into templates:

```java
// Step 1: Build a method validator that extends the defaults with your domain
AllowlistMethodValidator methodValidator = AllowlistMethodValidator.create(
    MethodValidatorConfig.builder()
        .addDefaultAllowlistGroups()
        .addAllowedDeclaredMethodsFromCanonicalClassPrefixes(
            "com.example.domain."
        )
        .build()
);

// Step 2: Build a return-type validator similarly
AllowlistReturnTypeValidator returnTypeValidator = AllowlistReturnTypeValidator.create(
    ReturnTypeValidatorConfig.builder()
        .addDefaultAllowlistGroups()
        .addAllowedCanonicalClassPrefixes(
            "com.example.domain."
        )
        .setAllowArrays(true)
        .build()
);

// Step 3: Wire them into JinjavaConfig
JinjavaConfig config = JinjavaConfig.builder()
    .withMethodValidator(methodValidator)
    .withReturnTypeValidator(returnTypeValidator)
    .build();

Jinjava jinjava = new Jinjava(config);
```

The builders support three matching modes:

| Builder Method | Match Rule |
|---|---|
| `addAllowedMethods(Method...)` | Exact `java.lang.reflect.Method` match |
| `addAllowedDeclaredMethodsFromCanonicalClassNames(String...)` | Exact canonical class name (e.g. `"com.example.domain.User"`) |
| `addAllowedDeclaredMethodsFromCanonicalClassPrefixes(String...)` | Canonical class name starts with prefix (e.g. `"com.example.domain."`) |

For return types, the corresponding methods are `addAllowedCanonicalClassNames(...)` and `addAllowedCanonicalClassPrefixes(...)`.

**Prefix convention:** Prefixes should typically end with `.` to avoid matching unintended classes. For example, `"com.example.domain."` matches `com.example.domain.User` but not `com.example.domainutils.Helper`.

You can also select individual `AllowlistGroup` values instead of all defaults:

```java
MethodValidatorConfig.builder()
    .addAllowlistGroups(AllowlistGroup.JavaPrimitives, AllowlistGroup.Collections)
    .addAllowedDeclaredMethodsFromCanonicalClassPrefixes("com.example.domain.")
    .build();
```

### Debugging "My Method Returns Null Now"

The most common 3.0 upgrade symptom: a template expression that worked in 2.x now renders empty. This happens when:

1. A method is called on a class not in the allowlist (method validator rejects it, returns `null`).
2. A method returns an object whose class is not in the return-type allowlist (return-type validator replaces it with `null`).

**Wire up rejection callbacks to surface these:**

```java
MethodValidatorConfig.builder()
    .addDefaultAllowlistGroups()
    .onRejectedMethod(method ->
        LOG.warn("Blocked method call: {}.{}",
            method.getDeclaringClass().getCanonicalName(),
            method.getName()))
    .build();

ReturnTypeValidatorConfig.builder()
    .addDefaultAllowlistGroups()
    .onRejectedClass(clazz ->
        LOG.warn("Blocked return type: {}", clazz.getCanonicalName()))
    .build();
```

In tests, escalate rejections so they fail loudly:

```java
.onRejectedMethod(method -> {
    throw new AssertionError("Unexpected blocked method: "
        + method.getDeclaringClass().getCanonicalName() + "." + method.getName());
})
```

For reference, Jinjava's own test suite uses this pattern in `BaseJinjavaTest`:

```java
public static final AllowlistMethodValidator METHOD_VALIDATOR =
    AllowlistMethodValidator.create(
        MethodValidatorConfig.builder()
            .addDefaultAllowlistGroups()
            .addAllowedDeclaredMethodsFromCanonicalClassPrefixes(
                "com.hubspot.jinjava.testobjects."
            )
            .build()
    );

public static JinjavaConfig.Builder newConfigBuilder() {
    return JinjavaConfig.builder()
        .withMethodValidator(METHOD_VALIDATOR)
        .withReturnTypeValidator(RETURN_TYPE_VALIDATOR);
}
```

---

## Migration Recipes

### Recipe 1: Drop-In 2.x Compatibility

Start here if you want 3.0 on the classpath but need templates to behave identically to 2.x while you audit:

```java
Jinjava jinjava = new Jinjava(
    JinjavaConfig.builder()
        .withLegacyOverrides(LegacyOverrides.NONE)
        .withNestedInterpretationEnabled(true)
        .withMethodValidator(
            AllowlistMethodValidator.create(
                MethodValidatorConfig.builder()
                    .addDefaultAllowlistGroups()
                    .addAllowedDeclaredMethodsFromCanonicalClassPrefixes(
                        "com.yourcompany.domain."
                    )
                    .build()
            )
        )
        .withReturnTypeValidator(
            AllowlistReturnTypeValidator.create(
                ReturnTypeValidatorConfig.builder()
                    .addDefaultAllowlistGroups()
                    .addAllowedCanonicalClassPrefixes(
                        "com.yourcompany.domain."
                    )
                    .build()
            )
        )
        .build()
);
```

### Recipe 2: Gradual Adoption

Start from Recipe 1, then flip flags individually:

```java
// Week 1: adopt natural operator precedence
LegacyOverrides overrides = LegacyOverrides.newBuilder()
    .from(LegacyOverrides.NONE)
    .withUseNaturalOperatorPrecedence(true)
    .build();

// Week 2: adopt map key iteration
overrides = LegacyOverrides.newBuilder()
    .from(overrides)
    .withIterateOverMapKeys(true)
    .build();

// ... continue until all flags are true
```

What to audit for each flag:

| Flag | What to Look For in Templates |
|---|---|
| `useNaturalOperatorPrecedence` | Arithmetic/comparison expressions that depend on Java-style evaluation order |
| `iterateOverMapKeys` | `{% for item in dict %}` loops — `item` will be keys, not values |
| `useSnakeCasePropertyNaming` | `{{ obj.myField }}` needs to become `{{ obj.my_field }}` |
| `usePyishObjectMapper` | Any JSON serialization of context objects — property names change |
| `parseWhitespaceControlStrictly` | `{%-`, `-%}` syntax — strict parser rejects some lenient constructs |
| `allowAdjacentTextNodes` | Usually safe to enable; affects AST structure, not output |
| `useTrimmingForNotesAndExpressions` | Whitespace around `{# #}` and `{{ }}` may change |
| `keepNullableLoopValues` | `{% for item in list %}` when list contains nulls — nulls are preserved |
| `evaluateMapKeys` | `{% for key in {foo: 1} %}` — `foo` is evaluated as an expression, not a string |
| `iteratorOnlyReverseFilter` | `| reverse` only works on iterables, not arrays/lists directly |

### Recipe 3: Greenfield 3.0

If you're starting fresh or have good test coverage, use the 3.0 defaults and just add your domain classes:

```java
Jinjava jinjava = new Jinjava(
    JinjavaConfig.builder()
        // LegacyOverrides.THREE_POINT_0 is the default — no need to set it
        .withMethodValidator(
            AllowlistMethodValidator.create(
                MethodValidatorConfig.builder()
                    .addDefaultAllowlistGroups()
                    .addAllowedDeclaredMethodsFromCanonicalClassPrefixes(
                        "com.yourcompany.domain."
                    )
                    .onRejectedMethod(method -> LOG.warn("Blocked: {}",
                        method.getDeclaringClass().getCanonicalName() + "." + method.getName()))
                    .build()
            )
        )
        .withReturnTypeValidator(
            AllowlistReturnTypeValidator.create(
                ReturnTypeValidatorConfig.builder()
                    .addDefaultAllowlistGroups()
                    .addAllowedCanonicalClassPrefixes(
                        "com.yourcompany.domain."
                    )
                    .build()
            )
        )
        .build()
);
```

---

## Reference: LegacyOverrides Flags

All flags default to `false` when using `LegacyOverrides.NONE`, and `true` when using `LegacyOverrides.THREE_POINT_0` (the default in 3.0).

| Flag | When `true` (3.0 behavior) | When `false` (2.x behavior) |
|---|---|---|
| `evaluateMapKeys` | Map literal keys (`{key: val}`) are evaluated as expressions | Map literal keys are treated as string literals |
| `iterateOverMapKeys` | `{% for x in dict %}` iterates over keys | Iterates over values |
| `usePyishObjectMapper` | Uses Python-like object attribute/index access | Uses Java-style object access |
| `useSnakeCasePropertyNaming` | Properties converted to snake_case (`my_field`) | Properties use Java camelCase (`myField`) |
| `useNaturalOperatorPrecedence` | Standard math precedence (`*` before `+`) | Java EL precedence |
| `parseWhitespaceControlStrictly` | Strict validation of `{%-` / `-%}` syntax | Lenient parsing |
| `allowAdjacentTextNodes` | Combines adjacent text/literal nodes in AST | Separate text nodes |
| `useTrimmingForNotesAndExpressions` | Trims whitespace around `{# #}` and `{{ }}` | No automatic trimming |
| `keepNullableLoopValues` | Preserves null values in loop iterations | Skips null values |
| `iteratorOnlyReverseFilter` | `\| reverse` works on iterables only | `\| reverse` works on arrays, lists, and iterables |

---

## Reference: JinjavaConfig Changes

### New Fields in 3.0

| Field | Type | Default | Purpose |
|---|---|---|---|
| `methodValidator` | `AllowlistMethodValidator` | `DEFAULT` (all groups) | Controls which methods are callable from templates |
| `returnTypeValidator` | `AllowlistReturnTypeValidator` | `DEFAULT` (all groups) | Controls which return types are allowed |
| `featureConfig` | `FeatureConfig` | empty | Fine-grained feature flags |
| `processors` | `JinjavaProcessors` | empty | Custom pre/post-processors for AST nodes |

### Default Changes

| Field | 2.x Default | 3.0 Default |
|---|---|---|
| `nestedInterpretationEnabled` | `true` | `false` |
| `legacyOverrides` | `LegacyOverrides.NONE` | `LegacyOverrides.THREE_POINT_0` |

### Architectural Change

`JinjavaConfig` is now generated by the Immutables annotation processor (`@Value.Immutable`). The `Builder` class extends the generated `ImmutableJinjavaConfig.Builder`, and `build()` is aliased to `buildImpl()` for binary compatibility. The public API (`JinjavaConfig.builder()...withX(...)...build()`) is unchanged.

---

## Appendix: Notable Pull Requests

For a full diff, see the [GitHub comparison view](https://github.com/HubSpot/jinjava/compare/master-2.8.x...master).

### Configuration and Architecture

- [#1290](https://github.com/HubSpot/jinjava/pull/1290) — Migrated `JinjavaConfig` to Immutables
- [#1291](https://github.com/HubSpot/jinjava/pull/1291) — Base Jinjava 3.0 setup
- [#1292](https://github.com/HubSpot/jinjava/pull/1292) — Added `LegacyOverrides.THREE_POINT_0` preset
- [#1294](https://github.com/HubSpot/jinjava/pull/1294) — Defaulted LegacyOverrides to `THREE_POINT_0`
- [#1295](https://github.com/HubSpot/jinjava/pull/1295) — Disabled nested interpretation by default
- [#1316](https://github.com/HubSpot/jinjava/pull/1316) — Reduced binary incompatibility in `JinjavaConfig`; added `PyishDate` `ZonedDateTime` delegation and `iteratorOnlyReverseFilter`

### Sandbox / Allowlists

- [#1296](https://github.com/HubSpot/jinjava/pull/1296) — Moved test objects into `testobjects` package for allowlisting
- [#1297](https://github.com/HubSpot/jinjava/pull/1297) — Introduced method and return-type validators
- [#1298](https://github.com/HubSpot/jinjava/pull/1298) — Added `BaseJinjavaTest` with canonical allowlist setup
- [#1301](https://github.com/HubSpot/jinjava/pull/1301) — Required prefix packages to end with `.` for explicit matching

### Bug Fixes

- [#1308](https://github.com/HubSpot/jinjava/pull/1308) — Fixed integer-to-long set conversion
- [#1310](https://github.com/HubSpot/jinjava/pull/1310) — Fixed NPE when calling macros in ternary operators
- [#1312](https://github.com/HubSpot/jinjava/pull/1312) — Fixed block reconstruction
- [#1313](https://github.com/HubSpot/jinjava/pull/1313) — Fixed raw override parameter unwrapping

### Performance

- [#1300](https://github.com/HubSpot/jinjava/pull/1300) — Validator caching, primitive fast-paths, ScopeMap optimization
