package mips;
import Tree.*;

public class InFrame extends frame.Access{
    int offset;
    InFrame(int o) {
	offset = o;
    }

    public Tree.Exp exp(Tree.Exp fp) {
        return new MEM(new BINOP(BINOP.PLUS, fp, new CONST(offset)));
    }

    public String toString() {
        Integer offset = new Integer(this.offset);
	return offset.toString();
    }
}
