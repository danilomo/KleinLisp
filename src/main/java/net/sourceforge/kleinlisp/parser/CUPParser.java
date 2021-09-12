package net.sourceforge.kleinlisp.parser;

import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.Parser;
import net.sourceforge.kleinlisp.objects.ErrorObject;

import java.io.ByteArrayInputStream;

/**
 * @author daolivei
 */
public class CUPParser implements Parser {

    @Override
    public LispObject parse(String expression, Environment env) {
        try {
            ByteArrayInputStream in =
                    new ByteArrayInputStream(expression.getBytes());
            parser p = new parser(new LexicalAnalyzer(in)).withEnvironment(env);

            return (LispObject) p.parse().value;
        } catch (Exception ex) {
            return new ErrorObject(ex);
        }
    }

}
