# Bug Specification: Let Binding Stale Values

## Summary

When a function containing `let` or `let*` bindings is called multiple times (e.g., via `map`, `fold-left`, or repeated invocations), the let-bound variables retain values from the **first invocation** instead of being re-evaluated with the current parameter values.

**Severity**: High - Affects core functional programming patterns
**Status**: Fixed
**Affected Versions**: Prior to 2026-03-20
**Fixed In**: 2026-03-20

## Resolution

The bug was caused by `CachedFunctionSupplier` incorrectly caching let-bound variable lookups.

### Root Cause

`CachedFunctionSupplier` was designed to cache global variable lookups using version-based invalidation. However, it was also being used for let-bound variables, which are stored in the dynamic `letEnvStack` and change on each function invocation. Since let bindings don't update the global definition version, the cache was never invalidated, causing stale values to be returned.

### Fix Applied

Modified `CachedFunctionSupplier.get()` to check the `letEnvStack` FIRST, bypassing the cache for let-bound variables:

```java
@Override
public LispObject get() {
  // First check let environment stack - these are dynamic and should NOT be cached
  LispObject letValue = environment.lookupInLetEnvStack(atom);
  if (letValue != null) {
    return letValue;
  }

  // For global variables, use inline caching
  // ... existing caching logic ...
}
```

Added `lookupInLetEnvStack()` method to `LispEnvironment` to support this lookup.

### Files Changed

- `src/main/java/net/sourceforge/kleinlisp/evaluator/CachedFunctionSupplier.java`
- `src/main/java/net/sourceforge/kleinlisp/LispEnvironment.java`

### Tests Added

- `LetBindingBugTest.java` - Unit tests for all bug scenarios
- `LetBindingGuileComparisonTest.java` - Integration tests comparing behavior with Guile Scheme

## Reproduction

### Minimal Test Case

```scheme
(define (test acc val)
  (let ((captured val))
    (println (string-append "captured: " (number->string captured)
                           ", val: " (number->string val)))))

(fold-left test 0 (list 1 2 3))
```

**Expected Output**:
```
captured: 1, val: 1
captured: 2, val: 2
captured: 3, val: 3
```

**Actual Output**:
```
captured: 1, val: 1
captured: 1, val: 2
captured: 1, val: 3
```

### Additional Test Cases

#### Test 2: Using `map`

```scheme
(define (double x)
  (let ((result (* x 2)))
    result))

(map double (list 1 2 3 4 5))
```

**Expected**: `(2 4 6 8 10)`
**Actual**: `(2 2 2 2 2)` (first result repeated)

#### Test 3: Building data structures with `fold-left`

```scheme
(define (build-map acc item)
  (let ((key (car item))
        (val (cadr item)))
    (p-map-assoc acc key val)))

(fold-left build-map (p-map) (list (list "a" 1) (list "b" 2) (list "c" 3)))
```

**Expected**: Map with keys "a", "b", "c"
**Actual**: Map with only key "a" (or corrupted state)

#### Test 4: Direct parameter access works correctly

```scheme
(define (test-direct acc val)
  (println (string-append "val: " (number->string val)))
  val)

(fold-left test-direct 0 (list 1 2 3))
```

**Output** (CORRECT):
```
val: 1
val: 2
val: 3
```

This confirms the bug is specifically in `let`/`let*` binding evaluation, not in parameter passing.

#### Test 5: `let*` has the same bug

```scheme
(define (test-let-star acc val)
  (let* ((captured val)
         (doubled (* captured 2)))
    (println (string-append "doubled: " (number->string doubled)))))

(fold-left test-let-star 0 (list 1 2 3))
```

**Expected**: `doubled: 2`, `doubled: 4`, `doubled: 6`
**Actual**: `doubled: 2`, `doubled: 2`, `doubled: 2`

## Root Cause Analysis

### Code Flow

1. **Lambda Compilation** (`LambdaForm.java`):
   - When a lambda is compiled, `transformBodySymbols` replaces parameter references with `ComputedLispObject` instances
   - Each `ComputedLispObject` has a getter that reads from `environment.stackTop().parameterAt(i)`

