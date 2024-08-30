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

/**
 * @author daolivei
 */
public class LispEnvironment implements Environment {

    private final Map<AtomObject, LispObject> objects;
    private final Map<AtomObject, String> names;
    private final AtomFactory atomFactory;
    private final List<List<LispObject>> stack;

    public LispEnvironment() {
        this.objects = new HashMap<>();
        this.names = new HashMap<>();
        this.atomFactory = new AtomFactory(this);
        this.stack = new ArrayList<>();
        initFunctionTable();
    }

    private void initFunctionTable() {
        registerFunction("+", MathFunctions::add);
        registerFunction("-", MathFunctions::sub);
        registerFunction("*", MathFunctions::mul);
        registerFunction("/", MathFunctions::div);
        registerFunction("%", MathFunctions::mod);
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
        
        registerFunction("print", IOFunctions::print);
        registerFunction("println", IOFunctions::println);
    }

    private void registerFunction(String symbol, net.sourceforge.kleinlisp.Function func) {
        set(atomOf(symbol), new FunctionObject(func));
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

    public void stackPush(List<LispObject> parameters) {
        stack.add(parameters);
    }

    public List<LispObject> stackTop() {
        return stack.get(stack.size() -1);
    }

    public void stackPop() {
        stack.remove(stack.size()-1);
    }

}
