/*
 * MIT License
 *
 * Copyright (c) 2018 Danilo Oliveira
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.sourceforge.kleinlisp.evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;
import net.sourceforge.kleinlisp.WrongTypeToApplyException;
import net.sourceforge.kleinlisp.objects.*;
import net.sourceforge.kleinlisp.special_forms.FormErrors;
import net.sourceforge.kleinlisp.special_forms.LambdaForm;
import net.sourceforge.kleinlisp.special_forms.SpecialForm;
import net.sourceforge.kleinlisp.special_forms.SpecialForms;

/**
 * @author danilo
 */
public class Evaluator implements LispVisitor<Supplier<LispObject>> {

  private final SpecialForms forms;
  private final LispEnvironment environment;
  private final AtomObject recur;
  private final AtomObject callCcAtom;
  private final AtomObject callWithCurrentContinuationAtom;
  private final AtomObject dynamicWindAtom;

  public Evaluator(LispEnvironment environment) {
    this.environment = environment;
    this.recur = environment.atomOf("recur");
    this.callCcAtom = environment.atomOf("call/cc");
    this.callWithCurrentContinuationAtom = environment.atomOf("call-with-current-continuation");
    this.dynamicWindAtom = environment.atomOf("dynamic-wind");
    this.forms = new SpecialForms(environment, this);
  }

  // ---------------------------------------------------------------------------
  // Top-level entry point — uses the CPS path so call/cc works everywhere
  // ---------------------------------------------------------------------------

  public LispObject evaluate(LispObject obj) {
    LispObject transformed = ClosureVisitor.addClosureMeta(environment.expandMacros(obj));
    // Wrap the initial compile+apply in ofContinuation so that ContinuationResumeException thrown
    // synchronously during the apply (e.g. inside a lambda body evaluated by the old Supplier path)
    // is caught by the same while-loop that handles exceptions from the trampoline.
    TcoResult result =
        TcoResult.ofContinuation(() -> compileCps(transformed).apply(TcoResult::ofValue));
    while (true) {
      try {
        return result.trampoline();
      } catch (ContinuationResumeException e) {
        // Transition the dynamic-wind extent: run after thunks for exited frames and before
        // thunks for frames being re-entered, then resume the continuation.
        WindManager.windTo(e.getCapturedWind());
        result = e.getContinuation().apply(e.getValue());
      }
    }
  }

  // ---------------------------------------------------------------------------
  // CPS compilation — converts a (transformed) LispObject into a CpsExpr
  // ---------------------------------------------------------------------------

  /**
   * Compile {@code obj} into a CPS expression. The returned {@link CpsExpr} accepts a continuation
   * and returns a {@link TcoResult} for the top-level trampoline.
   */
  public CpsExpr compileCps(LispObject obj) {
    if (obj instanceof ListObject) {
      return compileCpsList((ListObject) obj);
    }
    if (obj instanceof ComputedLispObject) {
      ComputedLispObject comp = (ComputedLispObject) obj;
      return k -> k.apply(comp.getComputed().get());
    }
    if (obj instanceof CellObject) {
      CellObject cell = (CellObject) obj;
      return k -> k.apply(cell.get());
    }
    if (obj instanceof IdentifierObject) {
      // IdentifierObject wraps an AtomObject with source info; delegate.
      return compileCps(((IdentifierObject) obj).asAtom());
    }
    if (obj instanceof AtomObject) {
      // Use the existing cached-lookup supplier for global variable access.
      Supplier<LispObject> lookup = new CachedFunctionSupplier(environment, (AtomObject) obj);
      return k -> k.apply(lookup.get());
    }
    // Self-evaluating: literals (int, bool, string, function objects, etc.)
    return k -> k.apply(obj);
  }

  // ---------------------------------------------------------------------------
  // CPS compilation of list forms
  // ---------------------------------------------------------------------------

