//----------------------------------------------------
// The following code was generated by CUP v0.11a beta 20060608
// Sun May 05 18:49:13 CEST 2019
//----------------------------------------------------

package net.sourceforge.kleinlisp.parser;

import net.sourceforge.kleinlisp.Environment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.DoubleObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.StringObject;
import net.sourceforge.kleinlisp.parser.java_cup.Scanner;
import net.sourceforge.kleinlisp.parser.java_cup.Symbol;
import net.sourceforge.kleinlisp.parser.java_cup.SymbolFactory;
import net.sourceforge.kleinlisp.parser.java_cup.lr_parser;

/**
 * CUP v0.11a beta 20060608 generated parser.
 *
 * @version Sun May 05 18:49:13 CEST 2019
 */
public class parser extends lr_parser {

    /**
     * Production table.
     */
    protected static final short[][] _production_table =
            unpackFromStrings(new String[]{
                    "\000\015\000\002\002\004\000\002\006\003\000\002\006" +
                            "\003\000\002\006\003\000\002\002\003\000\002\002\003" +
                            "\000\002\002\003\000\002\002\003\000\002\003\005\000" +
                            "\002\003\004\000\002\004\004\000\002\004\003\000\002" +
                            "\005\004"});
    /**
     * Parse-action table.
     */
    protected static final short[][] _action_table =
            unpackFromStrings(new String[]{
                    "\000\022\000\016\004\011\005\010\006\014\007\004\013" +
                            "\007\014\006\001\002\000\020\004\011\005\010\006\014" +
                            "\007\004\010\021\013\007\014\006\001\002\000\004\002" +
                            "\017\001\002\000\022\002\ufffc\004\ufffc\005\ufffc\006\ufffc" +
                            "\007\ufffc\010\ufffc\013\ufffc\014\ufffc\001\002\000\022\002" +
                            "\ufffd\004\ufffd\005\ufffd\006\ufffd\007\ufffd\010\ufffd\013\ufffd" +
                            "\014\ufffd\001\002\000\022\002\ufffb\004\ufffb\005\ufffb\006" +
                            "\ufffb\007\ufffb\010\ufffb\013\ufffb\014\ufffb\001\002\000\022" +
                            "\002\ufffa\004\ufffa\005\ufffa\006\ufffa\007\ufffa\010\ufffa\013" +
                            "\ufffa\014\ufffa\001\002\000\022\002\000\004\000\005\000" +
                            "\006\000\007\000\010\000\013\000\014\000\001\002\000" +
                            "\022\002\uffff\004\uffff\005\uffff\006\uffff\007\uffff\010\uffff" +
                            "\013\uffff\014\uffff\001\002\000\016\004\011\005\010\006" +
                            "\014\007\004\013\007\014\006\001\002\000\022\002\ufffe" +
                            "\004\ufffe\005\ufffe\006\ufffe\007\ufffe\010\ufffe\013\ufffe\014" +
                            "\ufffe\001\002\000\022\002\ufff5\004\ufff5\005\ufff5\006\ufff5" +
                            "\007\ufff5\010\ufff5\013\ufff5\014\ufff5\001\002\000\004\002" +
                            "\001\001\002\000\020\004\011\005\010\006\014\007\004" +
                            "\010\ufff6\013\007\014\006\001\002\000\022\002\ufff8\004" +
                            "\ufff8\005\ufff8\006\ufff8\007\ufff8\010\ufff8\013\ufff8\014\ufff8" +
                            "\001\002\000\004\010\023\001\002\000\022\002\ufff9\004" +
                            "\ufff9\005\ufff9\006\ufff9\007\ufff9\010\ufff9\013\ufff9\014\ufff9" +
                            "\001\002\000\004\010\ufff7\001\002"});
    /**
     * <code>reduce_goto</code> table.
     */
    protected static final short[][] _reduce_table =
            unpackFromStrings(new String[]{
                    "\000\022\000\012\002\011\003\012\005\014\006\004\001" +
                            "\001\000\014\002\011\003\012\004\021\005\014\006\017" +
                            "\001\001\000\002\001\001\000\002\001\001\000\002\001" +
                            "\001\000\002\001\001\000\002\001\001\000\002\001\001" +
                            "\000\002\001\001\000\012\002\011\003\012\005\014\006" +
                            "\015\001\001\000\002\001\001\000\002\001\001\000\002" +
                            "\001\001\000\014\002\011\003\012\004\023\005\014\006" +
                            "\017\001\001\000\002\001\001\000\002\001\001\000\002" +
                            "\001\001\000\002\001\001"});
    /**
     * Instance of action encapsulation class.
     */
    protected CUP$parser$actions action_obj;
    private Environment environment;

