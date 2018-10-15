/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp;

import net.sourceforge.kleinlisp.forms.ListForm;

/**
 *
 * @author daolivei
 */
public interface Function {
    public Form evaluate(ListForm parameters);
}
