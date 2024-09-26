// ----------------------------------------------------
// The following code was generated by CUP v0.11a beta 20060608
// Wed Sep 18 22:11:43 CEST 2024
// ----------------------------------------------------

package net.sourceforge.kleinlisp.parser;

import net.sourceforge.kleinlisp.*;
import net.sourceforge.kleinlisp.objects.*;

/**
 * CUP v0.11a beta 20060608 generated parser.
 *
 * @version Wed Sep 18 22:11:43 CEST 2024
 */
@SuppressWarnings("all")
public class parser extends java_cup.runtime.lr_parser {

  /** Default constructor. */
  public parser() {
    super();
  }

  /** Constructor which sets the default scanner. */
  public parser(java_cup.runtime.Scanner s) {
    super(s);
  }

  /** Constructor which sets the default scanner. */
  public parser(java_cup.runtime.Scanner s, java_cup.runtime.SymbolFactory sf) {
    super(s, sf);
  }

  /** Production table. */
  protected static final short _production_table[][] =
      unpackFromStrings(
          new String[] {
            "\000\017\000\002\002\004\000\002\006\003\000\002\006"
                + "\003\000\002\006\003\000\002\002\003\000\002\002\003"
                + "\000\002\002\003\000\002\002\003\000\002\003\005\000"
                + "\002\003\007\000\002\003\004\000\002\004\004\000\002"
                + "\004\003\000\002\004\003\000\002\005\004"
          });

  /** Access to production table. */
  public short[][] production_table() {
    return _production_table;
  }

  /** Parse-action table. */
  protected static final short[][] _action_table =
      unpackFromStrings(
          new String[] {
            "\000\026\000\016\004\011\005\010\006\014\007\004\014"
                + "\007\015\006\001\002\000\022\003\022\004\011\005\010"
                + "\006\014\007\004\010\021\014\007\015\006\001\002\000"
                + "\004\002\017\001\002\000\026\002\ufffc\003\ufffc\004\ufffc"
                + "\005\ufffc\006\ufffc\007\ufffc\010\ufffc\013\ufffc\014\ufffc\015"
                + "\ufffc\001\002\000\026\002\ufffd\003\ufffd\004\ufffd\005\ufffd"
                + "\006\ufffd\007\ufffd\010\ufffd\013\ufffd\014\ufffd\015\ufffd\001"
                + "\002\000\026\002\ufffb\003\ufffb\004\ufffb\005\ufffb\006\ufffb"
                + "\007\ufffb\010\ufffb\013\ufffb\014\ufffb\015\ufffb\001\002\000"
                + "\026\002\ufffa\003\ufffa\004\ufffa\005\ufffa\006\ufffa\007\ufffa"
                + "\010\ufffa\013\ufffa\014\ufffa\015\ufffa\001\002\000\026\002"
                + "\000\003\000\004\000\005\000\006\000\007\000\010\000"
                + "\013\000\014\000\015\000\001\002\000\026\002\uffff\003"
                + "\uffff\004\uffff\005\uffff\006\uffff\007\uffff\010\uffff\013\uffff"
                + "\014\uffff\015\uffff\001\002\000\016\004\011\005\010\006"
                + "\014\007\004\014\007\015\006\001\002\000\026\002\ufffe"
                + "\003\ufffe\004\ufffe\005\ufffe\006\ufffe\007\ufffe\010\ufffe\013"
                + "\ufffe\014\ufffe\015\ufffe\001\002\000\026\002\ufff3\003\ufff3"
                + "\004\ufff3\005\ufff3\006\ufff3\007\ufff3\010\ufff3\013\ufff3\014"
                + "\ufff3\015\ufff3\001\002\000\004\002\001\001\002\000\024"
                + "\003\022\004\011\005\010\006\014\007\004\010\ufff5\013"
                + "\ufff5\014\007\015\006\001\002\000\026\002\ufff7\003\ufff7"
                + "\004\ufff7\005\ufff7\006\ufff7\007\ufff7\010\ufff7\013\ufff7\014"
                + "\ufff7\015\ufff7\001\002\000\006\010\ufff4\013\ufff4\001\002"
                + "\000\006\010\024\013\025\001\002\000\026\002\ufff9\003"
                + "\ufff9\004\ufff9\005\ufff9\006\ufff9\007\ufff9\010\ufff9\013\ufff9"
                + "\014\ufff9\015\ufff9\001\002\000\016\004\011\005\010\006"
                + "\014\007\004\014\007\015\006\001\002\000\004\010\027"
                + "\001\002\000\026\002\ufff8\003\ufff8\004\ufff8\005\ufff8\006"
                + "\ufff8\007\ufff8\010\ufff8\013\ufff8\014\ufff8\015\ufff8\001\002"
                + "\000\006\010\ufff6\013\ufff6\001\002"
          });