2. **Let Form Compilation** (`LetForm.java` / `LetStarForm.java`):
   - The `apply` method compiles binding expressions **once** during function compilation
   - These compiled expressions (`valueSuppliers`) are stored and reused on every function call

3. **The Bug**:
   - When `let ((captured val))` is compiled:
     - `val` is already transformed to a `ComputedLispObject` by the outer lambda
     - This `ComputedLispObject` is wrapped in a supplier during let compilation
     - The supplier captures a reference to the specific `ComputedLispObject` instance

   - On first function call:
     - The `ComputedLispObject` getter returns parameter value correctly
     - **Caching may occur** in `ComputedLispObject.getObj()` if there's no setter

   - On subsequent calls:
     - The same `ComputedLispObject` instance is evaluated
     - If cached, it returns the stale first-call value
     - Even without caching, there may be issues with how the evaluation chain resolves

### Suspected Root Cause Locations

#### 1. `ComputedLispObject.java` - Caching

```java
private LispObject cache = null;

private LispObject getObj() {
    if (setter != null) {
        return getter.get();  // No cache if setter exists
    }
    if (cache == null) {
        cache = getter.get();  // PROBLEM: Caches on first access
    }
    return cache;
}
```

**Issue**: If the `ComputedLispObject` for a parameter has no setter (read-only), the value is cached on first access and never updated.

#### 2. `LambdaForm.java` - Parameter Object Creation

```java
private LispObject getParameterObj(int i) {
    Supplier<LispObject> getter = () -> {
        return environment.stackTop().parameterAt(i);
    };
    Consumer<LispObject> setter = (obj) -> {
        environment.stackTop().setParameterAt(i, obj);
    };
    return new ComputedLispObject(getter, setter);
}
```

**Note**: This includes a setter, so caching shouldn't occur for direct parameter references. However, the issue might be in how nested evaluations handle these objects.

#### 3. `LetForm.java` - Expression Compilation

```java
public Supplier<LispObject> apply(LispObject obj) {
    // ...
    for (LispObject elem : head.asList()) {
        ListObject tuple = elem.asList();
        AtomObject name = tuple.car().asAtom();
        LispObject valueExpr = tuple.cdr().car();
        names.add(name);
        valueSuppliers.add(valueExpr.accept(evaluator));  // Compiled ONCE
    }

    return () -> {
        LispObject[] values = new LispObject[valueSuppliers.size()];
        for (int i = 0; i < valueSuppliers.size(); i++) {
            values[i] = valueSuppliers.get(i).get();  // Called on EACH execution
        }
        // Push to let environment and evaluate body
    };
}
```

**Issue**: The value expression suppliers are compiled once but should produce fresh values each time. If the expression contains a `ComputedLispObject` that caches, we get stale values.

#### 4. `Evaluator.java` - ComputedLispObject Evaluation

```java
@Override
public Supplier<LispObject> visit(ComputedLispObject obj) {
    return obj.getComputed();  // Returns the raw getter
}
```

**Issue**: This returns the getter directly without wrapping it in a fresh evaluation context. When `let` bindings are compiled, this might not properly chain through the parameter resolution.

## Proposed Fix Approaches

### Approach 1: Remove/Fix Caching in ComputedLispObject

```java
// Option A: Disable caching entirely
private LispObject getObj() {
    return getter.get();  // Always evaluate fresh
}

// Option B: Add cache invalidation mechanism
public void invalidateCache() {
    this.cache = null;
}
```

**Pros**: Simple fix
**Cons**: May impact performance; caching might be intentional for other use cases

### Approach 2: Fresh ComputedLispObject per Evaluation

Modify `Evaluator.visit(ComputedLispObject)` to create a fresh wrapper:

```java
@Override
public Supplier<LispObject> visit(ComputedLispObject obj) {
    // Return a supplier that always re-evaluates
    return () -> obj.getComputed().get();
}
```

