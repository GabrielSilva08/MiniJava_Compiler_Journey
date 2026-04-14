package mips;
import Temp.Temp;
import Tree.*;

public class InReg extends frame.Access {
    Temp temp;
    InReg(Temp t) {
	temp = t;
    }

    public Tree.Exp exp(Tree.Exp fp) {
        return new TEMP(temp);
    }

    public String toString() {
        return temp.toString();
    }
}