  /** Access to parse-action table. */
  public short[][] action_table() {
    return _action_table;
  }

  /** <code>reduce_goto</code> table. */
  protected static final short[][] _reduce_table =
      unpackFromStrings(
          new String[] {
            "\000\026\000\012\002\011\003\012\005\014\006\004\001"
                + "\001\000\014\002\011\003\012\004\022\005\014\006\017"
                + "\001\001\000\002\001\001\000\002\001\001\000\002\001"
                + "\001\000\002\001\001\000\002\001\001\000\002\001\001"
                + "\000\002\001\001\000\012\002\011\003\012\005\014\006"
                + "\015\001\001\000\002\001\001\000\002\001\001\000\002"
                + "\001\001\000\014\002\011\003\012\004\027\005\014\006"
                + "\017\001\001\000\002\001\001\000\002\001\001\000\002"
                + "\001\001\000\002\001\001\000\012\002\011\003\012\005"
                + "\014\006\025\001\001\000\002\001\001\000\002\001\001"
                + "\000\002\001\001"
          });

  /** Access to <code>reduce_goto</code> table. */
  public short[][] reduce_table() {
    return _reduce_table;
  }

  /** Instance of action encapsulation class. */
  protected CUP$parser$actions action_obj;

  /** Action encapsulation object initializer. */
  protected void init_actions() {
    action_obj = new CUP$parser$actions(this);
  }

  /** Invoke a user supplied parse action. */
  public java_cup.runtime.Symbol do_action(
      int act_num, java_cup.runtime.lr_parser parser, java.util.Stack stack, int top)
      throws java.lang.Exception {
    /* call code in generated class */
    return action_obj.CUP$parser$do_action(act_num, parser, stack, top);
  }

  /** Indicates start state. */
  public int start_state() {
    return 0;
  }

  /** Indicates start production. */
  public int start_production() {
    return 0;
  }

  /** <code>EOF</code> Symbol index. */
  public int EOF_sym() {
    return 0;
  }

  /** <code>error</code> Symbol index. */
  public int error_sym() {
    return 1;
  }

  private LispEnvironment environment;

  public parser withEnvironment(LispEnvironment environment) {
    this.environment = environment;
    return this;
  }

  public LispEnvironment environment() {
    return environment;
  }
}

/** Cup generated class to encapsulate user supplied action code. */
@SuppressWarnings("all")
class CUP$parser$actions {
  private final parser parser;

  /** Constructor */
  CUP$parser$actions(parser parser) {
    this.parser = parser;
  }