**Pros**: Preserves caching for other scenarios
**Cons**: Might not solve the root issue if caching happens at a different level

### Approach 3: Re-compile Let Binding Expressions

Modify `LetForm`/`LetStarForm` to re-compile expressions on each invocation:

```java
return () -> {
    LispObject[] values = new LispObject[valueExprs.size()];
    for (int i = 0; i < valueExprs.size(); i++) {
        // Re-compile and evaluate each time
        Supplier<LispObject> supplier = valueExprs.get(i).accept(evaluator);
        values[i] = supplier.get();
    }
    // ...
};
```

**Pros**: Ensures fresh evaluation
**Cons**: Performance hit from re-compilation; may break other optimizations

### Approach 4: Separate Parameter Resolution from Caching

Create a distinct `ParameterReference` object that never caches:

```java
public class ParameterReference implements LispObject {
    private final LispEnvironment environment;
    private final int index;

    @Override
    public LispObject evaluate() {
        return environment.stackTop().parameterAt(index);  // Always fresh
    }
}
```

**Pros**: Clean separation of concerns
**Cons**: Requires more extensive changes

## Recommended Investigation Steps

1. **Add Logging**: Add debug logging to `ComputedLispObject.getObj()` to confirm caching is the issue:
   ```java
   private LispObject getObj() {
       System.out.println("ComputedLispObject.getObj() called, cache=" + cache);
       // ... rest of method
   }
   ```

2. **Test Without Cache**: Temporarily disable caching in `ComputedLispObject` and verify all test cases pass.

3. **Identify Cache Usage**: Search codebase for intentional uses of `ComputedLispObject` caching to understand impact of removing it.

4. **Profile**: After fixing, profile to ensure no significant performance regression.

## Test Suite for Verification

After fixing, these tests should all pass:

```scheme
;; Test 1: Basic let in fold-left
(assert-equal
  (fold-left (lambda (acc x) (let ((v x)) (+ acc v))) 0 (list 1 2 3))
  6)

;; Test 2: let* with multiple bindings
(assert-equal
  (map (lambda (x) (let* ((a x) (b (* a 2))) b)) (list 1 2 3))
  (list 2 4 6))

;; Test 3: Nested let
(assert-equal
  (map (lambda (x)
         (let ((outer x))
           (let ((inner (* outer 2)))
             inner)))
       (list 1 2 3))
  (list 2 4 6))

;; Test 4: let with p-map operations
(assert-equal
  (p-map-get
    (fold-left
      (lambda (acc pair)
        (let ((k (car pair)) (v (cadr pair)))
          (p-map-assoc acc k v)))
      (p-map)
      (list (list "a" 1) (list "b" 2)))
    "b")
  2)

;; Test 5: Closure capturing let-bound variable
(define captured-fns
  (map (lambda (x)
         (let ((captured x))
           (lambda () captured)))
       (list 1 2 3)))
(assert-equal (map (lambda (f) (f)) captured-fns) (list 1 2 3))
```

## Workarounds

Until fixed, avoid these patterns:

### DON'T

```scheme
(define (process acc item)
  (let ((x (car item)))  ; x will be stale on subsequent calls
    (do-something x)))

(fold-left process init items)
```

### DO

```scheme
;; Option A: Use parameters directly without let
(define (process acc item)
  (do-something (car item)))

;; Option B: Use separate named functions for each call
(define (process-item-1) ...)
(define (process-item-2) ...)

;; Option C: Use recursion with fresh function calls instead of fold-left
(define (process-all items)
  (if (null? items)
      '()
      (cons (process-one (car items))
            (process-all (cdr items)))))
```

## Related Issues

- None known at this time

## References

- `LambdaForm.java`: Lambda compilation and parameter transformation
- `LetForm.java`: Let special form implementation
- `LetStarForm.java`: Let* special form implementation
- `ComputedLispObject.java`: Lazy-evaluated object wrapper
- `Evaluator.java`: Expression evaluation visitor
- `LispEnvironment.java`: Environment and stack management
