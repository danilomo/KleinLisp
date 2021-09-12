package net.sourceforge.kleinlisp;

import net.sourceforge.kleinlisp.evaluator.Evaluator;

/**
 * @author daolivei
 */
public class Lisp {

    private final Environment environment;
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

    public Environment environment() {
        return environment;
    }

}
