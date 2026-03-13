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
package net.sourceforge.kleinlisp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.kleinlisp.api.BooleanFunctions;
import net.sourceforge.kleinlisp.api.EqualityFunctions;
import net.sourceforge.kleinlisp.api.HigherOrderFunctions;
import net.sourceforge.kleinlisp.api.IOFunctions;
import net.sourceforge.kleinlisp.api.ListFunctions;
import net.sourceforge.kleinlisp.api.MathFunctions;
import net.sourceforge.kleinlisp.api.StringFunctions;
import net.sourceforge.kleinlisp.api.SymbolFunctions;
import net.sourceforge.kleinlisp.api.TypePredicates;
import net.sourceforge.kleinlisp.api.VectorFunctions;
import net.sourceforge.kleinlisp.evaluator.SourceRef;
import net.sourceforge.kleinlisp.macros.MacroDefinition;
import net.sourceforge.kleinlisp.macros.MacroExpander;
import net.sourceforge.kleinlisp.macros.StandardMacros;
import net.sourceforge.kleinlisp.objects.AtomFactory;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.IdentifierObject;

/**
 * @author Danilo Oliveira
 */
public class LispEnvironment implements Environment {

  public static class FunctionRef {

    private final String functionName;
    private final SourceRef ref;

    public FunctionRef(String functionName, SourceRef ref) {
      this.functionName = functionName;
      this.ref = ref;
    }

    public SourceRef getRef() {
      return ref;
    }

    public String getFunctionName() {
      return functionName;
    }

    @Override
    public String toString() {
      return String.format("%s[%d], at %s", ref.getSourceFile(), ref.getLine(), functionName);
    }
  }

  public static class FunctionStack {

    private LispObject[] parameters;
    private Environment env;

    public FunctionStack() {}

    public void set(LispObject[] parameters, Environment env) {
      this.parameters = parameters;
      this.env = env;
    }

    public final LispObject parameterAt(int i) {
      return parameters[i];
    }

    public void setParameterAt(int i, LispObject obj) {
      parameters[i] = obj;
    }

    public void setParameters(LispObject[] parameters) {
      this.parameters = parameters;
    }

    public LispObject[] getParameters() {
      return parameters;
    }

    public Environment getEnv() {
      return env;
    }
  }

  private final Map<AtomObject, LispObject> objects;
  private final Map<AtomObject, String> names;
  private final Map<AtomObject, MacroDefinition> macroTable;
  private final AtomFactory atomFactory;
  private final FunctionStack[] stack;
  private int stackPointer = 0;
  private final List<FunctionRef> functionCalls;
  private final List<Environment> letEnvStack;
  private int stackSize = 10000;
  private boolean trackStackTrace = true;

  // Version tracking for inline caching
  private final Map<AtomObject, Long> definitionVersions = new HashMap<>();
  private long globalVersion = 0;

  public LispEnvironment() {
    this.objects = new HashMap<>();
    this.names = new HashMap<>();
    this.atomFactory = new AtomFactory(this);
    this.stack = new FunctionStack[stackSize];
    for (int i = 0; i < stackSize; i++) {
      this.stack[i] = new FunctionStack();
    }
    this.functionCalls = new ArrayList<>();
    this.macroTable = new HashMap<>();
    this.letEnvStack = new ArrayList<>();
    initFunctionTable();
    initMacroTable();
  }

  public void addFuncCall(String funcName, SourceRef ref) {
    if (trackStackTrace) {
      functionCalls.add(new FunctionRef(funcName, ref));
    }
  }

  public void popFuncCall() {
    if (trackStackTrace) {
      functionCalls.remove(functionCalls.size() - 1);
    }
  }

  public void setTrackStackTrace(boolean track) {
    this.trackStackTrace = track;
  }

  public List<FunctionRef> getFunctionCalls() {
    return functionCalls;
  }

  public void printStackTrace() {
    for (FunctionRef ref : functionCalls) {
      System.out.println(">>" + ref);
    }
  }

