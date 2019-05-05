package net.sourceforge.kleinlisp;

/**
 *
 * @author daolivei
 */
public class Lisp {

    private final Environment environment;
    private final Parser parser;

    public Lisp() {
        this.environment = new LispEnvironment();
        this.parser = Parser.defaultParser();
    }

    public LispObject parse(String expression) {
        return parser.parse(expression, environment);
    }

    public Environment environment() {
        return environment;
    }

}