  private CpsExpr compileCpsList(ListObject list) {
    if (list == ListObject.NIL) {
      return k -> k.apply(ListObject.NIL);
    }

    LispObject head = list.head();

    // --- (recur f) head: tail-call annotation — compile as tail-apply signal for TCO loop ---
    ListObject headAsList = head.asList();
    if (headAsList != null && headAsList.car().asAtom() == recur) {
      LispObject realHead = headAsList.cdr().car();
      return compileCpsTailCall(realHead, list.cdr());
    }

    // --- call/cc and dynamic-wind detected by atom name in head position ---
    AtomObject headAtom = head.asAtom();
    if (headAtom != null
        && (headAtom == callCcAtom || headAtom == callWithCurrentContinuationAtom)) {
      return compileCpsCallCc(list);
    }
    if (headAtom != null && headAtom == dynamicWindAtom) {
      return compileCpsDynamicWind(list);
    }

    // --- Special forms ---
    Optional<SpecialForm> form = forms.get(head);
    if (form.isPresent()) {
      return compileCpsSpecialForm(list, form.get(), headAtom);
    }

    // --- General function call ---
    validateFunctionPosition(head, list);
    return compileCpsFunctionCall(head, list.cdr());
  }

  /**
   * Dispatch CPS compilation for special forms. A handful of forms get proper CPS treatment so that
   * call/cc nested inside them works correctly. All others are wrapped: the old Supplier is called
   * at runtime and its result passed to the continuation.
   */
  private CpsExpr compileCpsSpecialForm(ListObject list, SpecialForm sf, AtomObject headAtom) {
    if (headAtom != null) {
      switch (headAtom.value()) {
        case "begin":
          return compileCpsBegin(list);
        case "if":
          return compileCpsIf(list);
        case "and":
          return compileCpsAnd(list);
        case "or":
          return compileCpsOr(list);
        case "let":
        case "let*":
        case "letrec":
        case "letrec*":
          return compileCpsLet(list, sf);
        default:
          break;
      }
    }
    // Fallback: wrap the old Supplier-based path.
    return wrapOldForm(list, sf);
  }

  /**
   * Wrap an old-style SpecialForm: evaluate it at runtime and pass result to k.
   *
   * <p>Runs the old {@link TailCallObject} trampoline after the form returns so that tail calls
   * annotated with {@code (recur ...)} inside wrapped let/letrec bodies complete fully rather than
   * leaking a {@link TailCallObject} into the CPS continuation as a raw value.
   */
  private static CpsExpr wrapOldForm(ListObject list, SpecialForm sf) {
    return k -> {
      LispObject result = sf.apply(list).get();
      while (result instanceof net.sourceforge.kleinlisp.objects.TailCallObject) {
        net.sourceforge.kleinlisp.objects.TailCallObject tco =
            (net.sourceforge.kleinlisp.objects.TailCallObject) result;
        result = tco.getFunction().evaluate(tco.getParameters());
      }
      return k.apply(result);
    };
  }

  // ---------------------------------------------------------------------------
  // CPS implementations of common special forms
  // ---------------------------------------------------------------------------

  private CpsExpr compileCpsBegin(ListObject list) {
    List<CpsExpr> exprs = new ArrayList<>();
    for (LispObject expr : list.cdr()) {
      exprs.add(compileCps(expr));
    }
    if (exprs.isEmpty()) {
      return k -> k.apply(VoidObject.VOID);
    }
    return makeSequenceCps(exprs);
  }

  private CpsExpr compileCpsIf(ListObject list) {
    FormErrors.assertArgRange("if", list, 2, 3);
    ListObject params = list.cdr();
    CpsExpr cpsCond = compileCps(params.car());
    CpsExpr cpsThen = compileCps(params.cdr().car());
    ListObject elseTail = params.cdr().cdr();
    CpsExpr cpsElse =
        (elseTail != ListObject.NIL) ? compileCps(elseTail.car()) : k -> k.apply(ListObject.NIL);

    return k ->
        cpsCond.apply(
            condVal -> {
              if (condVal.truthiness()) {
                return TcoResult.ofContinuation(() -> cpsThen.apply(k));
              } else {
                return TcoResult.ofContinuation(() -> cpsElse.apply(k));
              }
            });
  }

