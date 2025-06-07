# AutoCloseableWrapper Refactoring Progress

## Overview
Applied AutoCloseableWrapper pattern to replace explicit stack popping throughout the Jinjava codebase, following the pattern established in commit 92f9f0a8 for ImportTag.

## Completed Tasks ✅

### 1. Context Helper Methods
**File**: `src/main/java/com/hubspot/jinjava/interpret/Context.java`
- Added `pushCurrentPath()` - wraps getCurrentPathStack().push() with AutoCloseableWrapper
- Added `pushImportPath()` - wraps getImportPathStack().push() with AutoCloseableWrapper  
- Added `pushIncludePath()` - wraps getIncludePathStack().push() with AutoCloseableWrapper
- Added `pushFromStackWithWrapper()` - wraps pushFromStack() with AutoCloseableWrapper
- Added `pushMacroStack()` - wraps getMacroStack().push() with AutoCloseableWrapper
- Added `withDualStackPush()` - handles dual stack operations for IncludeTag use case

### 2. MacroFunction.java
**File**: `src/main/java/com/hubspot/jinjava/lib/fn/MacroFunction.java`
- ✅ **COMPLETED**: Refactored `doEvaluate()` method to use `getImportFileWithWrapper()`
- Added `getImportFileWithWrapper()` method that returns `AutoCloseableWrapper<Optional<String>>`
- Replaced manual finally block with try-with-resources
- Pattern: `try (AutoCloseableWrapper<Optional<String>> importFile = getImportFileWithWrapper(interpreter))`

### 3. EagerMacroFunction.java  
**File**: `src/main/java/com/hubspot/jinjava/lib/fn/eager/EagerMacroFunction.java`
- ✅ **COMPLETED**: Refactored reconstructing branch in `doEvaluate()`
- Uses `getImportFileWithWrapper()` from parent MacroFunction class
- Replaced manual finally block with try-with-resources

### 4. IncludeTag.java
**File**: `src/main/java/com/hubspot/jinjava/lib/tag/IncludeTag.java`
- ✅ **COMPLETED**: Refactored dual stack operations (includePathStack + currentPathStack)
- Kept original exception handling for IncludeTagCycleException outside try-with-resources
- Used custom AutoCloseableWrapper to pop both stacks in correct order
- Note: Some diagnostic warnings about lambda variable scope

## Completed Tasks ✅ (Continued)

### 5. FromTag.java
**File**: `src/main/java/com/hubspot/jinjava/lib/tag/FromTag.java`
- ✅ **COMPLETED**: Created `getTemplateFileWithWrapper()` method that returns `AutoCloseableWrapper<String>`
- Added `@Deprecated` to original `getTemplateFile()` method for backwards compatibility
- Updated `interpret()` method to use try-with-resources pattern
- Returns `null` from wrapper for cycle exceptions with no-op cleanup

### 6. EagerFromTag.java
**File**: `src/main/java/com/hubspot/jinjava/lib/tag/eager/EagerFromTag.java`
- ✅ **COMPLETED**: Refactored to use `FromTag.getTemplateFileWithWrapper()`
- Replaced manual finally block with try-with-resources
- Moved DeferredValueException handling to catch block outside try-with-resources
- Uses same AutoCloseable pattern as parent FromTag

### 7. ImportTag.java (Remaining)
**File**: `src/main/java/com/hubspot/jinjava/lib/tag/ImportTag.java` 
- ✅ **COMPLETED**: Created `getTemplateFileWithWrapper()` method that wraps import path stack operations
- Added `@Deprecated` to original `getTemplateFile()` method for backwards compatibility  
- Updated `interpret()` method to use nested try-with-resources (template file + node)
- Eliminated manual finally block that was popping import path stack

### 8. EagerImportTag.java (Remaining)
**File**: `src/main/java/com/hubspot/jinjava/lib/tag/eager/EagerImportTag.java`
- ✅ **COMPLETED**: Refactored to use `ImportTag.getTemplateFileWithWrapper()`
- Replaced manual finally block with try-with-resources
- Moved DeferredValueException handling to catch block outside try-with-resources
- Uses nested try-with-resources for both template file and node parsing

### 9. AstMacroFunction.java
**File**: `src/main/java/com/hubspot/jinjava/el/ext/AstMacroFunction.java`
- ✅ **COMPLETED**: Created `checkAndPushMacroStackWithWrapper()` method
- Returns `AutoCloseableWrapper<Boolean>` where Boolean indicates if early return needed
- Added `@Deprecated` to original `checkAndPushMacroStack()` method
- Handles complex macro stack logic (max depth, cycle check, validation mode)
- Separate code paths for caller vs non-caller macros

### 10. JinjavaInterpreter.java Review
**File**: `src/main/java/com/hubspot/jinjava/interpret/JinjavaInterpreter.java`
- ✅ **COMPLETED**: Created `conditionallyPushParentPath()` helper method
- Refactored conditional push/pop pattern in `resolveBlockStubs()` method
- Returns `AutoCloseableWrapper<Boolean>` indicating if path was pushed
- Eliminated manual boolean tracking and conditional pop logic

## Key Patterns Established

### Try-With-Resources Pattern
```java
try (AutoCloseableWrapper<T> resource = getResourceWithWrapper(...)) {
  // use resource.get()
} // automatic cleanup
```

### Stack Operation Wrapper Pattern
```java
public AutoCloseableWrapper<StackType> pushSomethingWithWrapper(...) {
  stack.push(...);
  return AutoCloseableWrapper.of(stack, StackType::pop);
}
```

### Exception Handling Pattern
For operations that can throw cycle exceptions, handle outside try-with-resources:
```java
try {
  stack.push(...);
} catch (CycleException e) {
  // handle error, return early
}
try (AutoCloseableWrapper<T> wrapper = ...) {
  // main logic
}
```

## Files Modified
1. `src/main/java/com/hubspot/jinjava/interpret/Context.java` ✅
2. `src/main/java/com/hubspot/jinjava/lib/fn/MacroFunction.java` ✅  
3. `src/main/java/com/hubspot/jinjava/lib/fn/eager/EagerMacroFunction.java` ✅
4. `src/main/java/com/hubspot/jinjava/lib/tag/IncludeTag.java` ✅
5. `src/main/java/com/hubspot/jinjava/lib/tag/FromTag.java` ✅
6. `src/main/java/com/hubspot/jinjava/lib/tag/eager/EagerFromTag.java` ✅
7. `src/main/java/com/hubspot/jinjava/lib/tag/ImportTag.java` ✅
8. `src/main/java/com/hubspot/jinjava/lib/tag/eager/EagerImportTag.java` ✅
9. `src/main/java/com/hubspot/jinjava/el/ext/AstMacroFunction.java` ✅
10. `src/main/java/com/hubspot/jinjava/interpret/JinjavaInterpreter.java` ✅

## Testing Results ✅
- **Compilation**: ✅ All files compile successfully
- **Checkstyle**: ✅ No style violations  
- **FromTag Tests**: ✅ All tests pass
- **ImportTag Tests**: ✅ All tests pass
- **Macro Tests**: ✅ All tests pass
- **No Regressions**: ✅ Verified through targeted testing

## Benefits Achieved
- Automatic resource cleanup via try-with-resources
- Elimination of manual finally blocks for stack operations  
- More robust exception handling (stacks auto-pop even on exceptions)
- Consistent pattern across codebase
- Reduced chance of forgetting to pop stacks