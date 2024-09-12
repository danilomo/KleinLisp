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
import net.sourceforge.kleinlisp.api.BooleanFunctions;
import net.sourceforge.kleinlisp.api.ListFunctions;
import net.sourceforge.kleinlisp.api.MathFunctions;
import net.sourceforge.kleinlisp.objects.AtomFactory;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.kleinlisp.api.IOFunctions;
import net.sourceforge.kleinlisp.macros.MacroDefinition;
import net.sourceforge.kleinlisp.macros.MacroExpander;
import net.sourceforge.kleinlisp.macros.StandardMacros;
/**
 * @author daolivei
 */
public class LispEnvironment implements Environment {
    
    public static class FunctionStack {
        private final LispObject[] parameters;
        private final Environment env;
        
        public FunctionStack(LispObject[] parameters, Environment env) {        
            this.parameters = parameters;
            this.env = env;            
        }
        
        public final LispObject parameterAt(int i) {
            return parameters[i];
        }

        public void setParameterAt(int i, LispObject obj) {
            parameters[i] = obj;
        }

        public Environment getEnv() {
            return env;
        }        
    }    

    private final Map<AtomObject, LispObject> objects;
    private final Map<AtomObject, String> names;
    private final Map<AtomObject, MacroDefinition> macroTable;
    private final AtomFactory atomFactory;
    private final List<FunctionStack> stack;

    public LispEnvironment() {
        this.objects = new HashMap<>();
        this.names = new HashMap<>();
        this.atomFactory = new AtomFactory(this);
        this.stack = new ArrayList<>();
        this.macroTable = new HashMap<>();
        initFunctionTable();
        initMacroTable();
    }

    private void initFunctionTable() {
        registerFunction("+", MathFunctions::add);
        registerFunction("-", MathFunctions::sub);
        registerFunction("*", MathFunctions::mul);
        registerFunction("/", MathFunctions::div);
        registerFunction("mod", MathFunctions::mod);
        registerFunction("list", ListFunctions::list);
        registerFunction("length", ListFunctions::length);
        registerFunction("car", ListFunctions::car);
        registerFunction("cdr", ListFunctions::cdr);

        registerFunction("<",  BooleanFunctions::lt);
        registerFunction(">",  BooleanFunctions::gt);
        registerFunction("<=", BooleanFunctions::le);
        registerFunction(">=", BooleanFunctions::ge);
        registerFunction("=",  BooleanFunctions::eq);
        registerFunction("!=", BooleanFunctions::neq);
        registerFunction("not", BooleanFunctions::not);
        
        registerFunction("print", IOFunctions::print);
        registerFunction("println", IOFunctions::println);
        registerFunction("display", IOFunctions::println);
        registerFunction("newline", IOFunctions::newline);
    }
    
    private void initMacroTable() {
        StandardMacros macros = new StandardMacros(this);
        registerMacro("when", macros::when);
        registerMacro("let", macros::let);
    }    

    public void registerFunction(String symbol, net.sourceforge.kleinlisp.Function func) {
        set(atomOf(symbol), new FunctionObject(func));
    }
    
    public void registerMacro(String symbol, MacroDefinition macro) {
        macroTable.put(atomOf(symbol), macro);
    } 
    
    public void registerMacro(AtomObject symbol, MacroDefinition macro) {
        macroTable.put(symbol, macro);
    }      

    @Override
    public LispObject lookupValue(AtomObject atom) {
        return objects.get(atom);
    }

    @Override
    public void set(AtomObject atom, LispObject obj) {
        objects.put(atom, obj);
    }

    @Override
    public boolean exists(AtomObject atom) {
        return objects.containsKey(atom);
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
        stack.add(new FunctionStack(parameters, env));
    }

    public void stackPop() {
        stack.remove(stack.size()-1);
    }
    
    public FunctionStack stackTop() {
        return stack.get(stack.size()-1);
    }
    
    public boolean isStackEmpty() {
        return stack.isEmpty();
    }

    public LispObject expandMacros(LispObject obj) {
        return MacroExpander.expandMacro(macroTable, obj);
    }
}
