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
package net.sourceforge.kleinlisp.objects;

import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.LispVisitor;
import net.sourceforge.kleinlisp.functional.Tuple2;
import net.sourceforge.kleinlisp.functional.Tuple3;
import net.sourceforge.kleinlisp.functional.Tuple4;
import net.sourceforge.kleinlisp.functional.Tuple5;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author daolivei
 */
public class ListObject implements LispObject, Iterable<LispObject> {

    public static final ListObject NIL = new ListObject() {
        @Override
        public String toString() {
            return "()";
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
        public boolean truthiness() {
            return false;
        }

        @Override
        public int length() {
            return 0;
        }
    };
    private LispObject head;
    private LispObject tail;
    private final int length;
    private Object meta;

    private ListObject() {
        this.head = null;
        this.tail = null;
        this.length = 0;
    }

    public <T> T getMeta(Class<T> clazz) {
        return (T) meta;
    }
    
    public void setMeta(Object meta) {
        this.meta = meta;
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

    public ListObject(LispObject head) {
        this.head = head;
        this.tail = ListObject.NIL;
        this.length = 1;
    }

    public static LispObject fromList(List<LispObject> params) {
        ListObject obj = ListObject.NIL;

        for (int i = params.size() - 1; i >= 0; i--) {
            LispObject elem = params.get(i);
            obj = new ListObject(elem, obj);
        }

        return obj;
    }
    
    public static LispObject fromList(LispObject[] params) {
        ListObject obj = ListObject.NIL;

        for (int i = params.length - 1; i >= 0; i--) {
            LispObject elem = params[i];
            obj = new ListObject(elem, obj);
        }

        return obj;
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
        String metastr = "";
        if (meta != null) {
            //metastr = meta + "@";
        }
        return metastr + "(" + String.join(" ", toList().stream().map(t -> t.toString()).collect(Collectors.toList())) + ")";
    }

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

    @Override
    public Optional<ListObject> asList() {
        return Optional.of(this);
    }

    @Override
    public boolean truthiness() {
        return true;
    }

    @Override
    public <T> T accept(LispVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean error() {
        return false;
    }

    public <K, V> Optional<Tuple2<K, V>> unpack(Class<K> c1, Class<V> c2) {
        if (this.length() < 2) {
            return Optional.empty();
        }

        Optional<K> first = this.car().as(c1);
        Optional<V> second = this.cdr().car().as(c2);

        if (first.isPresent() && second.isPresent()) {
            return Optional.of(new Tuple2(first.get(), second.get()));
        } else {
            return Optional.empty();
        }
    }

    public <K, V, T> Optional<Tuple3<K, V, T>> unpack(Class<K> c1, Class<V> c2, Class<T> c3) {

        if (this.length() < 3) {
            return Optional.empty();
        }

        Optional<K> first = this.car().as(c1);
        Optional<V> second = this.cdr().car().as(c2);
        Optional<T> third = this.cdr().cdr().car().as(c3);

        System.out.println(first);
        System.out.println(second);
        System.out.println(third);

        if (first.isPresent() && second.isPresent() && third.isPresent()) {
            return Optional.of(new Tuple3(first.get(), second.get(), third.get()));
        } else {
            return Optional.empty();
        }
    }

    public <K, V, T, X> Optional<Tuple4<K, V, T, X>> unpack(Class<K> c1, Class<V> c2,
                                                            Class<T> c3, Class<X> c4) {

        if (this.length() < 4) {
            return Optional.empty();
        }

        Optional<K> first = this.car().as(c1);
        Optional<V> second = this.cdr().car().as(c2);
        Optional<T> third = this.cdr().cdr().car().as(c3);
        Optional<X> fourth = this.cdr().cdr().cdr().car().as(c4);

        if (first.isPresent() && second.isPresent()
                && third.isPresent() && fourth.isPresent()) {
            return Optional.of(new Tuple4(first.get(), second.get(), third.get(), fourth.get()));
        } else {
            return Optional.empty();
        }
    }

    public <K, V, T, X, Z> Optional<Tuple5<K, V, T, X, Z>> unpack(Class<K> c1, Class<V> c2,
                                                                  Class<T> c3, Class<X> c4, Class<Z> c5) {

        if (this.length() < 5) {
            return Optional.empty();
        }

        Optional<K> first = this.car().as(c1);
        Optional<V> second = this.cdr().car().as(c2);
        Optional<T> third = this.cdr().cdr().car().as(c3);
        Optional<X> fourth = this.cdr().cdr().cdr().car().as(c4);
        Optional<Z> fifth = this.cdr().cdr().cdr().cdr().car().as(c5);

        if (first.isPresent() && second.isPresent()
                && third.isPresent() && fourth.isPresent()
                && fifth.isPresent()) {
            return Optional.of(new Tuple5(first.get(),
                    second.get(), third.get(), fourth.get(), fifth.get()));
        } else {
            return Optional.empty();
        }
    }

    public void setCdr(LispObject list) {
        this.tail = list;
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

}