    /**
     * Default constructor.
     */
    public parser() {
        super();
    }

    /**
     * Constructor which sets the default scanner.
     */
    public parser(Scanner s) {
        super(s);
    }

    /**
     * Constructor which sets the default scanner.
     */
    public parser(Scanner s, SymbolFactory sf) {
        super(s, sf);
    }

    /**
     * Access to production table.
     */
    public short[][] production_table() {
        return _production_table;
    }

    /**
     * Access to parse-action table.
     */
    public short[][] action_table() {
        return _action_table;
    }

    /**
     * Access to <code>reduce_goto</code> table.
     */
    public short[][] reduce_table() {
        return _reduce_table;
    }

    /**
     * Action encapsulation object initializer.
     */
    protected void init_actions() {
        action_obj = new CUP$parser$actions(this);
    }

    /**
     * Invoke a user supplied parse action.
     */
    public Symbol do_action(
            int act_num,
            lr_parser parser,
            java.util.Stack stack,
            int top)
            throws java.lang.Exception {
        /* call code in generated class */
        return action_obj.CUP$parser$do_action(act_num, parser, stack, top);
    }

    /**
     * Indicates start state.
     */
    public int start_state() {
        return 0;
    }

    /**
     * Indicates start production.
     */
    public int start_production() {
        return 0;
    }

    /**
     * <code>EOF</code> Symbol index.
     */
    public int EOF_sym() {
        return 0;
    }

    /**
     * <code>error</code> Symbol index.
     */
    public int error_sym() {
        return 1;
    }

    public parser withEnvironment(Environment environment) {
        this.environment = environment;
        return this;
    }

    public Environment environment() {
        return environment;
    }

}

/**
 * Cup generated class to encapsulate user supplied action code.
 */
class CUP$parser$actions {
    private final parser parser;

    /**
     * Constructor
     */
    CUP$parser$actions(parser parser) {
        this.parser = parser;
    }

