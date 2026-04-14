package mips;

import Assem.*;
import Temp.Label;
import Temp.RegAllocConventions;
import Temp.Temp;
import Temp.TempList;
import frame.Access;
import frame.Frame;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import semantic.SymbolTable.Symbol;

public class MipsFrame extends Frame {
	/* Atributos do frame MIPS */

	// Tamanho de palavra na arquitetura MIPS (4 bytes)
    private static final int WORD_SIZE = 4;

	// Registradores especiais
	static final Temp ZERO = new Temp(); // zero reg
    static final Temp AT = new Temp(); // reserved for assembler
    static final Temp V0 = new Temp(); // function result
    static final Temp V1 = new Temp(); // second function result
    static final Temp A0 = new Temp(); // argument1
    static final Temp A1 = new Temp(); // argument2
    static final Temp A2 = new Temp(); // argument3
    static final Temp A3 = new Temp(); // argument4
    static final Temp T0 = new Temp(); // caller-saved
    static final Temp T1 = new Temp();
    static final Temp T2 = new Temp();
    static final Temp T3 = new Temp();
    static final Temp T4 = new Temp();
    static final Temp T5 = new Temp();
    static final Temp T6 = new Temp();
    static final Temp T7 = new Temp();
    static final Temp S0 = new Temp(); // callee-saved
    static final Temp S1 = new Temp();
    static final Temp S2 = new Temp();
    static final Temp S3 = new Temp();
    static final Temp S4 = new Temp();
    static final Temp S5 = new Temp();
    static final Temp S6 = new Temp();
    static final Temp S7 = new Temp();
    static final Temp T8 = new Temp(); // caller-saved
    static final Temp T9 = new Temp();
    static final Temp K0 = new Temp(); // reserved for OS kernel
    static final Temp K1 = new Temp(); // reserved for OS kernel
    static final Temp GP = new Temp(); // pointer to global area
    static final Temp SP = new Temp(); // stack pointer
    static final Temp S8 = new Temp(); // callee-save (frame pointer)
    static final Temp RA = new Temp(); // return address
	static final Temp FP = new Temp(); // virtual frame pointer (eliminated)

    // Register lists: must not overlap and must include every register that
    // might show up in code
    private static final Temp[]
	// registers dedicated to special purposes
	specialRegs = { ZERO, AT, K0, K1, GP, SP },
	// registers to pass outgoing arguments
	argRegs	= { A0, A1, A2, A3 },
    // registers that a callee must preserve for its caller
	calleeSaves = { RA, S0, S1, S2, S3, S4, S5, S6, S7, S8 },
	// registers that a callee may use without preserving
	callerSaves = { T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, V0, V1 };

	// Mapeamento de nomes para registradores
	private static final HashMap<Temp,String> tempMap = new HashMap<>();
    static {
    	tempMap.put(ZERO, "$0");
		tempMap.put(AT,   "$at");
		tempMap.put(V0,   "$v0");
		tempMap.put(V1,   "$v1");
		tempMap.put(A0,   "$a0");
		tempMap.put(A1,   "$a1");
		tempMap.put(A2,   "$a2");
		tempMap.put(A3,   "$a3");
		tempMap.put(T0,   "$t0");
		tempMap.put(T1,   "$t1");
		tempMap.put(T2,   "$t2");
		tempMap.put(T3,   "$t3");
		tempMap.put(T4,   "$t4");
		tempMap.put(T5,   "$t5");
		tempMap.put(T6,   "$t6");
		tempMap.put(T7,   "$t7");
		tempMap.put(S0,   "$s0");
		tempMap.put(S1,   "$s1");
		tempMap.put(S2,   "$s2");
		tempMap.put(S3,   "$s3");
		tempMap.put(S4,   "$s4");
		tempMap.put(S5,   "$s5");
		tempMap.put(S6,   "$s6");
		tempMap.put(S7,   "$s7");
		tempMap.put(T8,   "$t8");
		tempMap.put(T9,   "$t9");
		tempMap.put(K0,   "$k0");
		tempMap.put(K1,   "$k1");
		tempMap.put(GP,   "$gp");
		tempMap.put(SP,   "$sp");
		tempMap.put(S8,   "$fp");
		tempMap.put(RA,   "$ra");
		tempMap.put(FP,   "$fp"); // virtual frame pointer (eliminated)
    }

	// Nome do frame/função
	private Label name;
	
