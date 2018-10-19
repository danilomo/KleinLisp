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

    public Lisp(LispEnv environment, Parser parser) {
        this.environment = environment;
        this.parser = parser;
    }

    public LispObject evaluate(String expression) {
        return parse(expression).evaluate();
    }

    public LispObject parse(String expression) {
        return parser.parse(expression, environment);
    }

    public void addClass(Class clazz) {
        ((LispEnvironment) environment).addClass(clazz);
    }

    public Environment environment() {
        return environment;
    }

}
