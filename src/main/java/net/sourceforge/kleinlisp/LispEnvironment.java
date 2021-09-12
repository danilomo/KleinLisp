package net.sourceforge.kleinlisp;

import net.sourceforge.kleinlisp.functions.*;
import net.sourceforge.kleinlisp.functions.ArithmeticOperator.Operator;
import net.sourceforge.kleinlisp.objects.FunctionObject;
import net.sourceforge.kleinlisp.objects.JavaObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.playground.Main;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author daolivei
 */
public class LispEnvironment implements Environment {

    private final Map<String, BindingList> objects = new LinkedHashMap<>();

    public LispEnvironment() {
        initFunctionTable();
    }

    @Override
    public String toString() {
        return objects.toString();
    }

    public void addClass(Class clazz) {
        Function f = (p) -> {
            try {
                BeanInfo info = Introspector.getBeanInfo(clazz);
                PropertyDescriptor[] pds = info.getPropertyDescriptors();
                Object o = clazz.getConstructors()[0].newInstance();

                List<LispObject> list = p.toList();

                for (int i = 1; i < pds.length; i++) {
                    pds[i].getWriteMethod().invoke(o, list.get(i - 1).asObject());
                }

                return new JavaObject(o);

            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

            return ListObject.NIL;
        };

        this.addFunction(clazz.getSimpleName(), f);
    }

    public void addFunction(String name, Function function) {
        registerFunction(name, function);
    }

    private void initFunctionTable() {
//        registerFunction("+", new ArithmeticOperator(Operator.PLUS));
//        registerFunction("-", new ArithmeticOperator(Operator.MINUS));

        registerFunction("+", MiscFunctions::sum);
        registerFunction("-", MiscFunctions::minus);

        registerFunction("*", new ArithmeticOperator(Operator.TIMES));
        registerFunction("/", new ArithmeticOperator(Operator.DIV));
        registerFunction("%", new ArithmeticOperator(Operator.MOD));

        registerFunction("list", parameters -> parameters);

        registerFunction("<", new ComparisonOperator(ComparisonOperator.Operator.LT));
        registerFunction(">", new ComparisonOperator(ComparisonOperator.Operator.GT));
        registerFunction("<=", new ComparisonOperator(ComparisonOperator.Operator.LEQ));
        registerFunction(">=", new ComparisonOperator(ComparisonOperator.Operator.GEQ));
        registerFunction("=", new ComparisonOperator(ComparisonOperator.Operator.EQ));
        registerFunction("!=", new ComparisonOperator(ComparisonOperator.Operator.NEQ));

        registerFunction("list", ListFunctions::list);
        registerFunction("length", ListFunctions::length);
        registerFunction("car", ListFunctions::car);
        registerFunction("cdr", ListFunctions::cdr);
        registerFunction("map", ListFunctions::map);
        registerFunction("filter", ListFunctions::filter);
        registerFunction("takewhile", ListFunctions::takewhile);

        registerFunction("log", (parameters) -> {
            System.out.println("LOG::" + parameters);
            return parameters;
        });

        registerFunction("print", IOFunctions::print);
    }

    private void registerFunction(String name, Function func) {
        this.define(name, new FunctionObject(func));
    }

    @Override
    public LispObject lookupValue(String name) {
        try {
            return this.objects.get(name).head().value();
        } catch (Exception e) {
            System.out.println("Failed to look up: " + name);
            throw e;
        }
    }

    @Override
    public void set(String name, LispObject obj) {
        this.objects.get(name).head().set(obj);
    }

    @Override
    public void define(String name, LispObject obj) {
        BindingList bl = this.objects.get(name);
        this.objects.put(name, new BindingList(new Binding(obj), bl));
    }

    @Override
    public void undefine(String name) {
        BindingList bl = this.objects.get(name);
        this.objects.put(name, bl.tail());
    }

    @Override
    public boolean exists(String name) {
        return objects.containsKey(name);
    }

    @Override
    public Binding lookup(String name) {
        try {
            return this.objects.get(name).head();
        } catch (Exception e) {
            System.out.println("Failed to look up: " + name);
            throw e;
        }
    }

    private static class BindingList {

        private final Binding head;
        private final BindingList tail;

        public BindingList(Binding head, BindingList tail) {
            this.head = head;
            this.tail = tail;
        }

        public Binding head() {
            return head;
        }

        public BindingList tail() {
            return tail;
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
