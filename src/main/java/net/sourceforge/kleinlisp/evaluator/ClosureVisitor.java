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

    for (AtomObject param : symbols) {
      if (closureInfo.containsKey(param)) {
        continue;
      }

      closureInfo.put(param, -1);
      closureSlotIndices.put(param, slotIndex++);
    }

    currentFunction.closureInfo = closureInfo;
    currentFunction.closureSlotIndices = closureSlotIndices;

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

    /**
     * Returns the slot index for a specific closure variable, or -1 if not found.
     */
    public int getClosureSlotIndex(AtomObject atom) {
      Integer index = closureSlotIndices.get(atom);
      return index != null ? index : -1;
    }

    /**
     * Returns the number of closure slots needed.
     */
    public int getClosureSlotCount() {
      return closureSlotIndices.size();
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
}