    /**
     * Method with the actual generated action code.
     */
    public final Symbol CUP$parser$do_action(
            int CUP$parser$act_num,
            lr_parser CUP$parser$parser,
            java.util.Stack CUP$parser$stack,
            int CUP$parser$top)
            throws java.lang.Exception {
        /* Symbol object for return from actions */
        Symbol CUP$parser$result;

        /* select the action based on the action number */
        switch (CUP$parser$act_num) {
            /*. . . . . . . . . . . . . . . . . . . .*/
            case 12: // quoted_form ::= QUOTE form
            {
                LispObject RESULT = null;
                int fleft = ((Symbol) CUP$parser$stack.peek()).left;
                int fright = ((Symbol) CUP$parser$stack.peek()).right;
                LispObject f = (LispObject) ((Symbol) CUP$parser$stack.peek()).value;
                RESULT = new ListObject(parser.environment().atomOf("quote"), new ListObject(f, ListObject.NIL));
                CUP$parser$result = parser.getSymbolFactory().newSymbol("quoted_form", 3, ((Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)), ((Symbol) CUP$parser$stack.peek()), RESULT);
            }
            return CUP$parser$result;

            /*. . . . . . . . . . . . . . . . . . . .*/
            case 11: // form_list ::= form
            {
                LispObject RESULT = null;
                int headleft = ((Symbol) CUP$parser$stack.peek()).left;
                int headright = ((Symbol) CUP$parser$stack.peek()).right;
                LispObject head = (LispObject) ((Symbol) CUP$parser$stack.peek()).value;
                RESULT = new ListObject(head, ListObject.NIL);
                CUP$parser$result = parser.getSymbolFactory().newSymbol("form_list", 2, ((Symbol) CUP$parser$stack.peek()), ((Symbol) CUP$parser$stack.peek()), RESULT);
            }
            return CUP$parser$result;

            /*. . . . . . . . . . . . . . . . . . . .*/
            case 10: // form_list ::= form form_list
            {
                LispObject RESULT = null;
                int headleft = ((Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).left;
                int headright = ((Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).right;
                LispObject head = (LispObject) ((Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).value;
                int tailleft = ((Symbol) CUP$parser$stack.peek()).left;
                int tailright = ((Symbol) CUP$parser$stack.peek()).right;
                LispObject tail = (LispObject) ((Symbol) CUP$parser$stack.peek()).value;
                RESULT = new ListObject(head, tail);
                CUP$parser$result = parser.getSymbolFactory().newSymbol("form_list", 2, ((Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)), ((Symbol) CUP$parser$stack.peek()), RESULT);
            }
            return CUP$parser$result;

            /*. . . . . . . . . . . . . . . . . . . .*/
            case 9: // list_form ::= OPEN_PAR CLOSE_PAR
            {
                LispObject RESULT = null;
                RESULT = ListObject.NIL;
                CUP$parser$result = parser.getSymbolFactory().newSymbol("list_form", 1, ((Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)), ((Symbol) CUP$parser$stack.peek()), RESULT);
            }
            return CUP$parser$result;

            /*. . . . . . . . . . . . . . . . . . . .*/
            case 8: // list_form ::= OPEN_PAR form_list CLOSE_PAR
            {
                LispObject RESULT = null;
                int lleft = ((Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).left;
                int lright = ((Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).right;
                LispObject l = (LispObject) ((Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).value;
                RESULT = l;
                CUP$parser$result = parser.getSymbolFactory().newSymbol("list_form", 1, ((Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)), ((Symbol) CUP$parser$stack.peek()), RESULT);
            }
            return CUP$parser$result;

            /*. . . . . . . . . . . . . . . . . . . .*/
            case 7: // self_evaluating_form ::= ATOM
            {
                LispObject RESULT = null;
                int aleft = ((Symbol) CUP$parser$stack.peek()).left;
                int aright = ((Symbol) CUP$parser$stack.peek()).right;
                String a = (String) ((Symbol) CUP$parser$stack.peek()).value;
                RESULT = parser.environment().atomOf(a);
                CUP$parser$result = parser.getSymbolFactory().newSymbol("self_evaluating_form", 0, ((Symbol) CUP$parser$stack.peek()), ((Symbol) CUP$parser$stack.peek()), RESULT);
            }
            return CUP$parser$result;

            /*. . . . . . . . . . . . . . . . . . . .*/
            case 6: // self_evaluating_form ::= STRING_LITERAL
            {
                LispObject RESULT = null;
                int sleft = ((Symbol) CUP$parser$stack.peek()).left;
                int sright = ((Symbol) CUP$parser$stack.peek()).right;
                String s = (String) ((Symbol) CUP$parser$stack.peek()).value;
                RESULT = new StringObject(s);
                CUP$parser$result = parser.getSymbolFactory().newSymbol("self_evaluating_form", 0, ((Symbol) CUP$parser$stack.peek()), ((Symbol) CUP$parser$stack.peek()), RESULT);
            }
            return CUP$parser$result;

            /*. . . . . . . . . . . . . . . . . . . .*/
            case 5: // self_evaluating_form ::= DOUBLE_LITERAL
            {
                LispObject RESULT = null;
                int dleft = ((Symbol) CUP$parser$stack.peek()).left;
                int dright = ((Symbol) CUP$parser$stack.peek()).right;
                Double d = (Double) ((Symbol) CUP$parser$stack.peek()).value;
                RESULT = new DoubleObject(d);
                CUP$parser$result = parser.getSymbolFactory().newSymbol("self_evaluating_form", 0, ((Symbol) CUP$parser$stack.peek()), ((Symbol) CUP$parser$stack.peek()), RESULT);
            }
            return CUP$parser$result;

            /*. . . . . . . . . . . . . . . . . . . .*/
            case 4: // self_evaluating_form ::= INT_LITERAL
            {
                LispObject RESULT = null;
                int ileft = ((Symbol) CUP$parser$stack.peek()).left;
                int iright = ((Symbol) CUP$parser$stack.peek()).right;
                Integer i = (Integer) ((Symbol) CUP$parser$stack.peek()).value;
                RESULT = new IntObject(i);
                CUP$parser$result = parser.getSymbolFactory().newSymbol("self_evaluating_form", 0, ((Symbol) CUP$parser$stack.peek()), ((Symbol) CUP$parser$stack.peek()), RESULT);
            }
            return CUP$parser$result;

            /*. . . . . . . . . . . . . . . . . . . .*/
            case 3: // form ::= quoted_form
            {
                LispObject RESULT = null;
                int fleft = ((Symbol) CUP$parser$stack.peek()).left;
                int fright = ((Symbol) CUP$parser$stack.peek()).right;
                LispObject f = (LispObject) ((Symbol) CUP$parser$stack.peek()).value;
                RESULT = f;
                CUP$parser$result = parser.getSymbolFactory().newSymbol("form", 4, ((Symbol) CUP$parser$stack.peek()), ((Symbol) CUP$parser$stack.peek()), RESULT);
            }
            return CUP$parser$result;

            /*. . . . . . . . . . . . . . . . . . . .*/
            case 2: // form ::= list_form
            {
                LispObject RESULT = null;
                int fleft = ((Symbol) CUP$parser$stack.peek()).left;
                int fright = ((Symbol) CUP$parser$stack.peek()).right;
                LispObject f = (LispObject) ((Symbol) CUP$parser$stack.peek()).value;
                RESULT = f;
                CUP$parser$result = parser.getSymbolFactory().newSymbol("form", 4, ((Symbol) CUP$parser$stack.peek()), ((Symbol) CUP$parser$stack.peek()), RESULT);
            }
            return CUP$parser$result;

            /*. . . . . . . . . . . . . . . . . . . .*/
            case 1: // form ::= self_evaluating_form
            {
                LispObject RESULT = null;
                int fleft = ((Symbol) CUP$parser$stack.peek()).left;
                int fright = ((Symbol) CUP$parser$stack.peek()).right;
                LispObject f = (LispObject) ((Symbol) CUP$parser$stack.peek()).value;
                RESULT = f;
                CUP$parser$result = parser.getSymbolFactory().newSymbol("form", 4, ((Symbol) CUP$parser$stack.peek()), ((Symbol) CUP$parser$stack.peek()), RESULT);
            }
            return CUP$parser$result;

            /*. . . . . . . . . . . . . . . . . . . .*/
            case 0: // $START ::= form EOF
            {
                Object RESULT = null;
                int start_valleft = ((Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).left;
                int start_valright = ((Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).right;
                LispObject start_val = (LispObject) ((Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).value;
                RESULT = start_val;
                CUP$parser$result = parser.getSymbolFactory().newSymbol("$START", 0, ((Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)), ((Symbol) CUP$parser$stack.peek()), RESULT);
            }
            /* ACCEPT */
            CUP$parser$parser.done_parsing();
            return CUP$parser$result;

            /* . . . . . .*/
            default:
                throw new Exception(
                        "Invalid action number found in internal parse table");

        }
    }
}

