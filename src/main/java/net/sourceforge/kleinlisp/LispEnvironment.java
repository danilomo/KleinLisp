package net.sourceforge.kleinlisp;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.kleinlisp.api.ListFunctions;
import net.sourceforge.kleinlisp.api.MathFunctions;
import net.sourceforge.kleinlisp.objects.AtomFactory;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.FunctionObject;

/**
 *
 * @author daolivei
 */
public class LispEnvironment implements Environment {

    private final Map<AtomObject, BindingList> objects;
    private final Map<AtomObject, String> names;
    private final AtomFactory atomFactory;

    public LispEnvironment() {
        this.objects = new HashMap<>();
        this.names = new HashMap<>();
        this.atomFactory = new AtomFactory(this);

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
