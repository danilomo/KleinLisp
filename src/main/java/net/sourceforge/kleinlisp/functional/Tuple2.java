/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.functional;

/**
 * @author Danilo Oliveira
 */
public class Tuple2<K, V> {
    private final K k;
    private final V v;

    public Tuple2(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public K first() {
        return k;
    }

    public V second() {
        return v;
    }
}
