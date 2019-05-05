package net.sourceforge.kleinlisp;

import net.sourceforge.kleinlisp.objects.JavaObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    }

    @Override
    public LispObject lookupValue(AtomObject name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void set(AtomObject name, LispObject obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void define(AtomObject name, LispObject obj) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void undefine(AtomObject name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean exists(AtomObject name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Binding lookup(AtomObject name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
