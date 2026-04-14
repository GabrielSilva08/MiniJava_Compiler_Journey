package Translate;

import Tree.*;

public class Nx extends Exp {
    Tree.Stm stm;

    public Nx(Tree.Stm s) {
        stm = s;
    }

    public Tree.Exp unEx() {
        return new ESEQ(stm, new CONST(0)); // retorna um valor default
    }

    public Tree.Stm unNx() {
        return stm;
    }

    public Cx unCx() {
        throw new Error("Nx can't be converted to Cx");
    }
}
