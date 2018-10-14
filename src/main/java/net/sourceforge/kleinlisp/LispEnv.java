package net.sourceforge.kleinlisp;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.kleinlisp.functions.ArithmeticFunction;
import net.sourceforge.kleinlisp.functions.ArithmeticFunction.Operator;

/**
 *
 * @author daolivei
 */
public class LispEnv implements Environment {

    private Map<String, Function> functionTable = new HashMap<>();

    public LispEnv() {
        initFunctionTable();
    }

    public void addClass(Class clazz) {
        Function f = (p) -> {
            try {
                BeanInfo info = Introspector.getBeanInfo(clazz);
                PropertyDescriptor[] pds = info.getPropertyDescriptors();
                Object o = clazz.getConstructors()[0].newInstance();

                List<Form> list = p.toList();

                for (int i = 1; i < pds.length; i++) {
                    pds[i].getWriteMethod().invoke(o, list.get(i - 1).asObject());
                }

                return new ObjectForm(o);

            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

            return ListForm.NIL;
        };

        this.addFunction(clazz.getSimpleName().toLowerCase(), f);
    }

    public void addFunction(String name, Function function) {
        functionTable.put(name, function);
    }

    public void removeFunction(String name) {
        functionTable.remove(name);
    }

    private void initFunctionTable() {
        
        functionTable.put("+", new ArithmeticFunction(Operator.PLUS));
        functionTable.put("-", new ArithmeticFunction(Operator.MINUS));
        functionTable.put("*", new ArithmeticFunction(Operator.TIMES));
        functionTable.put("/", new ArithmeticFunction(Operator.DIV));
        functionTable.put("%", new ArithmeticFunction(Operator.MOD));

        functionTable.put("list", parameters -> parameters);
    }

    @Override
    public Function lookupFunction(String name) {
        return functionTable.get(name);
    }
}
