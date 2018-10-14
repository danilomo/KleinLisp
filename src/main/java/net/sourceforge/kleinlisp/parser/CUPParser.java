package net.sourceforge.kleinlisp.parser;

import net.sourceforge.kleinlisp.Parser;
import java.io.ByteArrayInputStream;
import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.Form;
import net.sourceforge.kleinlisp.ListForm;

/**
 *
 * @author daolivei
 */
public class CUPParser implements Parser{         

    @Override
    public Form parse(String expression, Environment env ) {
        try {
            ByteArrayInputStream in = 
                    new ByteArrayInputStream(expression.getBytes());
            parser p = new parser(new LexicalAnalyzer(in)).withEnvironment(env);

            return (Form) p.parse().value;
        } catch (Exception ex) {
            return ListForm.NIL;
        }
    }

}