	// Labels for special purposes
	private static HashMap<String,Label> labels = new HashMap<>();

	// Label for bad pointer error (used in array access)
	private static final Label badPtr = new Label("BADPTR");

	// Label for bad subtraction error (used in array access)
    private static final Label badSub = new Label("BADSUB");

	// Lista de parâmetros formais e reais
	private List<Access> formals = new LinkedList<>();
    private List<Access> actuals = new LinkedList<>();

	// Offset para alocação de variáveis locais
	private int offset = 0;

	// Offset para alocação de variáveis locais
    private int maxArgOffset = 0;

	// Indica se o código gerado deve realizar spill (ainda não implementado)
    private static boolean spilling = true;

	/* Métodos do frame MIPS */ 

	// Construtor padrão
	public MipsFrame() {}

	// Construtor para criar um novo frame MIPS
	// com um nome e uma lista de booleanos indicando se os parâmetros escapam ou não
	public MipsFrame(Symbol n, List<Boolean> escapes) {
		name = new Label(n);
		int off = 0;
		Iterator<Boolean> it = escapes.iterator();
		// Aloca argumentos formais e reais conforme escapam ou não
		for (int i = 0; i < argRegs.length && it.hasNext(); i++) {
            boolean esc = it.next();
            actuals.add(new InReg(argRegs[i]));
            if (esc) {
                formals.add(new InFrame(off));
            } else {
                formals.add(new InReg(new Temp()));
            }
            off += WORD_SIZE;
        }while (it.hasNext()) {
        	boolean esc = it.next();
            actuals.add(new InFrame(off));
            if (esc) {
                formals.add(new InFrame(off));
            } else {
                formals.add(new InReg(new Temp()));
            }
            off += WORD_SIZE;
        }
		offset = -off;
        maxArgOffset = off;
    }

	// Métodos de acesso aos atributos do frame MIPS (getter methods)
	@Override
	public int wordSize() { return WORD_SIZE; }
	@Override
	public Temp FP() {
		return RegAllocConventions.FP;
	}

	@Override
	public Temp RV() {
		return RegAllocConventions.RV;
	}

	public Label badPtr() { return badPtr; }
    @Override
	public Label badSub() { return badSub;}
    public List<Access> formals() { return formals; }

	// Aloca variável local (em registrador ou na pilha)
	@Override
    public Access allocLocal(boolean escape) {
		if (escape) {
		    Access result = new InFrame(offset);
		    offset -= WORD_SIZE;
		    return result;
		} else
		    return new InReg(new Temp());
    }

    // Mapeamento de Temp para nome de registrador
	@Override
    public String tempMap(Temp temp) {
		return tempMap.getOrDefault(temp, temp.toString());
    }

	// Cria um novo frame com o nome fornecido e a lista de parâmetros formais
	@Override
    public Frame newFrame(Symbol name, List<Boolean> formals) {
        if (this.name != null) name = Symbol.symbol(this.name + "." + name);
        return new MipsFrame(name, formals);
    }

    // Implementação de externalCall para chamadas externas
	@Override
    public Tree.Exp externalCall(String s, List<Tree.Exp> args){
		// Verifica se o nome da função já está mapeado para um label, se não estiver, cria um novo label com o nome da função
		String func = s.intern();
		Label l = labels.get(func);
		if (l == null) {
	    	l = new Label("_" + func);
	    	labels.put(func, l);
		}
		// Cria uma lista de argumentos, adicionando o primeiro argumento como 0 (conforme especificação)
		args.add(0, new Tree.CONST(0));
		Tree.ExpList argList = null;
        for (int i = args.size() - 1; i >= 0; i--) {
            argList = new Tree.ExpList(args.get(i), argList);
        }
		// Cria uma chamada de função com o nome do label e a lista de argumentos
		return new Tree.CALL(new Tree.NAME(l), argList);
    } 
	
	// Retorna string formatada para literais
	@Override
    public String string(Label lab, String string) {
		int length = string.length();
		StringBuilder lit = new StringBuilder();
		for (int i = 0; i < length; i++) {
	    	char c = string.charAt(i);
	    	switch (c) {
	    		case '\b': lit.append("\\b"); break;
	    		case '\t': lit.append("\\t"); break;
	    		case '\n': lit.append("\\n"); break;
	    		case '\f': lit.append("\\f"); break;
	    		case '\r': lit.append("\\r"); break;
	    		case '\"': lit.append("\\\""); break;
	    		case '\\': lit.append("\\\\"); break;
	    		default:
					if (c < ' ' || c > '~') {
					    int v = (int)c;
					    lit.append("\\").append(((v>>6)&7)).append(((v>>3)&7)).append((v&7));
					} else {
					    lit.append(c);
					}
					break;
	    	}
		}
		return "\t.data\n\t.word " + length + "\n" + lab.toString() + ":\t.asciiz\t\"" + lit.toString() + "\"";
    }