  private CpsExpr compileCpsAnd(ListObject list) {
    List<CpsExpr> exprs = new ArrayList<>();
    for (LispObject expr : list.cdr()) {
      exprs.add(compileCps(expr));
    }
    if (exprs.isEmpty()) {
      return k -> k.apply(BooleanObject.TRUE);
    }
    return k -> evalAndCps(exprs, 0, k);
  }

  private static TcoResult evalAndCps(List<CpsExpr> exprs, int idx, Continuation k) {
    if (idx == exprs.size() - 1) {
      return exprs.get(idx).apply(k);
    }
    return exprs
        .get(idx)
        .apply(
            v -> {
              if (!v.truthiness()) {
                return TcoResult.ofContinuation(() -> k.apply(v));
              }
              return TcoResult.ofContinuation(() -> evalAndCps(exprs, idx + 1, k));
            });
  }

  private CpsExpr compileCpsOr(ListObject list) {
    List<CpsExpr> exprs = new ArrayList<>();
    for (LispObject expr : list.cdr()) {
      exprs.add(compileCps(expr));
    }
    if (exprs.isEmpty()) {
      return k -> k.apply(BooleanObject.FALSE);
    }
    return k -> evalOrCps(exprs, 0, k);
  }

  private static TcoResult evalOrCps(List<CpsExpr> exprs, int idx, Continuation k) {
    if (idx == exprs.size() - 1) {
      return exprs.get(idx).apply(k);
    }
    return exprs
        .get(idx)
        .apply(
            v -> {
              if (v.truthiness()) {
                return TcoResult.ofContinuation(() -> k.apply(v));
              }
              return TcoResult.ofContinuation(() -> evalOrCps(exprs, idx + 1, k));
            });
  }

  /**
   * CPS let / let* / letrec / letrec*: wrap the old form. The old form correctly pushes/pops the
   * let environment. Inner call/cc expressions in binding values and bodies are handled via CPS
   * because the old form calls evaluator.evaluate() recursively (which now uses CPS). This covers
   * the most common cases while avoiding reimplementing the letrec* initialization order.
   */
  private CpsExpr compileCpsLet(ListObject list, SpecialForm sf) {
    return wrapOldForm(list, sf);
  }

  // ---------------------------------------------------------------------------
  // CPS call/cc
  // ---------------------------------------------------------------------------

  private CpsExpr compileCpsCallCc(ListObject list) {
    // (call/cc proc) — proc receives a ContinuationObject wrapping the current k
    LispObject procExpr = list.cdr().car();
    CpsExpr cpsProc = compileCps(procExpr);
    return k ->
        cpsProc.apply(
            procValue -> {
              ContinuationObject cont = new ContinuationObject(k);
              return applyFunctionCps(procValue, new LispObject[] {cont}, k);
            });
  }

  // ---------------------------------------------------------------------------
  // CPS function application
  // ---------------------------------------------------------------------------

  /** Compile a regular function call in CPS: head → args left-to-right → apply. */
  private CpsExpr compileCpsFunctionCall(LispObject headExpr, ListObject argsListObj) {
    CpsExpr cpsHead = compileCps(headExpr);
    List<CpsExpr> cpsArgs = new ArrayList<>();
    for (LispObject arg : argsListObj) {
      cpsArgs.add(compileCps(arg));
    }
    return k ->
        cpsHead.apply(
            fv -> {
              LispObject[] args = new LispObject[cpsArgs.size()];
              return evalCpsArgs(cpsArgs, 0, args, fv, k);
            });
  }

  /**
   * Compile a tail call: like {@link #compileCpsFunctionCall} but produces {@link
   * TcoResult#ofTailApply} so the {@code evaluateCps} TCO loop can reuse the stack frame.
   */
  private CpsExpr compileCpsTailCall(LispObject headExpr, ListObject argsListObj) {
    CpsExpr cpsHead = compileCps(headExpr);
    List<CpsExpr> cpsArgs = new ArrayList<>();
    for (LispObject arg : argsListObj) {
      cpsArgs.add(compileCps(arg));
    }
    return k ->
        cpsHead.apply(
            fv -> {
              LispObject[] args = new LispObject[cpsArgs.size()];
              return evalCpsTailArgs(cpsArgs, 0, args, fv, k);
            });
  }

