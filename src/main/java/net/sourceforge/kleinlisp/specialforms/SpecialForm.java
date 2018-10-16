/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.specialforms;

import java.util.Optional;
import net.sourceforge.kleinlisp.Function;

/**
 *
 * @author Danilo Oliveira
 */
public class SpecialForm {
    public static Optional<Function> of(String str){
        switch(str){
            case "quote": return Optional.of(QuoteForm.instance());
            case "if" : return Optional.of(IfForm.instance());
        }
        
        return Optional.empty();
    }
}
