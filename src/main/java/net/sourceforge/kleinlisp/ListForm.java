package net.sourceforge.kleinlisp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author daolivei
 */
public class ListForm implements Form, Iterable<Form> {

    private final Form head;
    private final Form tail;
    private Environment environment;

    public ListForm(Form head, Form tail) {
        this.head = head;
        this.tail = tail;
    }

    public ListForm(Form head, Form tail, Environment env) {
        this.head = head;
        this.tail = tail;
        this.environment = env;
    }

    public Form head() {
        return head;
    }

    public Form tail() {
        return tail;
    }

    public Form car() {
        return head;
    }

    public ListForm cdr() {
        return (ListForm) tail;
    }

    @Override
    public Object asObject() {
        List list = new ArrayList();

        for (Form f : this) {
            list.add(f.asObject());
        }

        return list;
    }

    @Override
    public String toString() {
        return "(" + String.join(" ", toList().stream().map(t -> t.toString()).collect(Collectors.toList())) + ")";
    }

    public static final ListForm NIL = new ListForm(null, null) {
        @Override
        public String toString() {
            return "()";
        }

        @Override
        public Form evaluate() {
            return this;
        }

        @Override
        public Optional<Integer> asInt() {
            return Optional.empty();
        }

        @Override
        public Optional<Double> asDouble() {
            return Optional.empty();
        }

        @Override
        public Optional<ListForm> asList() {
            return Optional.of(this);
        }

        @Override
        public boolean truthness() {
            return false;
        }
    };

    public List<Form> toList() {
        List<Form> list = new ArrayList<>();
        Iterator<Form> it = iterator();
        it.forEachRemaining(list::add);
        return list;
    }

    @Override
    public Iterator<Form> iterator() {
        return new ListFormIterator();
    }

    private ListForm evaluateContents() {
        if (cdr() != NIL) {
            return new ListForm(car().evaluate(), cdr().evaluateContents(), environment);
        } else {
            return new ListForm(car().evaluate(), NIL, environment);
        }
    }

    private class ListFormIterator implements Iterator<Form> {

        private ListForm cursor;

        public ListFormIterator() {
            cursor = ListForm.this;
        }

        @Override
        public boolean hasNext() {
            return cursor != NIL;
        }

        @Override
        public Form next() {
            Form f = cursor.head;
            cursor = cursor.cdr();
            return f;
        }
    }

    @Override
    public Form evaluate() {
        String fname = car().toString();

        if ("quote".equals(fname)) {
            return cdr();
        }

        ListForm parameters = cdr().evaluateContents();

        try {
            return environment.lookupFunction(fname).evaluate(parameters);
        } catch (Exception e) {
            System.out.println(fname);
            System.out.println(parameters);
            throw e;
        }
    }

    @Override
    public Optional<Integer> asInt() {
        return Optional.empty();
    }

    @Override
    public Optional<Double> asDouble() {
        return Optional.empty();
    }

    @Override
    public Optional<ListForm> asList() {
        return Optional.of(this);
    }

    @Override
    public <T> Optional<T> asObject(Class<T> clazz) {
        return Optional.empty();
    }
    
    @Override
    public boolean truthness() {
        return true;
    }    
}