  private TcoResult evalCpsArgs(
      List<CpsExpr> cpsArgs, int idx, LispObject[] evaluated, LispObject fn, Continuation k) {
    if (idx == cpsArgs.size()) {
      return applyFunctionCps(fn, evaluated, k);
    }
    return cpsArgs
        .get(idx)
        .apply(
            v -> {
              evaluated[idx] = v;
              return TcoResult.ofContinuation(
                  () -> evalCpsArgs(cpsArgs, idx + 1, evaluated, fn, k));
            });
  }

  /** Like {@link #evalCpsArgs} but produces {@link TcoResult#ofTailApply} at the leaf. */
  private TcoResult evalCpsTailArgs(
      List<CpsExpr> cpsArgs, int idx, LispObject[] evaluated, LispObject fn, Continuation k) {
    if (idx == cpsArgs.size()) {
      // Continuation objects jump immediately; everything else is a tail-apply signal.
      if (fn instanceof ContinuationObject) {
        LispObject value = evaluated.length > 0 ? evaluated[0] : ListObject.NIL;
        return ((ContinuationObject) fn).jump(value);
      }
      FunctionObject fnObj = fn.asFunction();
      if (fnObj != null && fnObj.function() instanceof LambdaForm.LambdaFunction) {
        return TcoResult.ofTailApply(fn, evaluated, k);
      }
      // Built-in in tail position: just apply and pass to k
      return applyFunctionCps(fn, evaluated, k);
    }
    return cpsArgs
        .get(idx)
        .apply(
            v -> {
              evaluated[idx] = v;
              return TcoResult.ofContinuation(
                  () -> evalCpsTailArgs(cpsArgs, idx + 1, evaluated, fn, k));
            });
  }

  /**
   * Apply a function in CPS mode.
   *
   * <ul>
   *   <li>{@link ContinuationObject}: calls {@link ContinuationObject#jump} (runs windTo + returns
   *       the captured continuation's TcoResult, bypassing the outer {@code k}).
   *   <li>{@link LambdaForm.LambdaFunction}: uses {@link LambdaForm.LambdaFunction#evaluateCps} so
   *       that {@code call/cc} inside lambda bodies captures proper CPS continuations. The TCO loop
   *       in {@code evaluateCps} handles tail calls without growing the LispEnvironment stack.
   *   <li>Built-in: calls {@code Function.evaluate()} synchronously and passes the result to {@code
   *       k}.
   * </ul>
   */
  TcoResult applyFunctionCps(LispObject fnObj, LispObject[] params, Continuation k) {
    if (fnObj instanceof ContinuationObject) {
      LispObject value = params.length > 0 ? params[0] : ListObject.NIL;
      return ((ContinuationObject) fnObj).jump(value);
    }

    FunctionObject functionObj = fnObj.asFunction();
    if (functionObj == null) {
      throw new WrongTypeToApplyException(fnObj);
    }

    net.sourceforge.kleinlisp.Function function = functionObj.function();

    if (function instanceof LambdaForm.LambdaFunction) {
      LambdaForm.LambdaFunction lambdaFn = (LambdaForm.LambdaFunction) function;
      environment.addFuncCall(functionObj.functionName(), null);
      Continuation kWithPop =
          v -> {
            environment.popFuncCall();
            return k.apply(v);
          };
      return lambdaFn.evaluateCps(params, kWithPop);
    }

    // Built-in: call synchronously, pass result to k
    LispObject result = function.evaluate(params);
    return k.apply(result);
  }

  // ---------------------------------------------------------------------------
  // CPS dynamic-wind (full R7RS §6.10 semantics)
  // ---------------------------------------------------------------------------

