package frame;
import java.util.List;
import semantic.SymbolTable.Symbol;

public abstract class Frame implements Temp.TempMap {
    public Temp.Label name;
    public List<Access> formals;
    public abstract Frame newFrame(Symbol name, List<Boolean> formals);
    public abstract Access allocLocal(boolean escape);
    public abstract Temp.Temp FP();
    public abstract int wordSize();
    public abstract Tree.Exp externalCall(String func, List<Tree.Exp> args);
    public abstract Temp.Temp RV();
    public abstract String string(Temp.Label label, String value);
    public abstract Temp.Label badPtr();
    public abstract Temp.Label badSub();
    public abstract String tempMap(Temp.Temp temp);
    // public abstract List<Assem.Instr> codegen(List<Tree.Stm> stms);
    public abstract void procEntryExit1(List<Tree.Stm> body);               
    public abstract void procEntryExit2(List<Assem.Instr> body);
    public abstract void procEntryExit3(List<Assem.Instr> body);
    public abstract Temp.Temp[] registers();
    public abstract void spill(List<Assem.Instr> insns, Temp.Temp[] spills);
    public abstract String programTail(); //append to end of target code
}