    // Retorna todos os registradores usados
	@Override
    public Temp[] registers() {
        List<Temp> regs = new LinkedList<>();
        regs.addAll(Arrays.asList(callerSaves));
        regs.addAll(Arrays.asList(calleeSaves));
        regs.addAll(Arrays.asList(argRegs));
        regs.addAll(Arrays.asList(specialRegs));
        return regs.toArray(new Temp[0]);
    }

	// Registradores vivos no retorno
    private Temp[] returnSink() {
        List<Temp> l = new LinkedList<>();
        l.add(V0);
        l.addAll(Arrays.asList(specialRegs));
        l.addAll(Arrays.asList(calleeSaves));
        return l.toArray(new Temp[0]);
    }

    // Registradores definidos por chamada
    public Temp[] calldefs() {
        List<Temp> l = new LinkedList<>();
        l.add(RA);
        l.addAll(Arrays.asList(argRegs));
        l.addAll(Arrays.asList(callerSaves));
        return l.toArray(new Temp[0]);
	}

	// Método auxiliar para atribuir parâmetros formais
	// para os parâmetros reais, começando do índice 0
    private void assignFormals(Iterator<Access> formals, Iterator<Access> actuals, List<Tree.Stm> body){
		if (!formals.hasNext() || !actuals.hasNext())
		    return;
		Access formal = formals.next();
		Access actual = actuals.next();
		assignFormals(formals, actuals, body);
		body.add(0, MOVE(formal.exp(TEMP(FP)), actual.exp(TEMP(FP))));
    }

	// Método auxiliar para atribuir registradores de callee
	// para os parâmetros formais, começando do índice i
    private void assignCallees(int i, List<Tree.Stm> body){
		if (i >= calleeSaves.length) return;
		if (body == null) return; // Adicione esta linha para evitar NullPointerException
		
		Access a = allocLocal(!spilling);
		assignCallees(i+1, body);
		body.add(0, MOVE(a.exp(TEMP(FP)), TEMP(calleeSaves[i])));
		body.add(MOVE(TEMP(calleeSaves[i]), a.exp(TEMP(FP))));
    } 

	// Método auxiliar para criar instruções OPER
	private static Assem.Instr OPER(String assem, Temp[] dst, Temp[] src) {
		TempList dstList = arrayToTempList(dst);
    	TempList srcList = arrayToTempList(src);
		return new OPER(assem, dstList, srcList, null);
    } 

	// Converte Temp[] para TempList
	private static TempList arrayToTempList(Temp[] arr) {
    	TempList list = null;
    	if (arr != null) {
        	for (int i = arr.length - 1; i >= 0; i--) {
            	list = new TempList(arr[i], list);
        	}
    	}
    	return list;
	}

	//
	@Override
    public void procEntryExit1(List<Tree.Stm> body) {
		assignFormals(formals.iterator(), actuals.iterator(), body);
		assignCallees(0, body);
    }

	//
	@Override
	public void procEntryExit2(List<Assem.Instr> body) {
		// Cria um deslocamento local para salvar callee-saved
		int saveOffset = offset;

		// Salva callee-saved
		for (Temp reg : calleeSaves) {
			body.add(0, OPER(String.format("sw `s0, %d(`s1)", saveOffset),
				null, new Temp[]{reg, FP()}));
			saveOffset -= WORD_SIZE;
		}

		// Restaura callee-saved
		int restoreOffset = offset;
		for (Temp reg : calleeSaves) {
			body.add(OPER(String.format("lw `d0, %d(`s0)", restoreOffset),
				new Temp[]{reg}, new Temp[]{FP()}));
			restoreOffset -= WORD_SIZE;
		}

		// Instrução de retorno
		body.add(OPER("jr `s0", null, new Temp[]{RA}));
	}
   