  private void initFunctionTable() {
    // Math functions
    registerFunction("+", MathFunctions::add);
    registerFunction("-", MathFunctions::sub);
    registerFunction("*", MathFunctions::mul);
    registerFunction("/", MathFunctions::div);
    registerFunction("mod", MathFunctions::mod);
    registerFunction("modulo", MathFunctions::mod);
    registerFunction("remainder", MathFunctions::mod);
    registerFunction("abs", MathFunctions::abs);
    registerFunction("min", MathFunctions::min);
    registerFunction("max", MathFunctions::max);

    // Core list functions
    registerFunction("list", ListFunctions::list);
    registerFunction("length", ListFunctions::length);
    registerFunction("car", ListFunctions::car);
    registerFunction("cdr", ListFunctions::cdr);
    registerFunction("cons", ListFunctions::cons);
    registerFunction("null?", ListFunctions::isNull);

    // Additional list functions
    registerFunction("append", ListFunctions::append);
    registerFunction("reverse", (params) -> ListFunctions.reverse(params[0].asList()));
    registerFunction("member", ListFunctions::member);
    registerFunction("memq", ListFunctions::memq);
    registerFunction("memv", ListFunctions::memv);
    registerFunction("assoc", ListFunctions::assoc);
    registerFunction("assq", ListFunctions::assq);
    registerFunction("assv", ListFunctions::assv);
    registerFunction("list-ref", ListFunctions::listRef);
    registerFunction("list-tail", ListFunctions::listTail);
    registerFunction("last", ListFunctions::last);
    registerFunction("last-pair", ListFunctions::lastPair);
    registerFunction("butlast", ListFunctions::butlast);
    registerFunction("take", ListFunctions::take);
    registerFunction("drop", ListFunctions::drop);
    registerFunction("iota", ListFunctions::iota);

    // Car/cdr compositions
    registerFunction("caar", ListFunctions::caar);
    registerFunction("cadr", ListFunctions::cadr);
    registerFunction("cdar", ListFunctions::cdar);
    registerFunction("cddr", ListFunctions::cddr);
    registerFunction("caaar", ListFunctions::caaar);
    registerFunction("caadr", ListFunctions::caadr);
    registerFunction("cadar", ListFunctions::cadar);
    registerFunction("caddr", ListFunctions::caddr);
    registerFunction("cdaar", ListFunctions::cdaar);
    registerFunction("cdadr", ListFunctions::cdadr);
    registerFunction("cddar", ListFunctions::cddar);
    registerFunction("cdddr", ListFunctions::cdddr);
    registerFunction("cadddr", ListFunctions::cadddr);

    // Boolean/comparison functions
    registerFunction("<", BooleanFunctions::lt);
    registerFunction(">", BooleanFunctions::gt);
    registerFunction("<=", BooleanFunctions::le);
    registerFunction(">=", BooleanFunctions::ge);
    registerFunction("=", BooleanFunctions::eq);
    registerFunction("!=", BooleanFunctions::neq);
    registerFunction("not", BooleanFunctions::not);

    // Equality predicates
    registerFunction("eq?", EqualityFunctions::eq);
    registerFunction("eqv?", EqualityFunctions::eqv);
    registerFunction("equal?", EqualityFunctions::equal);

    // Type predicates
    registerFunction("string?", TypePredicates::isString);
    registerFunction("number?", TypePredicates::isNumber);
    registerFunction("integer?", TypePredicates::isInteger);
    registerFunction("real?", TypePredicates::isDouble);
    registerFunction("pair?", TypePredicates::isPair);
    registerFunction("list?", TypePredicates::isList);
    registerFunction("symbol?", TypePredicates::isSymbol);
    registerFunction("boolean?", TypePredicates::isBoolean);
    registerFunction("procedure?", TypePredicates::isProcedure);
    registerFunction("vector?", TypePredicates::isVector);
    registerFunction("zero?", TypePredicates::isZero);
    registerFunction("positive?", TypePredicates::isPositive);
    registerFunction("negative?", TypePredicates::isNegative);
    registerFunction("odd?", TypePredicates::isOdd);
    registerFunction("even?", TypePredicates::isEven);

    // String functions
    registerFunction("string-append", StringFunctions::stringAppend);
    registerFunction("string-length", StringFunctions::stringLength);
    registerFunction("string-ref", StringFunctions::stringRef);
    registerFunction("substring", StringFunctions::substring);
    registerFunction("string=?", StringFunctions::stringEqual);
    registerFunction("string<?", StringFunctions::stringLessThan);
    registerFunction("string>?", StringFunctions::stringGreaterThan);
    registerFunction("string<=?", StringFunctions::stringLessOrEqual);
    registerFunction("string>=?", StringFunctions::stringGreaterOrEqual);
    registerFunction("number->string", StringFunctions::numberToString);
    registerFunction("string->number", StringFunctions::stringToNumber);
    registerFunction("string-upcase", StringFunctions::stringUpcase);
    registerFunction("string-downcase", StringFunctions::stringDowncase);
    registerFunction("string-split", StringFunctions::stringSplit);
    registerFunction("string-join", StringFunctions::stringJoin);
    registerFunction("string-trim", StringFunctions::stringTrim);
    registerFunction("string-contains?", StringFunctions::stringContains);
    registerFunction("string-prefix?", StringFunctions::stringPrefix);
    registerFunction("string-suffix?", StringFunctions::stringSuffix);
    registerFunction("string-replace", StringFunctions::stringReplace);

    // Higher-order functions
    registerFunction("map", HigherOrderFunctions::map);
    registerFunction("filter", HigherOrderFunctions::filter);
    registerFunction("for-each", HigherOrderFunctions::forEach);
    registerFunction("fold-left", HigherOrderFunctions::foldLeft);
    registerFunction("fold-right", HigherOrderFunctions::foldRight);
    registerFunction("foldl", HigherOrderFunctions::foldLeft);
    registerFunction("foldr", HigherOrderFunctions::foldRight);
    registerFunction("reduce", HigherOrderFunctions::reduce);
    registerFunction("apply", HigherOrderFunctions::apply);
    registerFunction("compose", HigherOrderFunctions::compose);
    registerFunction("identity", HigherOrderFunctions::identity);
    registerFunction("negate", HigherOrderFunctions::negate);
    registerFunction("any", HigherOrderFunctions::any);
    registerFunction("all", HigherOrderFunctions::all);
    registerFunction("every", HigherOrderFunctions::all);

    // Vector functions
    registerFunction("make-vector", VectorFunctions::makeVector);
    registerFunction("vector", VectorFunctions::vector);
    registerFunction("vector-ref", VectorFunctions::vectorRef);
    registerFunction("vector-set!", VectorFunctions::vectorSet);
    registerFunction("vector-length", VectorFunctions::vectorLength);
    registerFunction("vector->list", VectorFunctions::vectorToList);
    registerFunction("list->vector", VectorFunctions::listToVector);
    registerFunction("vector-fill!", VectorFunctions::vectorFill);
    registerFunction("vector-copy", VectorFunctions::vectorCopy);

    // Symbol functions (require environment reference)
    SymbolFunctions symbolFuncs = new SymbolFunctions(this);
    registerFunction("symbol->string", symbolFuncs::symbolToString);
    registerFunction("string->symbol", symbolFuncs::stringToSymbol);
    registerFunction("gensym", symbolFuncs::gensym);

    // I/O functions
    registerFunction("print", IOFunctions::print);
    registerFunction("println", IOFunctions::println);
    registerFunction("display", IOFunctions::println);
    registerFunction("newline", IOFunctions::newline);
  }

