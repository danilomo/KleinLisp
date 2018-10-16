package net.sourceforge.kleinlisp;

/**
 *
 * @author daolivei
 */
public class Lisp {
    private final LispEnv environment;
    private final Parser parser;

    public Lisp() {
        this.environment = new LispEnv();
        this.parser = Parser.defaultParser();
    }

    public Lisp(LispEnv environment, Parser parser) {
        this.environment = environment;
        this.parser = parser;
    }

    public LispObject evaluate(String expression) {
        return parser.parse(expression, environment).evaluate();
    }

    public LispObject parse(String expression) {
        return parser.parse(expression, environment);
    }
    
    public void addClass(Class clazz){
        environment.addClass(clazz);
    }

}
