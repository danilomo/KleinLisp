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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.kleinlisp.DefaultVisitor;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.IdentifierObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.special_forms.SpecialFormEnum;

/**
 * @author danilo
 */
public class ClosureVisitor extends DefaultVisitor {

  public static LispObject addClosureMeta(LispObject obj) {
    ClosureVisitor visitor = new ClosureVisitor();
    LispObject proc = obj.accept(visitor);

    collectIndirectUsedSymbols(visitor.currentFunction);
    collectClosureInfo(visitor.currentFunction);

    return proc;
  }

  private static void collectIndirectUsedSymbols(LambdaMeta currentFunction) {
    for (LambdaMeta func : currentFunction.children) {
      collectIndirectUsedSymbols(func);
    }

    for (LambdaMeta func : currentFunction.children) {
      for (AtomObject symbol : func.symbols) {
        if (currentFunction.parameters.contains(symbol)) {
          continue;
        }

        currentFunction.symbols.add(symbol);
      }
    }
  }

  private static void collectClosureInfo(LambdaMeta currentFunction) {

    if (currentFunction.children.isEmpty()) {
      return;
    }

    Set<AtomObject> symbols = new HashSet<>();

    for (LambdaMeta func : currentFunction.children) {
      symbols.addAll(func.getUsedSymbols());
    }

    Map<AtomObject, Integer> closureInfo = new HashMap<>();
    Map<AtomObject, Integer> closureSlotIndices = new HashMap<>();
    int slotIndex = 0;

    for (int i = 0; i < currentFunction.parameters.size(); i++) {
      AtomObject param = currentFunction.parameters.get(i);

      if (!symbols.contains(param)) {
        continue;
      }

      closureInfo.put(param, i);
      closureSlotIndices.put(param, slotIndex++);
    }

    // Handle rest parameter - use Integer.MAX_VALUE as special marker
    if (currentFunction.restParameter != null && symbols.contains(currentFunction.restParameter)) {
      // Store the regular parameter count in high bits, use MAX_VALUE marker in low
      // Actually, use (parameters.size() + 1) * -1 - 1 to encode both info
      // Or simpler: use negative value where abs(value) - 2 = regular param count
      // E.g., -2 means 0 regular params, -3 means 1 regular param, etc.
      int marker = -(currentFunction.parameters.size() + 2);
      closureInfo.put(currentFunction.restParameter, marker);
      closureSlotIndices.put(currentFunction.restParameter, slotIndex++);
    }

    Set<AtomObject> letBoundClosureVars = new HashSet<>();

    for (AtomObject param : symbols) {
      if (closureInfo.containsKey(param)) {
        continue;
      }

      closureInfo.put(param, -1);
      closureSlotIndices.put(param, slotIndex++);

      // Track if this is a let-bound variable in this function
      if (currentFunction.letBoundVars.contains(param)) {
        letBoundClosureVars.add(param);
      }
    }

    currentFunction.closureInfo = closureInfo;
    currentFunction.closureSlotIndices = closureSlotIndices;
    currentFunction.letBoundClosureVars = letBoundClosureVars;

    for (LambdaMeta func : currentFunction.children) {
      collectClosureInfo(func);
    }
  }

  public static class LambdaMeta {

    private List<AtomObject> parameters;
    private AtomObject restParameter; // For (a b . rest) syntax
    private Set<AtomObject> symbols = new HashSet<>();
    private List<LambdaMeta> children = new ArrayList<>();
    private LambdaMeta parent;
    private Map<AtomObject, Integer> closureInfo = Collections.emptyMap();
    private String repr;

    // Closure slot indices for O(1) indexed access
    private Map<AtomObject, Integer> closureSlotIndices = Collections.emptyMap();

    // Variables bound by let forms within this function (not closure variables for this function)
    Set<AtomObject> letBoundVars = new HashSet<>();

    // Variables in closureInfo that are let-bound (need special handling - captured at inner
    // lambda creation time, not at outer function entry time)
    private Set<AtomObject> letBoundClosureVars = Collections.emptySet();

    public LambdaMeta(List<AtomObject> parameters) {
      this.parameters = parameters;
    }

    public LambdaMeta(List<AtomObject> parameters, AtomObject restParameter) {
      this.parameters = parameters;
      this.restParameter = restParameter;
    }

    public AtomObject getRestParameter() {
      return restParameter;
    }

    public boolean hasRestParameter() {
      return restParameter != null;
    }

    public List<LambdaMeta> getChildren() {
      return children;
    }

    public List<AtomObject> getParameters() {
      return parameters;
    }

    public Set<AtomObject> getUsedSymbols() {
      return symbols;
    }

    public Map<AtomObject, Integer> getClosureInfo() {
      return closureInfo;
    }

    public LambdaMeta getParent() {
      return parent;
    }

    /**
     * Returns the slot index map for closure variables. Maps each captured variable to its slot
     * index in the IndexedEnvironment.
     */
    public Map<AtomObject, Integer> getClosureSlotIndices() {
      return closureSlotIndices;
    }

