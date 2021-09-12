package net.sourceforge.kleinlisp;

import net.sourceforge.kleinlisp.parser.CUPParser;

/**
 * @author daolivei
 */
public interface Parser {
    static Parser defaultParser() {
        return new CUPParser();
    }

    LispObject parse(String expression, Environment env);
}
