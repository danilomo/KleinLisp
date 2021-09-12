

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.functional;

/**
 * @author Danilo Oliveira
 */
public class Tuple3<K, V, T> {
    private final K k;
    private final V v;
    private final T t;

    public Tuple3(K k, V v, T t) {
        this.k = k;
        this.v = v;
        this.t = t;
    }

    public K first() {
        return k;
    }

    public V second() {
        return v;
    }

    public T third() {
        return t;
    }

    @Override
    public String toString() {
        return "[ " + k + ", " + v + ", " + t + " ]";
    }


}
