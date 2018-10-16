package net.sourceforge.kleinlisp.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.sourceforge.kleinlisp.Function;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.specialforms.SpecialForm;

/**
 *
 * @author daolivei
 */
public class ListObject implements LispObject, Iterable<LispObject> {

    private final LispObject head;
    private final LispObject tail;
    private final int length;

    private ListObject() {
        this.head = null;
        this.tail = null;
        this.length = 0;
    }

    public ListObject(LispObject head, LispObject tail) {
        this.head = head;
        this.tail = tail;

        if (tail.asList().isPresent()) {
            this.length = 1 + tail.asList().get().length;
        } else {
            this.length = 2;
        }

    }

    public LispObject head() {
        return head;
    }

    public LispObject tail() {
        return tail;
    }

    public LispObject car() {
        return head;
    }

    public ListObject cdr() {
        return (ListObject) tail;
    }

    public int length() {
        return length;
    }

    @Override
    public Object asObject() {
        List list = new ArrayList();

        for (LispObject f : this) {
            list.add(f.asObject());
        }

        return list;
    }

    @Override
    public String toString() {
        return "(" + String.join(" ", toList().stream().map(t -> t.toString()).collect(Collectors.toList())) + ")";
    }

    public static final ListObject NIL = new ListObject() {
        @Override
        public String toString() {
            return "()";
        }

        @Override
        public LispObject evaluate() {
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
        public Optional<ListObject> asList() {
            return Optional.of(this);
        }

        @Override
        public boolean truthness() {
            return false;
        }

        @Override
        public int length() {
            return 0;
        }
    };

    public List<LispObject> toList() {
        List<LispObject> list = new ArrayList<>();
        Iterator<LispObject> it = iterator();
        it.forEachRemaining(list::add);
        return list;
    }

    @Override
    public Iterator<LispObject> iterator() {
        return new ListFormIterator();
    }

    private ListObject evaluateContents() {
        if (cdr() != NIL) {
            return new ListObject(car().evaluate(), cdr().evaluateContents());
        } else {
            return new ListObject(car().evaluate(), NIL);
        }
    }

    private class ListFormIterator implements Iterator<LispObject> {

        private ListObject cursor;

        public ListFormIterator() {
            cursor = ListObject.this;
        }

        @Override
        public boolean hasNext() {
            return cursor != NIL;
        }

        @Override
        public LispObject next() {
            LispObject f = cursor.head;
            cursor = cursor.cdr();
            return f;
        }
    }

    @Override
    public LispObject evaluate() {
        Optional<AtomObject> atom = car().asAtom();

        if (atom.isPresent()) {
            Optional<Function> specialForm = SpecialForm
                    .of(atom.get().toString(), atom.get().environment());

            if (specialForm.isPresent()) {
                return specialForm.get().evaluate(cdr());
            }
        }

        ListObject parameters = cdr().evaluateContents();
        FunctionObject obj = car().evaluate().asFunction().get();
        return obj.function().evaluate(parameters);

//        try {
//            return environment.lookup(fname).evaluate(parameters);
//        } catch (Exception e) {
//            System.out.println(fname);
//            System.out.println(parameters);
//            throw e;
//        }
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
    public Optional<ListObject> asList() {
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

    @Override
    public Optional<FunctionObject> asFunction() {
        return Optional.empty();
    }

    @Override
    public Optional<AtomObject> asAtom() {
        return Optional.empty();
    }
}
