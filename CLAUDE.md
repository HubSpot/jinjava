# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Jinjava is a Java-based template engine that implements Django template syntax, adapted for rendering Jinja templates. It's used in production to render thousands of websites with hundreds of millions of page views on the HubSpot CMS.

## Core Architecture

### Main Components
- **`Jinjava.java`** - Main API entry point for template rendering
- **`JinjavaConfig.java`** - Configuration for template engine behavior  
- **`interpret/`** - Core interpretation engine including `JinjavaInterpreter` and `Context`
- **`lib/`** - Template language features:
  - `tag/` - Template tags (if, for, extends, include, etc.)
  - `filter/` - Template filters for data transformation
  - `fn/` - Functions available in templates
  - `exptest/` - Expression tests (is defined, is even, etc.)
- **`tree/`** - AST nodes and parsing (`TreeParser`, `ExpressionNode`, `TagNode`, etc.)
- **`loader/`** - Template loading system (`ResourceLocator` implementations)
- **`el/`** - Expression language evaluation with custom extensions
- **`objects/`** - Python-like object wrappers (`PyList`, `PyMap`, `SafeString`, etc.)

### Execution Modes
- **Default mode** - Standard template execution
- **Eager mode** - Advanced execution mode for handling deferred expressions and macro optimization
  - Located in `mode/EagerExecutionMode.java` and various `eager/` subdirectories
  - Complex system for optimizing template rendering with deferred value resolution

## Development Commands

### Building and Testing
```bash
# Build the project
mvn clean compile

# Run all tests
mvn test

# Run specific test
mvn test -Dtest=TestClassName

# Run test with debug output
mvn test -Dtest=TestClassName -X

# Package with shaded dependencies
mvn clean package

# Check code style
mvn checkstyle:check

# Run SpotBugs analysis
mvn spotbugs:check
```

### Code Quality
The project uses:
- **Checkstyle** - Code style validation (config in `checkstyle.xml`)
- **SpotBugs** - Static analysis (exclusions in `spotbugs-exclude-filter.xml`) 
- **JaCoCo** - Test coverage reporting

## Key Dependencies
- **Guava** - Core utility library
- **Immutables** - Immutable object generation with HubSpot style
- **JUEL** - Expression language implementation (shaded)
- **JSoup** - HTML parsing (shaded)
- **Jackson** - JSON/YAML processing
- **AssertJ** - Test assertions
- **JUnit 4** - Testing framework

## Template Engine Features

### Template Loading
Templates are loaded via `ResourceLocator` implementations:
- `ClasspathResourceLocator` - Load from classpath
- `FileLocator` - Load from filesystem  
- `CascadingResourceLocator` - Chain multiple locators

### Custom Extensions
The engine supports custom:
- **Tags** - Implement `Tag` interface for custom template syntax
- **Filters** - Implement `Filter` interface for data transformation
- **Functions** - Register static methods as template functions
- **Expression Tests** - Implement `ExpTest` interface for custom boolean tests

### Security Considerations
- Template paths are resolved through `ResourceLocator` to prevent unauthorized file access
- Output size limits prevent memory exhaustion
- Expression evaluation has recursion depth limits

## Testing Strategy
- Unit tests in `src/test/java/` mirror the main source structure
- Integration tests use template files in `src/test/resources/`
- Eager execution mode has extensive test coverage in `src/test/resources/eager/`
- Test naming follows `itShouldDescribeExpectedBehavior()` pattern