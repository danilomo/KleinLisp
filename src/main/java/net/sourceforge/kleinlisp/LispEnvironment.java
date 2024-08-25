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

/**
 * @author daolivei
 */
public class LispEnvironment implements Environment {

    private final Map<AtomObject, BindingList> objects;
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
    }

    private void registerFunction(String symbol, net.sourceforge.kleinlisp.Function func) {
        define(atomOf(symbol), new FunctionObject(func));
    }

    @Override
    public LispObject lookupValue(AtomObject atom) {
        return objects.get(atom).head.value();
    }

    @Override
    public void set(AtomObject atom, LispObject obj) {
        objects.get(atom).head.set(obj);
    }

    @Override
    public void define(AtomObject atom, LispObject obj) {
        BindingList tail = objects.get(atom);
        objects.put(atom, new BindingList(new Binding(obj), tail));
    }

    @Override
    public void undefine(AtomObject name) {
        BindingList bl = this.objects.get(name);
        this.objects.put(name, bl.tail);
    }

    @Override
    public boolean exists(AtomObject atom) {
        return objects.containsKey(atom);
    }

    @Override
    public Binding lookup(AtomObject name) {
        return objects.get(name).head;
    }

    @Override
    public AtomObject atomOf(String atom) {
        AtomObject obj = atomFactory.newAtom(atom);
        names.put(obj, atom);
        return obj;
    }

    @Override
    public String valueOf(AtomObject atom) {
        return names.get(atom);
    }

    @Override
    public void stackPush(List<LispObject> parameters) {
        stack.add(parameters);
    }
    
    @Override
    public List<LispObject> stackTop() {
        return stack.get(stack.size() -1);
    }

    @Override
    public void stackPop() {
        stack.remove(stack.size()-1);
    }

    private static class BindingList {
        private final Binding head;
        private final BindingList tail;

        public BindingList(Binding head, BindingList tail) {
            this.head = head;
            this.tail = tail;
        }

        @Override
        public String toString() {
            BindingList bl = this;
            StringBuilder builder = new StringBuilder().append("[");
            while (bl != null) {
                builder.append(bl.head).append(", ");
                bl = bl.tail;
            }
            builder.append("]");
            return builder.toString();
        }
    }

}
