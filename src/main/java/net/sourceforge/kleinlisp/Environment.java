/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp;

/**
 *
 * @author daolivei
 */
public interface Environment {
    public Function lookupFunction(String name);
}