	//
	@Override
    public void procEntryExit3(List<Assem.Instr> body) {
		int frameSize = maxArgOffset - offset;
		ListIterator<Assem.Instr> cursor = body.listIterator();
		cursor.add(OPER("\t.text", null, null));
		cursor.add(OPER(name + ":", null, null));
		cursor.add(OPER(name + "_framesize=" + frameSize, null, null));
		if (frameSize != 0) {
		    cursor.add(OPER("\tsubu $sp " + name + "_framesize", new Temp[]{SP}, new Temp[]{SP}));
		    body.add(OPER("\taddu $sp " + name + "_framesize", new Temp[]{SP}, new Temp[]{SP}));
		}
    }

	private static Tree.Stm SEQ(Tree.Stm left, Tree.Stm right){
		if (left == null)
		    return right;
		if (right == null)
		    return left;
		return new Tree.SEQ(left, right);
    }

    private static Tree.MOVE MOVE(Tree.Exp d, Tree.Exp s) {
		return new Tree.MOVE(d, s);
    }

    private static Tree.TEMP TEMP(Temp t) {
		return new Tree.TEMP(t);
    }

	// Implementação do método codegen
	/* @Override
	public List<Assem.Instr> codegen(Tree.StmList stms) {
		List<Assem.Instr> insns = new java.util.LinkedList<Assem.Instr>();
		Codegen cg = new Codegen(this, insns.listIterator());
		for (java.util.Iterator<Tree.Stm> s = stms.iterator(); s.hasNext(); )
	    	s.next().accept(cg);
		return insns; 
    } */

	public Assem.InstrList codegen(Tree.Stm stm) {
    	return (new Codegen(this)).codegen(stm);
	}
	 
    // set spilling to true when the spill method is implemented
	// Instruções para salvar/restaurar temporários na pilha.
	// Método a ser atualizado na etapa de alocação de registradores	
	@Override
    public void spill(List<Assem.Instr> insns, Temp[] spills){
		/*
		if (spills != null) {
	    	if (!spilling) {
				for (int s = 0; s < spills.length; s++)
		    		System.err.println("Need to spill " + spills[s]);
				throw new Error("Spilling unimplemented");
	    	}
        else 
			for (int s = 0; s < spills.length; s++) {
				Tree.Exp exp = allocLocal(true).exp(TEMP(FP));
			for (ListIterator<Assem.Instr> i = insns.listIterator(); i.hasNext();){
		    	Assem.Instr insn = i.next();
		    	Temp[] use = insn.use;
		    	if (use != null)
					for (int u = 0; u < use.length; u++) {
			    		if (use[u] == spills[s]) {
							Temp t = new Temp();
							t.spillTemp = true;
							Tree.Stm stm = MOVE(TEMP(t), exp);
							i.previous();
							stm.accept(new Codegen(this, i));
							if (insn != i.next())
				    			throw new Error();
							insn.replaceUse(spills[s], t);
							break;
			    		}
					}
		    	Temp[] def = insn.def;
		    	if (def != null)
					for (int d = 0; d < def.length; d++) {
			    		if (def[d] == spills[s]) {
							Temp t = new Temp();
							t.spillTemp = true;
							insn.replaceDef(spills[s], t);
							Tree.Stm stm = MOVE(exp, TEMP(t));
							stm.accept(new Codegen(this, i));
							break;
			   	 		}
					}
				}
	    	}
        } */
    }
   //Mini Java Library will be appended to end of program
   @Override
    public String programTail(){
		return      
			"         .text            \n" +
			"         .globl _halloc   \n" +
			"_halloc:                  \n" +
			"         li $v0, 9        \n" +
			"         syscall          \n" +
			"         j $ra            \n" +
			"                          \n" +
			"         .text            \n" +
			"         .globl _printint \n" +
			"_printint:                \n" +
			"         li $v0, 1        \n" +
			"         syscall          \n" +
			"         la $a0, newl     \n" +
			"         li $v0, 4        \n" +
			"         syscall          \n" +
			"         j $ra            \n" +
			"                          \n" +
			"         .data            \n" +
			"         .align   0       \n" +
			"newl:    .asciiz \"\\n\"  \n" +
			"         .data            \n" +
			"         .align   0       \n" +
			"str_er:  .asciiz \" ERROR: abnormal termination\\n\" "+
			"                          \n" +
			"         .text            \n" +
			"         .globl _error    \n" +
			"_error:                   \n" +
			"         li $v0, 4        \n" +
			"         la $a0, str_er   \n" +
			"         syscall          \n" +
			"         li $v0, 10       \n" +
			"         syscall          \n" ;
    }
}