    /** Returns the slot index for a specific closure variable, or -1 if not found. */
    public int getClosureSlotIndex(AtomObject atom) {
      Integer index = closureSlotIndices.get(atom);
      return index != null ? index : -1;
    }

    /** Returns the number of closure slots needed. */
    public int getClosureSlotCount() {
      return closureSlotIndices.size();
    }

    /** Returns the set of closure variables that are let-bound in this function. */
    public Set<AtomObject> getLetBoundClosureVars() {
      return letBoundClosureVars;
    }

    /** Returns true if the given atom is a let-bound closure variable. */
    public boolean isLetBoundClosureVar(AtomObject atom) {
      return letBoundClosureVars.contains(atom);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("LambdaMeta{");
      sb.append("parameters=").append(parameters);
      sb.append(", symbols=").append(symbols);
      sb.append(", closureInfo=").append(closureInfo);
      sb.append(", closureSlots=").append(closureSlotIndices);
      sb.append('}');
      return sb.toString();
    }

    public String getRepr() {
      return repr;
    }
  }

  Set<AtomObject> definedSymbols = new HashSet<>();
  LambdaMeta currentFunction = new LambdaMeta(Collections.emptyList());

  @Override
  public LispObject visit(ListObject obj) {
    if (obj == ListObject.NIL) {
      return obj;
    }

    AtomObject head = obj.car().asAtom();

    if (head == null) {
      return super.visit(obj);
    }

    // Handle let forms - add bindings to definedSymbols
    if (head.specialForm() == SpecialFormEnum.LET) {
      return visitLetForm(obj);
    }

    if (head.specialForm() != SpecialFormEnum.LAMBDA) {
      return super.visit(obj);
    }

    List<AtomObject> parameters = new ArrayList<>();
    AtomObject restParameter = null;
    LispObject paramSpec = obj.cdr().car();

    // Handle two forms of variadic parameters:
    // 1. (lambda args body) - single identifier, all args become a list
    // 2. (lambda (a b . rest) body) - improper list with rest parameter
    if (paramSpec.asAtom() != null) {
      // Case 1: single identifier - all args become a list bound to this symbol
      restParameter = paramSpec.asAtom();
      definedSymbols.add(restParameter);
    } else {
      ListObject paramList = paramSpec.asList();

      // Check if param list is improper (has rest parameter)
      LispObject improperTail = paramList.getImproperTail();
      if (improperTail != null && improperTail.asAtom() != null) {
        restParameter = improperTail.asAtom();
        definedSymbols.add(restParameter);
      }

      for (LispObject param : paramList) {
        AtomObject atom = param.asAtom();
        parameters.add(atom);
        definedSymbols.add(atom);
      }
    }

    LambdaMeta newFunction = new LambdaMeta(parameters, restParameter);
    newFunction.repr = obj.toString();
    currentFunction.children.add(newFunction);
    newFunction.parent = currentFunction;
    LambdaMeta temp = currentFunction;
    currentFunction = newFunction;

    ListObject result = (ListObject) super.visit(obj);

    for (AtomObject param : currentFunction.parameters) {
      currentFunction.symbols.remove(param);
    }
    // Also remove rest parameter from symbols (it's a local parameter)
    if (currentFunction.getRestParameter() != null) {
      currentFunction.symbols.remove(currentFunction.getRestParameter());
    }

    result.setMeta(currentFunction);

    currentFunction = temp;

    return result;
  }

  @Override
  public LispObject visit(AtomObject obj) {
    if (!definedSymbols.contains(obj)) {
      return obj;
    }

    // Don't add let-bound variables of this function to symbols - they're local, not closures
    if (currentFunction.letBoundVars.contains(obj)) {
      return obj;
    }

    if (obj.specialForm() == SpecialFormEnum.NONE) {
      currentFunction.symbols.add(obj);
    }

    return obj;
  }

  @Override
  public LispObject visit(IdentifierObject obj) {
    LispObject result = obj.asAtom().accept(this);

    if (result.asAtom() != null) {
      return new IdentifierObject(result.asAtom(), obj.getSource(), obj.getLine(), obj.getCol());
    }

    return result;
  }

  /**
   * Handle let forms by adding bound variables to definedSymbols. This ensures that let-bound
   * variables are tracked as closure variables when captured by nested lambdas. The method properly
   * rebuilds the AST so that nested lambdas get their LambdaMeta set.
   *
   * <p>Important: Let-bound variables should only be considered closure variables for nested
   * lambdas, not for the containing function. We track this using letBoundInCurrentFunction.
   *
   * <p>Also handles named let: (let loop ((var init) ...) body...) where loop is a recursive
   * binding.
   */
  private LispObject visitLetForm(ListObject obj) {
    ListObject list = obj.cdr();
    LispObject first = list.car();

    // Check for named let: (let name ((var init) ...) body...)
    if (first.asAtom() != null) {
      return visitNamedLetForm(obj, first.asAtom(), list.cdr());
    }

    // Regular let form: (let ((var1 val1) (var2 val2) ...) body...)
    return visitRegularLetForm(obj, first, list.cdr());
  }

