/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sourceforge.kleinlisp.compiler;

import java.util.List;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.functional.Tuple2;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 *
 * @author daolivei
 */
public class CondForm extends LispObjDecorator {

    private final LispObject elseClause;
    private final List<Tuple2<LispObject, LispObject>> clauses;

    public CondForm( List<Tuple2<LispObject, LispObject>> clauses) {
        this(clauses, ListObject.NIL);
    }

    public CondForm( List<Tuple2<LispObject, LispObject>> clauses, LispObject elseClause) {
        this.clauses = clauses;
        this.elseClause = elseClause;
    }

    @Override
    public LispObject evaluate() {

        for (Tuple2<LispObject, LispObject> clause : clauses) {
            if (clause.first().evaluate().truthness()) {
                return clause.second().evaluate();
            }
        }

        return elseClause.evaluate();

    }

}
