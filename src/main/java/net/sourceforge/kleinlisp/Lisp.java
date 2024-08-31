package net.sourceforge.kleinlisp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.parser.SourceLexicalAnalyzer;
import net.sourceforge.kleinlisp.parser.java_cup.Symbol;
import net.sourceforge.kleinlisp.parser.sym;

/**
 * @author daolivei
 */
public class Lisp {

    private final LispEnvironment environment;
    private final Parser parser;
    private final Evaluator evaluator;

    public Lisp() {
        environment = new LispEnvironment();
        parser = Parser.defaultParser();
        evaluator = new Evaluator(environment);
    }

    public LispObject parse(String expression) {
        return parser.parse(expression, environment);
    }

    public LispObject evaluate(String expression) {
        return evaluator.evaluate(parse(expression));
    }

    public LispEnvironment environment() {
        return environment;
    }

    public Evaluator evaluator() {
        return evaluator;
    }

    public void runFile(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            SourceLexicalAnalyzer sla = new SourceLexicalAnalyzer(in);

            while (true) {
                Symbol s = sla.next_token();
                if (s.value == null) {
                    return;
                }                                
                
                evaluate(s.value.toString());
                if (s.sym == sym.EOF) {
                    return;
                }
            }
        }
    }

}