  /** Handle regular let: (let ((var1 val1) ...) body...) */
  private LispObject visitRegularLetForm(ListObject obj, LispObject bindingsObj, ListObject body) {
    // First, visit the binding value expressions (they shouldn't see the bound variables yet)
    // and collect the bound variable names
    List<AtomObject> boundVars = new ArrayList<>();
    List<LispObject> newBindings = new ArrayList<>();

    if (bindingsObj.asList() != null) {
      for (LispObject binding : bindingsObj.asList()) {
        ListObject tuple = binding.asList();
        if (tuple != null && tuple.length() >= 2) {
          AtomObject name = tuple.car().asAtom();
          LispObject valueExpr = tuple.cdr().car();

          // Visit the value expression before adding the variable to scope
          LispObject newValueExpr = valueExpr.accept(this);

          if (name != null) {
            boundVars.add(name);
          }

          // Rebuild the binding tuple
          newBindings.add(
              new ListObject(tuple.car(), new ListObject(newValueExpr, ListObject.NIL)));
        }
      }
    }

    // Add bound variables to definedSymbols (so nested lambdas can capture them)
    // Also track them in the current function's let-bound set
    for (AtomObject var : boundVars) {
      definedSymbols.add(var);
      currentFunction.letBoundVars.add(var);
    }

    // Visit the body with the bound variables in scope and rebuild it
    List<LispObject> newBody = new ArrayList<>();
    for (LispObject expr : body) {
      newBody.add(expr.accept(this));
    }

    // Rebuild the let form: (let bindings-list body...)
    ListObject newBindingsList = ListObject.NIL;
    for (int i = newBindings.size() - 1; i >= 0; i--) {
      newBindingsList = new ListObject(newBindings.get(i), newBindingsList);
    }

    ListObject newBodyList = ListObject.NIL;
    for (int i = newBody.size() - 1; i >= 0; i--) {
      newBodyList = new ListObject(newBody.get(i), newBodyList);
    }

    // Result: (let new-bindings-list new-body...)
    return new ListObject(obj.car(), new ListObject(newBindingsList, newBodyList));
  }

  /**
   * Handle named let: (let loop ((var1 init1) ...) body...) Transform to letrec: (letrec ((loop
   * (lambda (var1 ...) body...))) (loop init1 ...))
   *
   * <p>This transformation is done here so the lambda gets properly processed by ClosureVisitor.
   */
  private LispObject visitNamedLetForm(ListObject obj, AtomObject loopName, ListObject rest) {
    LispObject bindingsObj = rest.car();
    ListObject body = rest.cdr();

    // Get the LispEnvironment to create atoms
    LispEnvironment lisp = (LispEnvironment) loopName.environment();

    // Extract parameter names and initial value expressions from bindings
    List<LispObject> paramNames = new ArrayList<>();
    List<LispObject> initValues = new ArrayList<>();

    if (bindingsObj.asList() != null) {
      for (LispObject binding : bindingsObj.asList()) {
        ListObject tuple = binding.asList();
        if (tuple != null && tuple.length() >= 2) {
          paramNames.add(tuple.car());
          initValues.add(tuple.cdr().car());
        }
      }
    }

    // Build parameter list: (var1 var2 ...)
    ListObject paramList = ListObject.NIL;
    for (int i = paramNames.size() - 1; i >= 0; i--) {
      paramList = new ListObject(paramNames.get(i), paramList);
    }

    // Build lambda: (lambda (params...) body...)
    // body is already a ListObject of expressions
    AtomObject lambdaAtom = lisp.atomOf("lambda");
    ListObject lambdaExpr = new ListObject(lambdaAtom, new ListObject(paramList, body));

    // Build letrec binding: ((loop (lambda ...)))
    ListObject loopBinding = new ListObject(loopName, new ListObject(lambdaExpr, ListObject.NIL));
    ListObject letrecBindings = new ListObject(loopBinding, ListObject.NIL);

    // Build initial call: (loop init1 init2 ...)
    ListObject initList = ListObject.NIL;
    for (int i = initValues.size() - 1; i >= 0; i--) {
      initList = new ListObject(initValues.get(i), initList);
    }
    ListObject initialCall = new ListObject(loopName, initList);

    // Build letrec: (letrec ((loop (lambda ...))) (loop init...))
    AtomObject letrecAtom = lisp.atomOf("letrec");
    ListObject letrecExpr =
        new ListObject(
            letrecAtom,
            new ListObject(letrecBindings, new ListObject(initialCall, ListObject.NIL)));

    // Continue visiting the transformed expression - this will process the lambda properly
    return letrecExpr.accept(this);
  }
}
