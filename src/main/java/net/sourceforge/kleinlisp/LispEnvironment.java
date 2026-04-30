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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.kleinlisp.api.BooleanFunctions;
import net.sourceforge.kleinlisp.api.BytevectorFunctions;
import net.sourceforge.kleinlisp.api.CharFunctions;
import net.sourceforge.kleinlisp.api.EqualityFunctions;
import net.sourceforge.kleinlisp.api.ExceptionFunctions;
import net.sourceforge.kleinlisp.api.FileFunctions;
import net.sourceforge.kleinlisp.api.HigherOrderFunctions;
import net.sourceforge.kleinlisp.api.IOFunctions;
import net.sourceforge.kleinlisp.api.IntrospectionFunctions;
import net.sourceforge.kleinlisp.api.JsonFunctions;
import net.sourceforge.kleinlisp.api.ListFunctions;
import net.sourceforge.kleinlisp.api.MathFunctions;
import net.sourceforge.kleinlisp.api.ParameterFunctions;
import net.sourceforge.kleinlisp.api.PersistentCollectionFunctions;
import net.sourceforge.kleinlisp.api.PersistentMapFunctions;
import net.sourceforge.kleinlisp.api.PersistentSetFunctions;
import net.sourceforge.kleinlisp.api.PersistentVectorFunctions;
import net.sourceforge.kleinlisp.api.PortFunctions;
import net.sourceforge.kleinlisp.api.PromiseFunctions;
import net.sourceforge.kleinlisp.api.ReadWriteFunctions;
import net.sourceforge.kleinlisp.api.SeqFunctions;
import net.sourceforge.kleinlisp.api.StringFunctions;
import net.sourceforge.kleinlisp.api.SymbolFunctions;
import net.sourceforge.kleinlisp.api.SystemFunctions;
import net.sourceforge.kleinlisp.api.TimeFunctions;
import net.sourceforge.kleinlisp.api.TypePredicates;
import net.sourceforge.kleinlisp.api.ValuesFunctions;
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

  // Reference to Lisp instance for accessing configuration (e.g., r7rsMode)
  private Lisp lispInstance;

  // R7RS library registry
  private final LibraryRegistry libraryRegistry;

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
    this.libraryRegistry = new LibraryRegistry();
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

  /**
   * Returns the Lisp instance associated with this environment.
   *
   * @return the Lisp instance, or null if not set
   */
  public Lisp getLispInstance() {
    return lispInstance;
  }

  /**
   * Returns the library registry for R7RS library management.
   *
   * @return the library registry
   */
  public LibraryRegistry getLibraryRegistry() {
    return libraryRegistry;
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
    registerFunction("abs", MathFunctions::abs);
    registerFunction("min", MathFunctions::min);
    registerFunction("max", MathFunctions::max);

    // Rounding operations
    registerFunction("floor", MathFunctions::floor);
    registerFunction("ceiling", MathFunctions::ceiling);
    registerFunction("truncate", MathFunctions::truncate);
    registerFunction("round", MathFunctions::round);

    // Integer division
    registerFunction("quotient", MathFunctions::quotient);
    registerFunction("remainder", MathFunctions::remainder);
    registerFunction("modulo", MathFunctions::modulo);
    registerFunction("floor/", MathFunctions::floorDiv);
    registerFunction("floor-quotient", MathFunctions::floorQuotient);
    registerFunction("floor-remainder", MathFunctions::floorRemainder);
    registerFunction("truncate/", MathFunctions::truncateDiv);
    registerFunction("truncate-quotient", MathFunctions::truncateQuotient);
    registerFunction("truncate-remainder", MathFunctions::truncateRemainder);

    // GCD and LCM
    registerFunction("gcd", MathFunctions::gcdFn);
    registerFunction("lcm", MathFunctions::lcmFn);

    // Exactness predicates and conversions
    registerFunction("exact?", MathFunctions::isExact);
    registerFunction("inexact?", MathFunctions::isInexact);
    registerFunction("exact", MathFunctions::exact);
    registerFunction("inexact", MathFunctions::inexact);

    // Transcendental functions
    registerFunction("sqrt", MathFunctions::sqrt);
    registerFunction("expt", MathFunctions::expt);
    registerFunction("exp", MathFunctions::exp);
    registerFunction("log", MathFunctions::log);
    registerFunction("sin", MathFunctions::sin);
    registerFunction("cos", MathFunctions::cos);
    registerFunction("tan", MathFunctions::tan);
    registerFunction("asin", MathFunctions::asin);
    registerFunction("acos", MathFunctions::acos);
    registerFunction("atan", MathFunctions::atan);

    // Additional numeric functions
    registerFunction("square", MathFunctions::square);
    registerFunction("exact-integer-sqrt", MathFunctions::exactIntegerSqrt);

    // Rational functions (simplified)
    registerFunction("numerator", MathFunctions::numerator);
    registerFunction("denominator", MathFunctions::denominator);
    registerFunction("rationalize", MathFunctions::rationalize);

    // Numeric predicates
    registerFunction("integer?", MathFunctions::isInteger);
    registerFunction("rational?", MathFunctions::isRational);
    registerFunction("real?", MathFunctions::isReal);
    registerFunction("complex?", MathFunctions::isComplex);
    registerFunction("finite?", MathFunctions::isFinite);
    registerFunction("infinite?", MathFunctions::isInfinite);
    registerFunction("nan?", MathFunctions::isNan);

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
    registerFunction("assoc-ref", ListFunctions::assocRef);
    registerFunction("list-ref", ListFunctions::listRef);
    registerFunction("nth", ListFunctions::nth);
    registerFunction("list-tail", ListFunctions::listTail);
    registerFunction("last", ListFunctions::last);
    registerFunction("last-pair", ListFunctions::lastPair);
    registerFunction("butlast", ListFunctions::butlast);
    registerFunction("take", ListFunctions::take);
    registerFunction("drop", ListFunctions::drop);
    registerFunction("iota", ListFunctions::iota);
    registerFunction("list-copy", ListFunctions::listCopy);
    registerFunction("set-car!", ListFunctions::setCar);
    registerFunction("set-cdr!", ListFunctions::setCdr);
    registerFunction("list-set!", ListFunctions::listSet);
    registerFunction("make-list", ListFunctions::makeList);

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
    registerFunction("caaaar", ListFunctions::caaaar);
    registerFunction("caaadr", ListFunctions::caaadr);
    registerFunction("caadar", ListFunctions::caadar);
    registerFunction("caaddr", ListFunctions::caaddr);
    registerFunction("cadaar", ListFunctions::cadaar);
    registerFunction("cadadr", ListFunctions::cadadr);
    registerFunction("caddar", ListFunctions::caddar);
    registerFunction("cdaaar", ListFunctions::cdaaar);
    registerFunction("cdaadr", ListFunctions::cdaadr);
    registerFunction("cdadar", ListFunctions::cdadar);
    registerFunction("cdaddr", ListFunctions::cdaddr);
    registerFunction("cddaar", ListFunctions::cddaar);
    registerFunction("cddadr", ListFunctions::cddadr);
    registerFunction("cdddar", ListFunctions::cdddar);
    registerFunction("cddddr", ListFunctions::cddddr);

    // Boolean/comparison functions
    registerFunction("<", BooleanFunctions::lt);
    registerFunction(">", BooleanFunctions::gt);
    registerFunction("<=", BooleanFunctions::le);
    registerFunction(">=", BooleanFunctions::ge);
    registerFunction("=", BooleanFunctions::eq);
    registerFunction("!=", BooleanFunctions::neq);
    registerFunction("not", BooleanFunctions::not);
    registerFunction("boolean=?", BooleanFunctions::booleanEqual);

    // Equality predicates
    registerFunction("eq?", EqualityFunctions::eq);
    registerFunction("eqv?", EqualityFunctions::eqv);
    registerFunction("equal?", EqualityFunctions::equal);

    // Type predicates
    registerFunction("string?", TypePredicates::isString);
    registerFunction("number?", TypePredicates::isNumber);
    // Note: integer? and real? are registered in numeric tower section above
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
    registerFunction("string-ci=?", StringFunctions::stringCiEqual);
    registerFunction("string-ci<?", StringFunctions::stringCiLessThan);
    registerFunction("string-ci>?", StringFunctions::stringCiGreaterThan);
    registerFunction("string-ci<=?", StringFunctions::stringCiLessOrEqual);
    registerFunction("string-ci>=?", StringFunctions::stringCiGreaterOrEqual);
    registerFunction("string<?", StringFunctions::stringLessThan);
    registerFunction("string>?", StringFunctions::stringGreaterThan);
    registerFunction("string<=?", StringFunctions::stringLessOrEqual);
    registerFunction("string>=?", StringFunctions::stringGreaterOrEqual);
    registerFunction("number->string", StringFunctions::numberToString);
    registerFunction("string->number", StringFunctions::stringToNumber);
    registerFunction("string-upcase", StringFunctions::stringUpcase);
    registerFunction("string-downcase", StringFunctions::stringDowncase);
    registerFunction("string-foldcase", StringFunctions::stringFoldcase);
    registerFunction("string-split", StringFunctions::stringSplit);
    registerFunction("string-join", StringFunctions::stringJoin);
    registerFunction("string-trim", StringFunctions::stringTrim);
    registerFunction("string-contains?", StringFunctions::stringContains);
    registerFunction("string-contains", StringFunctions::stringContains);
    registerFunction("string-prefix?", StringFunctions::stringPrefix);
    registerFunction("string-suffix?", StringFunctions::stringSuffix);
    registerFunction("string-replace", StringFunctions::stringReplace);
    registerFunction("make-string", StringFunctions::makeString);
    registerFunction("string", StringFunctions::string);
    registerFunction("string->list", StringFunctions::stringToList);
    registerFunction("list->string", StringFunctions::listToString);
    registerFunction("string-copy", StringFunctions::stringCopy);
    registerFunction("string-map", StringFunctions::stringMap);
    registerFunction("string-for-each", StringFunctions::stringForEach);
    registerFunction("string-set!", StringFunctions::stringSet);
    registerFunction("string-copy!", StringFunctions::stringCopyBang);
    registerFunction("string-fill!", StringFunctions::stringFillBang);

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
    registerFunction("find", HigherOrderFunctions::find);

    // Sequence functions (polymorphic like Clojure)
    registerFunction("seq", SeqFunctions::seq);
    registerFunction("first", SeqFunctions::first);
    registerFunction("rest", SeqFunctions::rest);
    registerFunction("next", SeqFunctions::next);
    registerFunction("seqable?", SeqFunctions::seqable);

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
    registerFunction("vector-append", VectorFunctions::vectorAppend);
    registerFunction("vector-map", VectorFunctions::vectorMap);
    registerFunction("vector-for-each", VectorFunctions::vectorForEach);
    registerFunction("vector->string", VectorFunctions::vectorToString);
    registerFunction("string->vector", VectorFunctions::stringToVector);

    // Symbol functions (require environment reference)
    SymbolFunctions symbolFuncs = new SymbolFunctions(this);
    registerFunction("symbol->string", symbolFuncs::symbolToString);
    registerFunction("string->symbol", symbolFuncs::stringToSymbol);
    registerFunction("gensym", symbolFuncs::gensym);
    registerFunction("symbol=?", symbolFuncs::symbolEqual);

    // I/O functions
    registerFunction("print", IOFunctions::print);
    registerFunction("println", IOFunctions::println);
    registerFunction("display", IOFunctions::println);
    registerFunction("newline", IOFunctions::newline);
    registerFunction("error", IOFunctions::error);
    registerFunction("line-seq", IOFunctions::lineSeq);
    registerFunction("slurp", IOFunctions::slurp);

    // JSON functions
    registerFunction("json-parse", JsonFunctions::jsonParse);

    // Character functions
    registerFunction("char?", CharFunctions::isChar);
    registerFunction("char=?", CharFunctions::charEqual);
    registerFunction("char<?", CharFunctions::charLessThan);
    registerFunction("char>?", CharFunctions::charGreaterThan);
    registerFunction("char<=?", CharFunctions::charLessOrEqual);
    registerFunction("char>=?", CharFunctions::charGreaterOrEqual);
    registerFunction("char-ci=?", CharFunctions::charCiEqual);
    registerFunction("char-ci<?", CharFunctions::charCiLessThan);
    registerFunction("char-ci>?", CharFunctions::charCiGreaterThan);
    registerFunction("char-ci<=?", CharFunctions::charCiLessOrEqual);
    registerFunction("char-ci>=?", CharFunctions::charCiGreaterOrEqual);
    registerFunction("char-alphabetic?", CharFunctions::charAlphabetic);
    registerFunction("char-numeric?", CharFunctions::charNumeric);
    registerFunction("char-whitespace?", CharFunctions::charWhitespace);
    registerFunction("char-upper-case?", CharFunctions::charUpperCase);
    registerFunction("char-lower-case?", CharFunctions::charLowerCase);
    registerFunction("char->integer", CharFunctions::charToInteger);
    registerFunction("integer->char", CharFunctions::integerToChar);
    registerFunction("char-upcase", CharFunctions::charUpcase);
    registerFunction("char-downcase", CharFunctions::charDowncase);
    registerFunction("char-foldcase", CharFunctions::charFoldcase);
    registerFunction("digit-value", CharFunctions::digitValue);

    // Persistent vector functions
    registerFunction("p-vec", PersistentVectorFunctions::pVec);
    registerFunction("p-vec-ref", PersistentVectorFunctions::pVecRef);
    registerFunction("p-vec-assoc", PersistentVectorFunctions::pVecAssoc);
    registerFunction("p-vec-conj", PersistentVectorFunctions::pVecConj);
    registerFunction("p-vec-pop", PersistentVectorFunctions::pVecPop);
    registerFunction("p-vec-peek", PersistentVectorFunctions::pVecPeek);
    registerFunction("p-vec-length", PersistentVectorFunctions::pVecLength);
    registerFunction("p-vec-subvec", PersistentVectorFunctions::pVecSubvec);
    registerFunction("p-vec->list", PersistentVectorFunctions::pVecToList);
    registerFunction("list->p-vec", PersistentVectorFunctions::listToPVec);
    registerFunction("p-vec-concat", PersistentVectorFunctions::pVecConcat);
    registerFunction("p-vec?", PersistentVectorFunctions::isPVec);
    registerFunction("p-vec-empty?", PersistentVectorFunctions::isPVecEmpty);

    // Persistent map functions
    registerFunction("p-map", PersistentMapFunctions::pMap);
    registerFunction("p-map-get", PersistentMapFunctions::pMapGet);
    registerFunction("p-map-assoc", PersistentMapFunctions::pMapAssoc);
    registerFunction("p-map-dissoc", PersistentMapFunctions::pMapDissoc);
    registerFunction("p-map-contains?", PersistentMapFunctions::pMapContains);
    registerFunction("p-map-keys", PersistentMapFunctions::pMapKeys);
    registerFunction("p-map-vals", PersistentMapFunctions::pMapVals);
    registerFunction("p-map-entries", PersistentMapFunctions::pMapEntries);
    registerFunction("p-map-size", PersistentMapFunctions::pMapSize);
    registerFunction("p-map-merge", PersistentMapFunctions::pMapMerge);
    registerFunction("p-map->list", PersistentMapFunctions::pMapToList);
    registerFunction("list->p-map", PersistentMapFunctions::listToPMap);
    registerFunction("p-map?", PersistentMapFunctions::isPMap);
    registerFunction("p-map-empty?", PersistentMapFunctions::isPMapEmpty);

    // Generic persistent collection functions (polymorphic like Clojure)
    registerFunction("get", PersistentCollectionFunctions::get);
    registerFunction("assoc", PersistentCollectionFunctions::assoc);
    registerFunction("conj", PersistentCollectionFunctions::conj);
    registerFunction("dissoc", PersistentCollectionFunctions::dissoc);
    registerFunction("contains?", PersistentCollectionFunctions::contains);
    registerFunction("count", PersistentCollectionFunctions::count);
    registerFunction("empty?", PersistentCollectionFunctions::isEmpty);
    registerFunction("into", PersistentCollectionFunctions::into);
    registerFunction("persistent?", PersistentCollectionFunctions::isPersistent);

    // Persistent set functions
    registerFunction("p-set", PersistentSetFunctions::pSet);
    registerFunction("p-set-contains?", PersistentSetFunctions::pSetContains);
    registerFunction("p-set-conj", PersistentSetFunctions::pSetConj);
    registerFunction("p-set-disj", PersistentSetFunctions::pSetDisj);
    registerFunction("p-set-union", PersistentSetFunctions::pSetUnion);
    registerFunction("p-set-intersection", PersistentSetFunctions::pSetIntersection);
    registerFunction("p-set-difference", PersistentSetFunctions::pSetDifference);
    registerFunction("p-set-subset?", PersistentSetFunctions::pSetSubset);
    registerFunction("p-set-superset?", PersistentSetFunctions::pSetSuperset);
    registerFunction("p-set-size", PersistentSetFunctions::pSetSize);
    registerFunction("p-set->list", PersistentSetFunctions::pSetToList);
    registerFunction("list->p-set", PersistentSetFunctions::listToPSet);
    registerFunction("p-set?", PersistentSetFunctions::isPSet);
    registerFunction("p-set-empty?", PersistentSetFunctions::isPSetEmpty);

    // Multiple values functions (R7RS)
    registerFunction("values", ValuesFunctions::values);
    registerFunction("call-with-values", ValuesFunctions::callWithValues);
    registerFunction("values?", ValuesFunctions::isValues);

    // Promise functions (R7RS)
    registerFunction("force", PromiseFunctions::force);
    registerFunction("promise?", PromiseFunctions::isPromise);
    registerFunction("make-promise", PromiseFunctions::makePromise);

    // Exception functions (R7RS)
    registerFunction("raise", ExceptionFunctions::raise);
    registerFunction("raise-continuable", ExceptionFunctions::raiseContinuable);
    registerFunction("error-object?", ExceptionFunctions::isErrorObject);
    registerFunction("error-object-message", ExceptionFunctions::errorObjectMessage);
    registerFunction("error-object-irritants", ExceptionFunctions::errorObjectIrritants);
    registerFunction("read-error?", ExceptionFunctions::isReadError);
    registerFunction("file-error?", ExceptionFunctions::isFileError);

    // Parameter functions (R7RS)
    registerFunction("make-parameter", ParameterFunctions::makeParameter);
    registerFunction("parameter?", ParameterFunctions::isParameter);

    // Port functions (R7RS)
    registerFunction("port?", PortFunctions::isPort);
    registerFunction("input-port?", PortFunctions::isInputPort);
    registerFunction("output-port?", PortFunctions::isOutputPort);
    registerFunction("textual-port?", PortFunctions::isTextualPort);
    registerFunction("binary-port?", PortFunctions::isBinaryPort);
    registerFunction("port-open?", PortFunctions::isPortOpen);
    registerFunction("input-port-open?", PortFunctions::isInputPortOpen);
    registerFunction("output-port-open?", PortFunctions::isOutputPortOpen);
    registerFunction("current-input-port", PortFunctions::currentInputPortFn);
    registerFunction("current-output-port", PortFunctions::currentOutputPortFn);
    registerFunction("current-error-port", PortFunctions::currentErrorPortFn);
    registerFunction("open-input-file", PortFunctions::openInputFile);
    registerFunction("open-output-file", PortFunctions::openOutputFile);
    registerFunction("open-input-string", PortFunctions::openInputString);
    registerFunction("open-output-string", PortFunctions::openOutputString);
    registerFunction("get-output-string", PortFunctions::getOutputString);
    registerFunction("close-port", PortFunctions::closePort);
    registerFunction("close-input-port", PortFunctions::closeInputPort);
    registerFunction("close-output-port", PortFunctions::closeOutputPort);
    registerFunction("read-char", PortFunctions::readChar);
    registerFunction("peek-char", PortFunctions::peekChar);
    registerFunction("read-line", PortFunctions::readLine);
    registerFunction("char-ready?", PortFunctions::charReady);
    registerFunction("eof-object", PortFunctions::eofObject);
    registerFunction("eof-object?", PortFunctions::isEofObject);
    registerFunction("write-char", PortFunctions::writeChar);
    registerFunction("write-string", PortFunctions::writeString);
    registerFunction("flush-output-port", PortFunctions::flushOutputPort);

    // Bytevector functions (R7RS)
    registerFunction("bytevector?", BytevectorFunctions::isBytevector);
    registerFunction("make-bytevector", BytevectorFunctions::makeBytevector);
    registerFunction("bytevector", BytevectorFunctions::bytevector);
    registerFunction("bytevector-length", BytevectorFunctions::bytevectorLength);
    registerFunction("bytevector-u8-ref", BytevectorFunctions::bytevectorU8Ref);
    registerFunction("bytevector-u8-set!", BytevectorFunctions::bytevectorU8Set);
    registerFunction("bytevector-copy", BytevectorFunctions::bytevectorCopy);
    registerFunction("bytevector-copy!", BytevectorFunctions::bytevectorCopyMutate);
    registerFunction("bytevector-append", BytevectorFunctions::bytevectorAppend);
    registerFunction("utf8->string", BytevectorFunctions::utf8ToString);
    registerFunction("string->utf8", BytevectorFunctions::stringToUtf8);

    // Binary ports (R7RS)
    registerFunction("open-binary-input-file", PortFunctions::openBinaryInputFile);
    registerFunction("open-binary-output-file", PortFunctions::openBinaryOutputFile);
    registerFunction("open-input-bytevector", PortFunctions::openInputBytevector);
    registerFunction("open-output-bytevector", PortFunctions::openOutputBytevector);
    registerFunction("get-output-bytevector", PortFunctions::getOutputBytevector);
    registerFunction("read-u8", PortFunctions::readU8);
    registerFunction("peek-u8", PortFunctions::peekU8);
    registerFunction("u8-ready?", PortFunctions::u8Ready);
    registerFunction("write-u8", PortFunctions::writeU8);
    registerFunction("read-bytevector", BytevectorFunctions::readBytevector);
    registerFunction("read-bytevector!", BytevectorFunctions::readBytevectorMutate);
    registerFunction("write-bytevector", BytevectorFunctions::writeBytevector);

    // System functions (R7RS) - require environment reference
    SystemFunctions systemFuncs = new SystemFunctions(this);
    registerFunction("features", systemFuncs::features);
    registerFunction("command-line", systemFuncs::commandLine);
    registerFunction("get-environment-variable", systemFuncs::getEnvironmentVariable);
    registerFunction("get-environment-variables", systemFuncs::getEnvironmentVariables);
    registerFunction("exit", systemFuncs::exit);
    registerFunction("emergency-exit", systemFuncs::emergencyExit);

    // File functions (R7RS - scheme.file)
    registerFunction("file-exists?", FileFunctions::fileExists);
    registerFunction("delete-file", FileFunctions::deleteFile);
    registerFunction("call-with-input-file", FileFunctions::callWithInputFile);
    registerFunction("call-with-output-file", FileFunctions::callWithOutputFile);
    registerFunction("with-input-from-file", FileFunctions::withInputFromFile);
    registerFunction("with-output-to-file", FileFunctions::withOutputToFile);
  }

  /**
   * Registers introspection functions that require a reference to the Lisp instance. This must be
   * called after the Lisp instance is created.
   *
   * @param lisp the Lisp instance
   */
  public void registerIntrospectionFunctions(Lisp lisp) {
    this.lispInstance = lisp;
    IntrospectionFunctions introspection = new IntrospectionFunctions(this, lisp);
    registerFunction("environment-symbols", introspection::environmentSymbols);
    registerFunction("procedure-arity", introspection::procedureArity);
    registerFunction("procedure-name", introspection::procedureName);
    registerFunction("load", introspection::load);
    registerFunction("load-relative", introspection::loadRelative);
    registerFunction("current-load-pathname", introspection::currentLoadPathname);

    // Environment functions (R7RS)
    registerFunction("interaction-environment", introspection::interactionEnvironment);
    registerFunction("scheme-report-environment", introspection::schemeReportEnvironment);
    registerFunction("null-environment", introspection::nullEnvironment);

    // Read/write functions (R7RS)
    ReadWriteFunctions readWrite = new ReadWriteFunctions(Parser.defaultParser(), this);
    registerFunction("read", readWrite::read);
    registerFunction("write", ReadWriteFunctions::write);
    registerFunction("write-simple", ReadWriteFunctions::writeSimple);
    registerFunction("eval", introspection::eval);

    // Time functions (R7RS - scheme.time)
    registerFunction("current-second", TimeFunctions::currentSecond);
    registerFunction("current-jiffy", TimeFunctions::currentJiffy);
    registerFunction("jiffies-per-second", TimeFunctions::jiffiesPerSecond);

    // Date/time formatting functions
    registerFunction("current-date-string", TimeFunctions::currentDateString);
    registerFunction("timestamp->date-string", TimeFunctions::timestampToDateString);
    registerFunction("date->display-string", TimeFunctions::dateToDisplayString);
  }

  private void initMacroTable() {
    StandardMacros macros = new StandardMacros(this);
    registerMacro("when", macros::when);
    // Note: 'unless' is intentionally NOT registered as a built-in macro
    // to allow users to override it with define-syntax. Users can define it as:
    // (define-syntax unless (syntax-rules () ((unless cond body ...) (if cond '() (begin body
    // ...)))))
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

  /**
   * Returns a copy of the current macro table. Used by let-syntax and letrec-syntax to create
   * augmented macro environments.
   */
  public Map<AtomObject, MacroDefinition> getMacroTable() {
    return new HashMap<>(macroTable);
  }

  /** Expands macros using a custom macro table (for local macro scopes). */
  public LispObject expandMacrosWithTable(LispObject obj, Map<AtomObject, MacroDefinition> macros) {
    return MacroExpander.expandMacro(macros, obj);
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

  /**
   * Returns true if we're currently inside a let environment. Used by CachedFunctionSupplier to
   * skip let environment checks when not needed (optimization).
   */
  public boolean hasLetEnv() {
    return !letEnvStack.isEmpty();
  }

  /**
   * Look up a value in the let environment stack only (not global). Returns null if not found in
   * any let environment. Used by let-bound ComputedLispObjects to get fresh values.
   */
  public LispObject lookupInLetEnvStack(AtomObject atom) {
    for (int i = letEnvStack.size() - 1; i >= 0; i--) {
      Environment letEnv = letEnvStack.get(i);
      LispObject value = letEnv.lookupValueOrNull(atom);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  /**
   * Set a value in the let environment stack. Finds the environment containing the binding and
   * updates it. Used by set! on let-bound variables.
   */
  public void setInLetEnvStack(AtomObject atom, LispObject value) {
    for (int i = letEnvStack.size() - 1; i >= 0; i--) {
      Environment letEnv = letEnvStack.get(i);
      if (letEnv.lookupValueOrNull(atom) != null) {
        letEnv.set(atom, value);
        return;
      }
    }
    // If not found in let stack, fall back to global (shouldn't normally happen)
    set(atom, value);
  }

  /**
   * Capture the current let environment stack for delayed evaluation. Returns a copy of the current
   * let environments that can be restored later.
   *
   * @return a list of captured let environments, or null if no let environments are active
   */
  public List<Environment> captureLetEnvStack() {
    if (letEnvStack.isEmpty()) {
      return null;
    }
    // Return a copy of the current let environment stack
    return new ArrayList<>(letEnvStack);
  }

  /**
   * Restore a previously captured let environment stack. Used by delay/delay-force to restore the
   * environment when forcing a promise.
   *
   * @param captured the captured let environments to restore
   */
  public void restoreLetEnvStack(List<Environment> captured) {
    if (captured != null) {
      letEnvStack.addAll(captured);
    }
  }

  /**
   * Pop multiple let environments from the stack. Used to clean up after restoring a captured let
   * environment stack.
   *
   * @param count the number of let environments to pop
   */
  public void popLetEnvs(int count) {
    for (int i = 0; i < count; i++) {
      if (!letEnvStack.isEmpty()) {
        letEnvStack.remove(letEnvStack.size() - 1);
      }
    }
  }

  /**
   * Returns the names of all defined symbols in the global environment. Used for introspection and
   * completions.
   *
   * @return a set of symbol names
   */
  public Set<String> getDefinedSymbols() {
    Set<String> symbols = new HashSet<>();
    for (AtomObject atom : objects.keySet()) {
      String name = names.get(atom);
      if (name != null) {
        symbols.add(name);
      }
    }
    return symbols;
  }
}
