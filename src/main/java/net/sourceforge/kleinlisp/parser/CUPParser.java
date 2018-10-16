package net.sourceforge.kleinlisp.parser;

import net.sourceforge.kleinlisp.Parser;
import java.io.ByteArrayInputStream;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.LispObject;

/**
 *
 * @author daolivei
 */
public class CUPParser implements Parser{         

    @Override
    public LispObject parse(String expression, Environment env ) {
        try {
            ByteArrayInputStream in = 
                    new ByteArrayInputStream(expression.getBytes());
            parser p = new parser(new LexicalAnalyzer(in)).withEnvironment(env);

            return (LispObject) p.parse().value;
        } catch (Exception ex) {
            return ListObject.NIL;
        }
    }

}