  /** Method with the actual generated action code. */
  public final java_cup.runtime.Symbol CUP$parser$do_action(
      int CUP$parser$act_num,
      java_cup.runtime.lr_parser CUP$parser$parser,
      java.util.Stack CUP$parser$stack,
      int CUP$parser$top)
      throws java.lang.Exception {
    /* Symbol object for return from actions */
    java_cup.runtime.Symbol CUP$parser$result;

    /* select the action based on the action number */
    switch (CUP$parser$act_num) {
        /*. . . . . . . . . . . . . . . . . . . .*/
      case 14: // quoted_form ::= QUOTE form
        {
          LispObject RESULT = null;
          int fleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
          int fright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
          LispObject f = (LispObject) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
          RESULT =
              new ListObject(
                  parser.environment().atomOf("quote"), new ListObject(f, ListObject.NIL));
          CUP$parser$result =
              parser
                  .getSymbolFactory()
                  .newSymbol(
                      "quoted_form",
                      3,
                      ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)),
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      RESULT);
        }
        return CUP$parser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
      case 13: // form_list ::= error
        {
          LispObject RESULT = null;
          int eleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
          int eright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
          Object e = (Object) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
          parser.report_error("Syntax error, skip rest", e);
          CUP$parser$result =
              parser
                  .getSymbolFactory()
                  .newSymbol(
                      "form_list",
                      2,
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      RESULT);
        }
        return CUP$parser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
      case 12: // form_list ::= form
        {
          LispObject RESULT = null;
          int headleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
          int headright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
          LispObject head = (LispObject) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
          RESULT = new ListObject(head, ListObject.NIL);
          CUP$parser$result =
              parser
                  .getSymbolFactory()
                  .newSymbol(
                      "form_list",
                      2,
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      RESULT);
        }
        return CUP$parser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
      case 11: // form_list ::= form form_list
        {
          LispObject RESULT = null;
          int headleft =
              ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).left;
          int headright =
              ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).right;
          LispObject head =
              (LispObject)
                  ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).value;
          int tailleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
          int tailright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
          LispObject tail = (LispObject) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
          RESULT = new ListObject(head, tail);
          CUP$parser$result =
              parser
                  .getSymbolFactory()
                  .newSymbol(
                      "form_list",
                      2,
                      ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)),
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      RESULT);
        }
        return CUP$parser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
      case 10: // list_form ::= OPEN_PAR CLOSE_PAR
        {
          LispObject RESULT = null;
          RESULT = ListObject.NIL;
          CUP$parser$result =
              parser
                  .getSymbolFactory()
                  .newSymbol(
                      "list_form",
                      1,
                      ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)),
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      RESULT);
        }
        return CUP$parser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
      case 9: // list_form ::= OPEN_PAR form_list DOT form CLOSE_PAR
        {
          LispObject RESULT = null;
          int lleft =
              ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 3)).left;
          int lright =
              ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 3)).right;
          LispObject l =
              (LispObject)
                  ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 3)).value;
          int lastleft =
              ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).left;
          int lastright =
              ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).right;
          LispObject last =
              (LispObject)
                  ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).value;
          ((ListObject) l).last().setTail(last);
          RESULT = l;
          CUP$parser$result =
              parser
                  .getSymbolFactory()
                  .newSymbol(
                      "list_form",
                      1,
                      ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 4)),
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      RESULT);
        }
        return CUP$parser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
      case 8: // list_form ::= OPEN_PAR form_list CLOSE_PAR
        {
          LispObject RESULT = null;
          int lleft =
              ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).left;
          int lright =
              ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).right;
          LispObject l =
              (LispObject)
                  ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).value;
          RESULT = l;
          CUP$parser$result =
              parser
                  .getSymbolFactory()
                  .newSymbol(
                      "list_form",
                      1,
                      ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)),
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      RESULT);
        }
        return CUP$parser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
      case 7: // self_evaluating_form ::= ATOM
        {
          LispObject RESULT = null;
          int aleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
          int aright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
          String a = (String) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;

          AtomObject atom = parser.environment().atomOf(a);
          RESULT = new IdentifierObject(atom, aleft, aright);

          CUP$parser$result =
              parser
                  .getSymbolFactory()
                  .newSymbol(
                      "self_evaluating_form",
                      0,
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      RESULT);
        }
        return CUP$parser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
      case 6: // self_evaluating_form ::= STRING_LITERAL
        {
          LispObject RESULT = null;
          int sleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
          int sright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
          String s = (String) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
          RESULT = new StringObject(s);
          CUP$parser$result =
              parser
                  .getSymbolFactory()
                  .newSymbol(
                      "self_evaluating_form",
                      0,
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      RESULT);
        }
        return CUP$parser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
      case 5: // self_evaluating_form ::= DOUBLE_LITERAL
        {
          LispObject RESULT = null;
          int dleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
          int dright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
          Double d = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
          RESULT = new DoubleObject(d);
          CUP$parser$result =
              parser
                  .getSymbolFactory()
                  .newSymbol(
                      "self_evaluating_form",
                      0,
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      RESULT);
        }
        return CUP$parser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
      case 4: // self_evaluating_form ::= INT_LITERAL
        {
          LispObject RESULT = null;
          int ileft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
          int iright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
          Integer i = (Integer) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
          RESULT = new IntObject(i);
          CUP$parser$result =
              parser
                  .getSymbolFactory()
                  .newSymbol(
                      "self_evaluating_form",
                      0,
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      RESULT);
        }
        return CUP$parser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
      case 3: // form ::= quoted_form
        {
          LispObject RESULT = null;
          int fleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
          int fright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
          LispObject f = (LispObject) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
          RESULT = f;
          CUP$parser$result =
              parser
                  .getSymbolFactory()
                  .newSymbol(
                      "form",
                      4,
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      RESULT);
        }
        return CUP$parser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
      case 2: // form ::= list_form
        {
          LispObject RESULT = null;
          int fleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
          int fright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
          LispObject f = (LispObject) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
          RESULT = f;
          CUP$parser$result =
              parser
                  .getSymbolFactory()
                  .newSymbol(
                      "form",
                      4,
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      RESULT);
        }
        return CUP$parser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
      case 1: // form ::= self_evaluating_form
        {
          LispObject RESULT = null;
          int fleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
          int fright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
          LispObject f = (LispObject) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
          RESULT = f;
          CUP$parser$result =
              parser
                  .getSymbolFactory()
                  .newSymbol(
                      "form",
                      4,
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      RESULT);
        }
        return CUP$parser$result;

        /*. . . . . . . . . . . . . . . . . . . .*/
      case 0: // $START ::= form EOF
        {
          Object RESULT = null;
          int start_valleft =
              ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).left;
          int start_valright =
              ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).right;
          LispObject start_val =
              (LispObject)
                  ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).value;
          RESULT = start_val;
          CUP$parser$result =
              parser
                  .getSymbolFactory()
                  .newSymbol(
                      "$START",
                      0,
                      ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)),
                      ((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
                      RESULT);
        }
        /* ACCEPT */
        CUP$parser$parser.done_parsing();
        return CUP$parser$result;

        /* . . . . . .*/
      default:
        throw new Exception("Invalid action number found in internal parse table");
    }
  }
}
