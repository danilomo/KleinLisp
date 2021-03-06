package net.sourceforge.kleinlisp;

import net.sourceforge.kleinlisp.parser.CUPParser;

/**
 *
 * @author daolivei
 */
public interface Parser {
    public LispObject parse( String expression, Environment env );
    
    public static Parser defaultParser(){
        return new CUPParser();
    }
}
