package net.sourceforge.kleinlisp;

import net.sourceforge.kleinlisp.objects.JavaObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.kleinlisp.functions.ArithmeticOperator;
import net.sourceforge.kleinlisp.functions.ArithmeticOperator.Operator;
import net.sourceforge.kleinlisp.functions.ComparisonOperator;
import net.sourceforge.kleinlisp.functions.ListFunctions;
import net.sourceforge.kleinlisp.objects.FunctionObject;

/**
 *
 * @author daolivei
 */
public class LispEnv implements Environment {

    private final Map<String, LispObject> objects = new HashMap<>();

    public LispEnv() {
        initFunctionTable();
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

    public void removeFunction(String name) {
        objects.remove(name);
    }

    private void initFunctionTable() {
        registerFunction("+", new ArithmeticOperator(Operator.PLUS));
        registerFunction("-", new ArithmeticOperator(Operator.MINUS));
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
        
        registerFunction("log", (parameters) -> {
            System.out.println("LOG::" + parameters);
            return parameters;
        });
    }

    private void registerFunction(String name, Function func) {
        objects.put(name, new FunctionObject(func));
    }

    @Override
    public LispObject lookup(String name) {
        return objects.get(name);
    }
}
