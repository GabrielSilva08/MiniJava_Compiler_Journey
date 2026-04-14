package mips;

import Assem.*;
import Temp.*;
import Tree.*;
import frame.Frame;

public class Codegen {
    private Frame frame;
    private InstrList ilist = null, last = null;

    public Codegen(Frame f) { frame = f; }

    private void emit(Instr inst) {
        if (last != null)
            last = last.tail = new InstrList(inst, null);
        else
            last = ilist = new InstrList(inst, null);
    }

    public InstrList codegen(Stm s) {
        munchStm(s);
        InstrList result = ilist;
        ilist = last = null;
        return result;
    }

    private void munchStm(Stm s) {
        if (s instanceof Tree.MOVE) munchMove((Tree.MOVE)s);
        else if (s instanceof SEQ) {
            munchStm(((SEQ)s).left);
            munchStm(((SEQ)s).right);
        }
        else if (s instanceof Tree.LABEL) {
            Label l = ((Tree.LABEL)s).label;
            emit(new Assem.LABEL(String.format("%s:\n", l), l));
        }
        else if (s instanceof JUMP) {
            Exp e = ((JUMP)s).exp;
            if (e instanceof NAME) {
                String target = ((NAME)e).label.toString();
                emit(new OPER("j " + target, null, null, new LabelList(((NAME)e).label, null)));
            }
        }
        else if (s instanceof CJUMP) {
            CJUMP c = (CJUMP)s;
            Temp t1 = munchExp(c.left), t2 = munchExp(c.right);
            String op = switch (c.relop) {
                case CJUMP.EQ -> "beq"; case CJUMP.NE -> "bne";
                case CJUMP.LT -> "blt"; case CJUMP.LE -> "ble";
                case CJUMP.GT -> "bgt"; case CJUMP.GE -> "bge";
                default -> "beq";
            };
            emit(new OPER(op + " `s0, `s1, " + c.iftrue,
                null, new TempList(t1, new TempList(t2, null)),
                new LabelList(c.iftrue, null)));
        }
    }

    private void munchMove(Tree.MOVE s){
        Exp dst = s.dst, src = s.src;

        if (dst instanceof MEM && src instanceof CONST) {
            MEM m = (MEM)dst;
            CONST c = (CONST)src;
            Temp addr = munchExp(m.exp);
            emit(new OPER(String.format("sw %d(`s0)", c.value),
                null, new TempList(addr, null)));
        }
        else if (dst instanceof MEM && src instanceof MEM) {
            MEM d = (MEM)dst, m = (MEM)src;
            Temp daddr = munchExp(d.exp), saddr = munchExp(m.exp);
            emit(new OPER("lw `d0, 0(`s1)", new TempList(new Temp(), null),
                new TempList(saddr, null)));
            emit(new OPER("sw `s0, 0(`s1)", null,
                new TempList(new Temp(), new TempList(daddr, null))));
        }
        else {
            Temp td = munchExp(dst), ts = munchExp(src);
            emit(new Assem.MOVE("move `d0, `s0", td, ts));
        }
    }

    public static Temp[] toArray(TempList list) {
        // Conta o tamanho
        int count = 0;
        for (TempList l = list; l != null; l = l.tail) count++;
        Temp[] arr = new Temp[count];
        int i = 0;
        for (TempList l = list; l != null; l = l.tail) {
            arr[i++] = l.head;
        }
        return arr;
    }

    private Temp munchExp(Exp e) {
        if (e instanceof CONST) {
            CONST c = (CONST)e;
            Temp r = new Temp();
            emit(new OPER("li `d0, " + c.value,
                new TempList(r, null), null));
            return r;
        }
        else if (e instanceof NAME) {
            NAME n = (NAME)e;
            Temp r = new Temp();
            emit(new OPER("la `d0, " + n.label,
                new TempList(r, null), null));
            return r;
        }
        else if (e instanceof TEMP) {
            return ((TEMP)e).temp;
        }
        else if (e instanceof BINOP) {
            BINOP b = (BINOP) e;
            Temp r = new Temp();
            Temp l = munchExp(b.left), r2 = munchExp(b.right);
            String op = switch (b.binop) {
                case BINOP.PLUS -> "add"; case BINOP.MINUS -> "sub";
                case BINOP.MUL -> "mul"; default -> "add";
            };
            emit(new OPER(op + " `d0, `s0, `s1",
                new TempList(r, null),
                new TempList(l, new TempList(r2, null))
            ));
            return r;
        }
        else if (e instanceof MEM) {
            MEM m = (MEM)e;
            Temp r = new Temp();
            Temp addr = munchExp(m.exp);
            emit(new OPER("lw `d0, 0(`s0)",
                new TempList(r, null), new TempList(addr, null)));
            return r;
        }

        // fallback
        Temp r = new Temp();
        emit(new OPER("nop", new TempList(r, null), null));
        return r;
    }
}
