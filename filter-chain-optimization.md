# Filter Chain Optimization

## Overview

This branch introduces a performance optimization for chained filter expressions in Jinjava's expression language. The optimization is **configurable** and **disabled by default** for backward compatibility.

## Problem

Previously, chained filters like `input|trim|lower|length` were parsed as deeply nested AST method calls:

```
filter:length.filter(filter:lower.filter(filter:trim.filter(input, interpreter), interpreter), interpreter)
```

This resulted in:
- Multiple redundant filter lookups per AST node traversal
- Increased method invocation overhead
- Extra object wrapping/unwrapping between filters
- Unnecessary context operations

## Solution

The optimization introduces a new `AstFilterChain` AST node that represents the entire filter chain as a **single evaluation unit**:

```
input|trim|lower|length
```

Instead of nested method calls, the filter chain is evaluated in a single pass:
1. Look up each filter once
2. Directly invoke `filter.filter(...)` sequentially
3. Handle `SafeString` preservation inline
4. Handle disabled filters and errors

## New Files

| File | Description |
|------|-------------|
| `AstFilterChain.java` | AST node for optimized filter chain evaluation |
| `FilterSpec.java` | Data class holding filter name and parameters |
| `AstFilterChainPerformanceTest.java` | Performance tests verifying the optimization |

## Modified Files

| File | Changes |
|------|---------|
| `JinjavaConfig.java` | Added `enableFilterChainOptimization` config option |
| `ExtendedParser.java` | Added conditional logic to use optimization based on config |
| `EagerExtendedParser.java` | Override to always use old behavior (no optimization for eager mode) |

## Configuration

To enable the filter chain optimization:

```java
JinjavaConfig config = JinjavaConfig.newBuilder()
    .withEnableFilterChainOptimization(true)
    .build();

Jinjava jinjava = new Jinjava(config);
```

### Behavior by Execution Mode

| Mode | Config Enabled | Behavior |
|------|---------------|----------|
| Normal (non-eager) | `true` | Uses optimized `AstFilterChain` |
| Normal (non-eager) | `false` | Uses old nested method approach |
| Eager | `true` or `false` | Always uses old nested method approach |

**Note:** The optimization is disabled for eager execution mode regardless of configuration, as eager mode requires the old behavior for proper deferred value handling.

## Performance

The optimization provides measurable performance improvements for chained filter expressions:

- **Single filter**: Minimal improvement
- **2+ chained filters**: Noticeable improvement
- **5+ chained filters**: Significant improvement

Run the performance test to see actual numbers:

```bash
# Run automated performance test
mvn test -Dtest=AstFilterChainPerformanceTest

# Run detailed benchmark (main method)
mvn exec:java -Dexec.mainClass="com.hubspot.jinjava.el.ext.AstFilterChainPerformanceTest"
```

## Commits

1. **Chained filters optimization** - Initial implementation with `AstFilterChain` and `FilterSpec`
2. **Make filter chain optimization configurable and disable for eager mode** - Added config option and disabled for eager execution
3. **Add performance test for filter chain optimization** - Added tests to verify correctness and performance

## Backward Compatibility

- The optimization is **disabled by default** (`enableFilterChainOptimization = false`)
- Existing code works unchanged without any configuration
- Eager execution mode always uses the old behavior
- All existing tests pass without modification