  private CpsExpr compileCpsDynamicWind(ListObject list) {
    FormErrors.assertArgRange("dynamic-wind", list, 3, 3);
    ListObject args = list.cdr();
    CpsExpr cpsBeforeExpr = compileCps(args.car());
    CpsExpr cpsThunkExpr = compileCps(args.cdr().car());
    CpsExpr cpsAfterExpr = compileCps(args.cdr().cdr().car());

    return k ->
        cpsBeforeExpr.apply(
            beforeFn ->
                cpsThunkExpr.apply(
                    thunkFn ->
                        cpsAfterExpr.apply(
                            afterFn -> {
                              FunctionObject beforeFnObj = beforeFn.asFunction();
                              FunctionObject afterFnObj = afterFn.asFunction();
                              if (beforeFnObj == null
                                  || thunkFn.asFunction() == null
                                  || afterFnObj == null) {
                                throw new net.sourceforge.kleinlisp.LispArgumentError(
                                    "dynamic-wind: all three arguments must be procedures");
                              }

                              WindFrame parent = WindManager.snapshot();
                              WindFrame frame = new WindFrame(beforeFnObj, afterFnObj, parent);

                              // Apply before in CPS, then push frame and run thunk
                              return applyFunctionCps(
                                  beforeFn,
                                  new LispObject[0],
                                  ignoredBeforeResult -> {
                                    WindManager.push(frame);

                                    // kAfterThunk: pop wind frame & run after when thunk is done
                                    Continuation kAfterThunk =
                                        thunkResult -> {
                                          if (WindManager.snapshot() == frame) {
                                            WindManager.pop(parent);
                                            return applyFunctionCps(
                                                afterFn,
                                                new LispObject[0],
                                                ignoredAfterResult -> k.apply(thunkResult));
                                          }
                                          return k.apply(thunkResult);
                                        };

                                    return applyFunctionCps(
                                        thunkFn, new LispObject[0], kAfterThunk);
                                  });
                            })));
  }

  // ---------------------------------------------------------------------------
  // Sequence helper (used by begin, lambda body, and)
  // ---------------------------------------------------------------------------

  /**
   * Build a CPS expression that evaluates each expression in {@code exprs} in order, discarding all
   * intermediate values, and passes the final value to the continuation.
   */
  public static CpsExpr makeSequenceCps(List<CpsExpr> exprs) {
    if (exprs.isEmpty()) {
      return k -> k.apply(VoidObject.VOID);
    }
    if (exprs.size() == 1) {
      return exprs.get(0);
    }
    return k -> runCpsSeq(exprs, 0, k);
  }

  private static TcoResult runCpsSeq(List<CpsExpr> exprs, int idx, Continuation k) {
    if (idx == exprs.size() - 1) {
      return exprs.get(idx).apply(k);
    }
    return exprs.get(idx).apply(v -> TcoResult.ofContinuation(() -> runCpsSeq(exprs, idx + 1, k)));
  }

  // ---------------------------------------------------------------------------
  // Legacy Supplier<LispObject> visitor — kept for internal use by wrapped forms
  // ---------------------------------------------------------------------------

  @Override
  public Supplier<LispObject> visit(AtomObject obj) {
    return new CachedFunctionSupplier(environment, obj);
  }

