/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.special_forms;

import net.sourceforge.kleinlisp.LispObject;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author danilo
 */
@FunctionalInterface
public interface SpecialForm extends Function<LispObject, Supplier<LispObject>> {

}
