package Translate;

import Tree.*;

public class Ex extends Exp {
    Tree.Exp exp;

    public Ex(Tree.Exp e) {
        exp = e;
    }

    public Tree.Exp unEx() {
        return exp;
    }

    public Tree.Stm unNx() {
        return new EXP1(exp);
    }

    public Cx unCx() {
        return new Cx((t, f) -> new CJUMP(CJUMP.NE, exp, new CONST(0), t, f));
    }
}
