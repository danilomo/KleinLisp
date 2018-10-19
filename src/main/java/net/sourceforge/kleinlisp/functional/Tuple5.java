/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.functional;

/**
 *
 * @author Danilo Oliveira
 */
public class Tuple5<K, V, T, X, Z> {
    private final K k;
    private final V v;
    private final T t;
    private final X x;
    private final Z z;

    public Tuple5(K k, V v, T t, X x, Z z) {
        this.k = k;
        this.v = v;
        this.t = t;
        this.x = x;
        this.z = z;
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

    public X fourth() {
        return x;
    }

    public Z fifth() {
        return z;
    }
    
    

}

