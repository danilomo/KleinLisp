/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.specialforms;

import java.util.Optional;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.Function;

/**
 *
 * @author Danilo Oliveira
 */
public class SpecialForm {
    public static Optional<Function> of(String str, Environment env){
        switch(str){
            case "quote": return Optional.of(QuoteForm.instance());
            case "if" : return Optional.of(IfForm.instance());
            case "lambda": return Optional.of(new LambdaForm(env));
            case "set!": return Optional.of(new SetForm(env));
            case "begin": return Optional.of(BeginForm.instance());
        }
        
        return Optional.empty();
    }
}
