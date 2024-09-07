/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.sourceforge.kleinlisp.functional;

/**
 *
 * @author danilo
 */
public interface TriFunction<K, V, T, R> {
    R apply(K k, V v, T t);
}