  @Override
  public Supplier<LispObject> visit(BooleanObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(DoubleObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(IntObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(JavaObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(ListObject list) {
    if (list == ListObject.NIL) {
      return () -> ListObject.NIL;
    }

    LispObject head = list.head();

    if (head.asList() != null && head.asList().car().asAtom() == recur) {
      return tailCall(list);
    }

    Optional<SpecialForm> form = forms.get(head);

    if (form.isPresent()) {
      return form.get().apply(list);
    }

    // Try fast path for common arithmetic/comparison operations
    Supplier<LispObject> fastPath = CommonCases.apply(list, this);
    if (fastPath != null) {
      return fastPath;
    }

    // Validate that head can be a function - catch errors at compile time
    validateFunctionPosition(head, list);

    Supplier<LispObject> headEval = head.accept(this);
    SourceRef ref = getSourceRef(list);

    // Use specialized FunctionCall based on arity
    int paramCount = list.cdr().length();

    switch (paramCount) {
      case 1:
        {
          Supplier<LispObject> p0 = list.cdr().car().accept(this);
          return new FunctionCall1(environment, ref, headEval, p0);
        }
      case 2:
        {
          Supplier<LispObject> p0 = list.cdr().car().accept(this);
          Supplier<LispObject> p1 = list.cdr().cdr().car().accept(this);
          return new FunctionCall2(environment, ref, headEval, p0, p1);
        }
      case 3:
        {
          Supplier<LispObject> p0 = list.cdr().car().accept(this);
          Supplier<LispObject> p1 = list.cdr().cdr().car().accept(this);
          Supplier<LispObject> p2 = list.cdr().cdr().cdr().car().accept(this);
          return new FunctionCall3(environment, ref, headEval, p0, p1, p2);
        }
      default:
        {
          List<Supplier<LispObject>> parameters = new ArrayList<>();
          for (LispObject obj : list.cdr()) {
            parameters.add(obj.accept(this));
          }
          return new FunctionCall(environment, ref, headEval, parameters);
        }
    }
  }

  public Supplier<LispObject> tailCall(ListObject list) {
    ListObject recur = list.head().asList();
    LispObject head = recur.cdr().car();

    Supplier<LispObject> headEval = head.accept(this);

    int paramCount = list.cdr().length();

    switch (paramCount) {
      case 1:
        {
          Supplier<LispObject> p0 = list.cdr().car().accept(this);
          return new TailFunctionCall1(headEval, p0);
        }
      case 2:
        {
          Supplier<LispObject> p0 = list.cdr().car().accept(this);
          Supplier<LispObject> p1 = list.cdr().cdr().car().accept(this);
          return new TailFunctionCall2(headEval, p0, p1);
        }
      case 3:
        {
          Supplier<LispObject> p0 = list.cdr().car().accept(this);
          Supplier<LispObject> p1 = list.cdr().cdr().car().accept(this);
          Supplier<LispObject> p2 = list.cdr().cdr().cdr().car().accept(this);
          return new TailFunctionCall3(headEval, p0, p1, p2);
        }
      default:
        {
          List<Supplier<LispObject>> parameters = new ArrayList<>();
          for (LispObject obj : list.cdr()) {
            parameters.add(obj.accept(this));
          }
          return new TailFunctionCall(environment, headEval, parameters);
        }
    }
  }

  private void validateFunctionPosition(LispObject head, ListObject list) {
    if (head.asInt() != null
        || head.asDouble() != null
        || head.asString() != null
        || head instanceof BooleanObject
        || head instanceof VectorObject) {
      throw new WrongTypeToApplyException(head);
    }
  }

  private SourceRef getSourceRef(ListObject list) {
    if (list == ListObject.NIL) {
      return null;
    }

    LispObject car = list.car();

    if (car.asIdentifier() != null) {
      return asSourceRef(car.asIdentifier());
    }

    if (car.asList() != null) {
      SourceRef result = getSourceRef(car.asList());
      if (result != null) {
        return result;
      }
    }

    return getSourceRef(list.cdr());
  }

  private SourceRef asSourceRef(IdentifierObject id) {
    return new SourceRef(id.getSource(), id.getLine());
  }

  @Override
  public Supplier<LispObject> visit(StringObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(MutableStringObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(FunctionObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(ErrorObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(VoidObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(ComputedLispObject obj) {
    return obj.getComputed();
  }

  @Override
  public Supplier<LispObject> visit(CellObject obj) {
    return () -> obj.get();
  }

  @Override
  public Supplier<LispObject> visit(IdentifierObject obj) {
    return obj.asAtom().accept(this);
  }

  @Override
  public Supplier<LispObject> visit(VectorObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(KeywordObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(PVectorObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(PMapObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(PSetObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(LazySeqObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(CharObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(ValuesObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(PromiseObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(ParameterObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(PortObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(EofObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(BytevectorObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(RecordTypeObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(RecordObject obj) {
    return () -> obj;
  }

  @Override
  public Supplier<LispObject> visit(EnvironmentObject obj) {
    return () -> obj;
  }
}
