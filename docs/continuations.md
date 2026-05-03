# Continuations in KleinLisp

This document is the canonical reference for the continuation implementation. Read it first when debugging continuation-related failures.

---

## Table of Contents

1. [What are Continuations?](#1-what-are-continuations)
2. [Architecture Overview](#2-architecture-overview)
3. [Phase 1 — Escape Continuations](#3-phase-1--escape-continuations)
4. [Phase 2 — Frame Holder Decoupling](#4-phase-2--frame-holder-decoupling)
5. [Phase 3 — CPS Evaluator](#5-phase-3--cps-evaluator)
6. [dynamic-wind](#6-dynamic-wind)
7. [Key Data Flow: call/cc End-to-End](#7-key-data-flow-callcc-end-to-end)
8. [Known Limitations](#8-known-limitations)
9. [Debugging Guide](#9-debugging-guide)
10. [File Index](#10-file-index)

---

## 1. What are Continuations?

A continuation represents **the rest of the computation** from a given point. In Scheme, `call/cc` (call-with-current-continuation) captures this "rest of the program" as a first-class procedure. Calling that procedure later resumes execution from the capture point with a new value.

```scheme
; Escape: k aborts the addition, returns 42 directly
(call/cc (lambda (k) (+ 1 (k 42))))  ; => 42

; Multi-shot: call k multiple times
(define saved #f)
(define r (+ 1 (call/cc (lambda (k) (set! saved k) 10))))  ; r = 11
(saved 20)  ; r becomes 21, execution continues from capture point
(saved 30)  ; r becomes 31
```

R7RS requires **unlimited-extent** multi-shot continuations: `k` can be stored, called any number of times, and called from any dynamic context—including after the originating `call/cc` has returned.

---

## 2. Architecture Overview

The implementation spans three phases, each building on the previous.

```
Evaluator.evaluate(expr)
        │
        ▼
  compileCps(expr)         ← Phase 3: CPS compilation
  returns CpsExpr
        │
        ▼
  CpsExpr.apply(k)         ← k is the top-level identity continuation
  returns TcoResult
        │
        ▼
  TcoResult.trampoline()   ← drives evaluation without growing the Java stack
  returns LispObject
        │
        ▼ (if ContinuationResumeException caught)
  resume captured cont     ← re-entry point for multi-shot continuations
```

### Core types

| Type | File | Role |
|------|------|------|
| `Continuation` | `evaluator/Continuation.java` | `@FunctionalInterface`: `TcoResult apply(LispObject value)` — "what to do with the result" |
| `CpsExpr` | `evaluator/CpsExpr.java` | `@FunctionalInterface`: `TcoResult apply(Continuation k)` — a compiled expression that accepts a continuation |
| `TcoResult` | `evaluator/TcoResult.java` | Trampoline step: `ofValue`, `ofContinuation`, `ofTailApply` |
| `ContinuationObject` | `objects/ContinuationObject.java` | First-class CPS continuation (Phase 3) |
| `EscapeContinuationObject` | `objects/EscapeContinuationObject.java` | Escape-only continuation (Phase 1, still used) |
| `WindFrame` | `evaluator/WindFrame.java` | One `dynamic-wind` frame: `(before, after, parent, depth)` |
| `WindManager` | `evaluator/WindManager.java` | Thread-local wind stack + `windTo` algorithm |
| `ContinuationException` | `evaluator/ContinuationException.java` | Thrown when Phase 1 escape continuation is invoked |
| `ContinuationResumeException` | `evaluator/ContinuationResumeException.java` | Thrown when CPS continuation is invoked from non-CPS context |

---

## 3. Phase 1 — Escape Continuations

**Files:** `ContinuationException.java`, `EscapeContinuationObject.java`, `ContinuationFunctions.java`

The original `call/cc` is a **downward-only** escape mechanism using Java exceptions.

```
call/cc captures a unique identity key
↓
EscapeContinuationObject wraps the key as a callable LispObject
↓
proc is called with the continuation object
↓
If proc calls the continuation:
  throws ContinuationException(key, value)    extends Error (bypasses RuntimeException catches)
↓
call/cc's try/catch catches it, checks the key, returns the value
```

### Why `extends Error` (not `RuntimeException`)?

`GuardForm` and other places catch `RuntimeException` to wrap them as Scheme errors. A `ContinuationException` must **bypass all of these** and propagate cleanly to the matching `call/cc` boundary. Since `Error` is not a subtype of `Exception`, no `catch (RuntimeException e)` or `catch (Exception e)` will intercept it.

### Critical fix in `LambdaFunction.evaluate()`

Before Phase 1, `stackPop()` was not in a `finally` block. If a continuation escape unwinds through a lambda, the environment stack would leak, causing `parameterAt()` to read the wrong frame in the calling lambda. Fixed by wrapping the trampoline in `try/finally { environment.stackPop(); }`.

---

## 4. Phase 2 — Frame Holder Decoupling

**Files:** `LambdaForm.java` (inner classes), `LispEnvironment.FunctionStack`, `DelayForm.java`, `DelayForceForm.java`

Before Phase 2, every parameter getter compiled into a lambda body called `environment.stackTop().parameterAt(i)` — a global side-channel that only works while the original lambda is on the stack.

### The `frameHolder` mechanism

Each `(lambda ...)` form creates one `FunctionStack[] frameHolder = new FunctionStack[1]` at **compile time**. This array is shared between:
- The compiled body's parameter getters (close over it)
- The `LambdaFunction` instance (updates it at call time)

```
Compile time:  frameHolder = new FunctionStack[1]   (one per lambda form)
               getter = () -> frameHolder[0].parameterAt(i)   (closes over array)
               
Call time:     LambdaFunction.evaluate(params):
                 stackPush(params, cenv)
                 savedFrame = frameHolder[0]
                 frameHolder[0] = environment.stackTop()     ← WRITE
                 try {
                   body.get()           ← body reads frameHolder[0]
                 } finally {
                   frameHolder[0] = savedFrame               ← RESTORE (safe for recursion)
                   stackPop()
                 }
```

### Recursive safety

Each call **saves and restores** `frameHolder[0]`. For a recursive `fact(n)`:
- Outer call: saves null, sets `frameHolder[0] = frame_A`
- Inner recursive call: saves frame_A, sets `frameHolder[0] = frame_B`
- Inner returns: restores `frameHolder[0] = frame_A`
- Outer continues using `frame_A` correctly

### TCO trampoline update

In the tail-call trampoline, when transferring to a different lambda `targetFunc`, the trampoline also sets `targetFunc.frameHolder[0] = environment.stackTop()` before calling `targetFunc.body.get()` directly (bypassing `targetFunc.evaluate()`).

### `FunctionStack.ownerFrameHolder` back-reference

`DelayForm` and `DelayForceForm` manually save and restore stack frames when a promise is forced. After Phase 2, they also need to update `frameHolder[0]`. The `FunctionStack` stores a reference to its owning lambda's `frameHolder` (set in `evaluate()` via `pushedFrame.setOwnerFrameHolder(frameHolder)`), so the delay forms can retrieve it:

```java
// When delay captures:
capturedFrameHolder = stackTop.getOwnerFrameHolder()

// When promise is forced:
environment.stackPush(finalParams, finalEnv)
savedFrameValue = capturedFrameHolder[0]
capturedFrameHolder[0] = environment.stackTop()    ← set to restored frame
try {
    exprSupplier.get()
} finally {
    environment.stackPop()
    capturedFrameHolder[0] = savedFrameValue        ← restore
}
```

---

## 5. Phase 3 — CPS Evaluator

**Files:** `Evaluator.java` (`compileCps`, `applyFunctionCps`, `compileCpsCallCc`, `compileCpsDynamicWind`), `ContinuationObject.java`, `ContinuationResumeException.java`, `TcoResult.java` (extended with `ofTailApply`)

### Entry point

`Evaluator.evaluate(LispObject expr)`:

```java
LispObject result = null;
Continuation topK = TcoResult::ofValue;       // identity: done when value arrives

TcoResult step = compileCps(transformed).apply(topK);
while (true) {
    try {
        result = step.trampoline();
        break;
    } catch (ContinuationResumeException e) {
        // A ContinuationObject was called from a non-CPS context (e.g. inside for-each).
        // Restore the dynamic extent and resume.
        WindManager.windTo(e.getCapturedWind());
        step = e.getContinuation().apply(e.getValue());
    }
}
return result;
```

### `TcoResult` variants

| Factory | Meaning |
|---------|---------|
| `ofValue(v)` | Evaluation is done; `v` is the result |
| `ofContinuation(Supplier<TcoResult>)` | Deferred step; trampoline calls it next |
| `ofTailApply(fn, params, k)` | Tail-call signal; applies `fn(params)` and then passes result to `k` |

### `compileCps` — CPS compilation

`compileCps(LispObject expr)` returns a `CpsExpr`. Forms handled natively in CPS:

| Form | CPS behaviour |
|------|---------------|
| **Atom / literal** | Look up or return directly, call `k` with value |
| **`begin`** | Sequence: compile each expr, each passes result to next via chained `k` |
| **`if`** | Evaluate test; branch `then` or `else` with the same outer `k` |
| **`and` / `or`** | Short-circuit via continuation threading |
| **`let` / `let*` / `letrec` / `letrec*`** | Push let env, evaluate bindings, evaluate body, pop env in continuation |
| **`call/cc`** | Capture current `k` as `ContinuationObject(k, currentWind)`, pass to proc |
| **`dynamic-wind`** | See §6 |
| **Function call** | Evaluate head + args left-to-right via continuation chain, then `applyFunctionCps` |
| **Tail function call** | Same but uses `ofTailApply` to signal TCO |

All other forms (including `define`, `set!`, `lambda`, `cond`, `case`, `guard`, etc.) are handled by the **legacy Supplier path** via `wrapOldForm()`:

```java
CpsExpr wrapOldForm(Supplier<LispObject> oldSupplier) {
    return k -> TcoResult.ofContinuation(() -> k.apply(oldSupplier.get()));
}
```

This means `call/cc` inside a `define` body currently uses the **Phase 1 escape semantics**, not Phase 3 CPS. See [§8 Known Limitations](#8-known-limitations).

### `ContinuationObject` — first-class CPS continuation

```java
public class ContinuationObject implements LispObject {
    private final Continuation cont;
    private final WindFrame capturedWind;   // dynamic-wind extent at capture time
    
    // CPS callers use this:
    public TcoResult jump(LispObject value) {
        WindManager.windTo(capturedWind);   // run exit/entry thunks
        return cont.apply(value);
    }
    
    // Non-CPS callers (map, for-each, etc.) use this via evaluate():
    public LispObject evaluate(LispObject[] params) {
        throw new ContinuationResumeException(cont, params[0], capturedWind);
    }
}
```

When called from **CPS context** (within `compileCps`-compiled code), `applyFunctionCps` detects `ContinuationObject` and calls `jump()` directly.

When called from **non-CPS context** (inside `map`, `for-each`, or a `define` body), `evaluate()` throws `ContinuationResumeException`. The top-level `Evaluator.evaluate()` retry loop catches it, runs `windTo`, and resumes.

### `applyFunctionCps` dispatch

```
Is it a ContinuationObject?
  YES → call jump(value) — direct CPS re-entry
  NO  → Is it a LambdaFunction?
          YES → call evaluateCps(params, k)
          NO  → call evaluate(params) (built-in), then k.apply(result)
```

`LambdaFunction.evaluateCps(params, k)` wraps `k` with frame cleanup (`frameHolder` restore + `stackPop`) and runs the CPS body.

---

## 6. dynamic-wind

**Files:** `WindFrame.java`, `WindManager.java`, `ContinuationFunctions.java`

### Data structures

`WindFrame` is an immutable singly-linked list node:
```
WindFrame { before, after, parent, depth }
```
`depth` enables O(1) comparison when finding the common ancestor of two extents.

`WindManager.CURRENT` is a `ThreadLocal<WindFrame>` — the current dynamic extent for this thread.

### `dynamic-wind` execution

```
before.evaluate([])
push new WindFrame(before, after, CURRENT) → CURRENT
try {
    thunk.evaluate([])
} finally {
    pop → CURRENT = frame.parent
    after.evaluate([])
}
```

The `finally` block ensures `after` runs on:
- Normal return
- `raise` / `error` (Lisp exceptions)
- `ContinuationException` escape (Phase 1, extends `Error`)
- `ContinuationResumeException` (Phase 3, extends `Error`)

### Continuation invocation with `windTo`

When a `ContinuationObject` is invoked, it must transition from the **current** dynamic extent to the **captured** extent:

```
windTo(target):
  ancestor = commonAncestor(CURRENT, target)
  
  // Unwind: run after-thunks from CURRENT up to ancestor (innermost first)
  for each frame from CURRENT to ancestor (exclusive):
      CURRENT = frame.parent
      frame.after.evaluate([])
  
  // Rewind: run before-thunks from ancestor down to target (outermost first)
  collect frames from target to ancestor
  for each frame outermost-first:
      frame.before.evaluate([])
      CURRENT = frame
```

This implements R7RS §6.10: the winding is correct even if `before`/`after` thunks themselves invoke continuations.

---

## 7. Key Data Flow: call/cc End-to-End

### Capture

```
Scheme: (call/cc (lambda (k) ... ))
         ↓
compileCpsCallCc():
  returns CpsExpr: λ(outerK) →
    capturedWind = WindManager.snapshot()
    cont = new ContinuationObject(outerK, capturedWind)
    compileCps(proc).apply(λ(procVal) →
      applyFunctionCps(procVal, [cont], outerK))
```

When the CPS trampoline runs this, `k` in the lambda receives a `ContinuationObject` wrapping the outer continuation `outerK`.

### Invocation from CPS context

```
(k 42) evaluated in CPS code
  ↓
compileCpsFunctionCall sees (k 42)
  ↓
evalCpsArgs evaluates k → ContinuationObject
  ↓
applyFunctionCps(contObj, [42], currentK):
  contObj is ContinuationObject → call contObj.jump(42)
    ↓
  WindManager.windTo(capturedWind)   ← runs after/before thunks
    ↓
  outerK.apply(42)                   ← returns TcoResult resuming at capture point
```

### Invocation from non-CPS context (e.g., inside `for-each`)

```
(for-each (lambda (x) (k x)) '(1 2 3))
          ↓ HigherOrderFunctions.forEachFn calls lambda.evaluate([item])
            ↓ lambda body evaluates (k x)
              ↓ k.asFunction().function().evaluate([x])
                ↓ ContinuationObject.evaluate([x])
                  ↓ throws ContinuationResumeException(outerK, x, capturedWind)
                    ↓ propagates as Error through for-each, LambdaFunction.evaluate()
                      ↓ caught by Evaluator.evaluate()'s retry loop
                        ↓ WindManager.windTo(capturedWind)
                          ↓ outerK.apply(x)
```

---

## 8. Known Limitations

### 8.1 Multi-shot within a single `define` body

```scheme
; This does NOT work — call/cc inside define uses Phase 1 escape semantics
(define result
  (let ((count 0))
    (define k #f)
    (let ((v (call/cc (lambda (c) (set! k c) 0))))
      (set! count (+ count 1))
      (if (< count 4) (k count) count))))   ; k is EscapeContinuationObject — fails on re-invoke
```

**Root cause:** `define` is compiled via `wrapOldForm()`, which uses the legacy `Supplier<LispObject>` path. `call/cc` inside that path dispatches to `ContinuationFunctions.callWithCurrentContinuation()` (Phase 1), producing an `EscapeContinuationObject`.

**Workaround:** Put `call/cc` at the top level of a `lisp.evaluate()` call, then re-invoke `k` in a separate `lisp.evaluate()` call:

```scheme
(define k #f)
(+ 1 (call/cc (lambda (c) (set! k c) 10)))   ; => 11  (ContinuationObject created)
(k 20)                                         ; => 21  (separate evaluate() call)
(k 30)                                         ; => 31
```

This works because each `lisp.evaluate()` call runs through `compileCps` and its retry loop.

### 8.2 `dynamic-wind` re-entry `before` thunk

When a continuation is invoked that re-enters a `dynamic-wind` thunk from outside, the `before` thunk fires correctly (via `windTo`). However, if a continuation is re-invoked multiple times alternating between different dynamic extents, the interleaving of `before`/`after` calls may differ from strict R7RS §6.10 in edge cases. Basic re-entry works correctly (verified by test and against Guile).

### 8.3 Continuations inside `map` / `for-each` are escape-only

Invoking a `ContinuationObject` from inside `map` or `for-each` triggers `ContinuationResumeException`. The retry loop in `Evaluator.evaluate()` catches it once and resumes the continuation. However, because `map`/`for-each` run in the legacy `Function.evaluate()` path, calling the continuation a **second time from within the same HOF callback** will throw again and may not be caught correctly. In practice: escape from `for-each` (first call) works; re-entrant use from HOF callbacks does not.

### 8.4 `evaluateTailCall` is dead code

`LambdaFunction.evaluateTailCall()` is never called by the TCO trampoline (which calls `targetFunc.body.get()` directly). It is safe to remove in a future cleanup.

---

## 9. Debugging Guide

### "ContinuationException" thrown and not caught

The `call/cc` that created the continuation is using Phase 1 (`EscapeContinuationObject`). This happens when `call/cc` is inside a `define` body or other legacy-compiled form. Check whether the code is in a context where `compileCps` handles it (top-level expression) or where `wrapOldForm` is used.

### "ContinuationResumeException" not caught

The `Evaluator.evaluate()` retry loop should always catch this. If it propagates out, it means the continuation was invoked from a context that created a NEW Java call stack not reachable from `Evaluator.evaluate()` — for example, from a separate thread or a Java callback.

### Stack corruption (wrong `parameterAt` values)

If parameter access returns wrong values after a continuation escape, the `frameHolder[0]` is stale. This happens if:
1. A lambda's `evaluate()` did not restore `frameHolder[0]` — check the `finally` block.
2. A `delay`/`delay-force` form captured the frame but didn't restore `ownerFrameHolder` — check `DelayForm.java`.
3. The TCO trampoline didn't update `targetFunc.frameHolder[0]` — check the trampoline loop.

### `NullPointerException` in `TcoOptimizer`

`TcoOptimizer.optimizeTailCall` receives `ListObject.NIL` as the `list` argument (e.g., from an `if` form with no else branch, or a `when` macro expansion). Fixed by adding `|| list == ListObject.NIL` to the null guard at the start of `optimizeTailCall`. If the NPE recurs, check that `list.car()` returns non-null before calling `.asAtom()`.

### `dynamic-wind` after-thunk not running

`ContinuationException` and `ContinuationResumeException` both extend `Error`. The `dynamic-wind` implementation uses `try/finally`, which runs on all `Throwable` including `Error`. If the after-thunk is not running, check whether a `catch (Throwable t)` somewhere is swallowing the exception without rethrowing.

---

## 10. File Index

```
src/main/java/net/sourceforge/kleinlisp/
├── evaluator/
│   ├── Continuation.java              Phase 3: @FunctionalInterface for continuations
│   ├── CpsExpr.java                   Phase 3: compiled CPS expression
│   ├── TcoResult.java                 Trampoline step (ofValue, ofContinuation, ofTailApply)
│   ├── ContinuationException.java     Phase 1: escape via Error subclass
│   ├── ContinuationResumeException.java  Phase 3: non-CPS resume via Error subclass
│   ├── WindFrame.java                 dynamic-wind frame node (immutable linked list)
│   ├── WindManager.java               Thread-local wind stack + windTo algorithm
│   └── Evaluator.java                 CPS compiler + trampoline entry point
├── objects/
│   ├── EscapeContinuationObject.java  Phase 1: escape-only continuation
│   └── ContinuationObject.java        Phase 3: first-class CPS continuation
├── special_forms/
│   ├── LambdaForm.java                LambdaFunction with frameHolder, evaluateCps
│   ├── DelayForm.java                 Captures + restores ownerFrameHolder
│   ├── DelayForceForm.java            Same as DelayForm
│   └── TcoOptimizer.java              TCO tail-call rewriter (NIL guard fix applied)
└── api/
    └── ContinuationFunctions.java     call/cc, dynamic-wind, with-exception-handler, continuation?

src/test/java/net/sourceforge/kleinlisp/
├── ContinuationTest.java              Unit tests (escape, multi-shot, dynamic-wind)
└── ContinuationGuileComparisonTest.java  Integration tests vs Guile/Chicken
```