  private void initMacroTable() {
    StandardMacros macros = new StandardMacros(this);
    registerMacro("when", macros::when);
    // Note: 'let' is now implemented as a special form for TCO compatibility
  }

  public void registerFunction(String symbol, net.sourceforge.kleinlisp.Function func) {
    AtomObject atom = atomOf(symbol);

    FunctionObject function = new FunctionObject(func);
    function.setIdentifier(new IdentifierObject(atom, "<java module>", -1, -1));

    set(atom, function);
  }

  public void registerMacro(String symbol, MacroDefinition macro) {
    macroTable.put(atomOf(symbol), macro);
  }

  public void registerMacro(AtomObject symbol, MacroDefinition macro) {
    macroTable.put(symbol, macro);
  }

  @Override
  public LispObject lookupValue(AtomObject atom) {
    // First check the let environment stack (innermost first)
    for (int i = letEnvStack.size() - 1; i >= 0; i--) {
      Environment letEnv = letEnvStack.get(i);
      LispObject value = letEnv.lookupValueOrNull(atom);
      if (value != null) {
        return value;
      }
    }
    // Fall back to global environment
    return objects.get(atom);
  }

  @Override
  public LispObject lookupValueOrNull(AtomObject atom) {
    // First check the let environment stack (innermost first)
    for (int i = letEnvStack.size() - 1; i >= 0; i--) {
      Environment letEnv = letEnvStack.get(i);
      LispObject value = letEnv.lookupValueOrNull(atom);
      if (value != null) {
        return value;
      }
    }
    // Fall back to global environment
    return objects.get(atom);
  }

  @Override
  public void set(AtomObject atom, LispObject obj) {
    objects.put(atom, obj);
    // Update version for inline cache invalidation
    definitionVersions.put(atom, ++globalVersion);
  }

  @Override
  public boolean exists(AtomObject atom) {
    return objects.containsKey(atom);
  }

  /**
   * Returns the version number for a specific atom's definition. Version changes when the atom is
   * redefined. Used by inline caching for cache invalidation.
   */
  public long getDefinitionVersion(AtomObject atom) {
    return definitionVersions.getOrDefault(atom, 0L);
  }

  public AtomObject atomOf(String atom) {
    AtomObject obj = atomFactory.newAtom(atom);
    names.put(obj, atom);
    return obj;
  }

  public String valueOf(AtomObject atom) {
    return names.get(atom);
  }

  public void stackPush(LispObject[] parameters, Environment env) {
    if (stackPointer == stackSize) {
      throw new StackOverflowError();
    }
    stack[stackPointer++].set(parameters, env);
  }

  public void stackPop() {
    stackPointer--;
  }

  public FunctionStack stackTop() {
    return stack[stackPointer - 1];
  }

  public boolean isStackEmpty() {
    return stackPointer == 0;
  }

  public LispObject expandMacros(LispObject obj) {
    return MacroExpander.expandMacro(macroTable, obj);
  }

  public void setStackTop(LispObject[] parameters) {
    stack[stackPointer - 1].setParameters(parameters);
  }

  public void setStackSize(int size) {
    this.stackSize = size;
  }

  public void pushLetEnv(Environment env) {
    letEnvStack.add(env);
  }

  public void popLetEnv() {
    letEnvStack.remove(letEnvStack.size() - 1);
  }
}
