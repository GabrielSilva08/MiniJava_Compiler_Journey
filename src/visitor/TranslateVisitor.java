package visitor;

import syntaxtree.*;
//import visitor.Visitor;
import Tree.*;
import Temp.*;
import Translate.Ex;
import Translate.Nx;
import frame.*;
//import mips.*;
import mips.InFrame;

import java.util.*;

import semantic.SymbolTable.Symbol;

public class TranslateVisitor implements Visitor {

    public Tree.TEMP thisPtr;
    public Translate.Exp exp;
    public Tree.Stm methodBody;
    private Frame frame;
    private Map<String, Access> varEnv = new HashMap<>();
    public Map<String, Access> classFields = new HashMap<>();;
    private HashMap<String, Access> fieldEnv = new HashMap<>();

    //private Map<String, Tree.Stm> methodsIR = new HashMap<>();


    // MÉTODOS JÁ IMPLEMENTADOS no TranslateVisitor

    public void visit(MainClass n) {
        List<Boolean> escapes = new LinkedList<>();
        escapes.add(true); // Exemplo: x escapa (está no Print)
        this.frame = new mips.MipsFrame().newFrame(Symbol.symbol("main"), escapes);

        // Aloca variável "x" (manual por enquanto)
        Access xAccess = frame.allocLocal(true);
        varEnv.put("x", xAccess);

        // Visita o corpo do método
        n.s.accept(this);

        // Guarda como método traduzido
        methodBody = this.exp.unNx();
    }

    public void visit(MethodDecl n) {
        // 1. Criar um novo Frame para o método atual
        List<Boolean> escapes = new LinkedList<>();
        for (int i = 0; i < n.fl.size(); i++) {
            escapes.add(true);  // ou false, dependendo se o parâmetro escapa
        }
        this.frame = new mips.MipsFrame().newFrame(Symbol.symbol(n.i.s), escapes);

        // this está sempre no primeiro argumento
        TEMP thisTemp = new TEMP(frame.FP());
        this.thisPtr = thisTemp;

        // 2. Inicializar um novo ambiente para variáveis locais e parâmetros
        this.varEnv = new HashMap<>();

        // 3. Adicionar os parâmetros no ambiente com allocLocal
        for (int i = 0; i < n.fl.size(); i++) {
            Formal f = n.fl.elementAt(i);
            Access acc = frame.allocLocal(false); // normal para parâmetros é false (não escapa)
            varEnv.put(f.i.s, acc);
        }

        // 4. Adicionar as variáveis locais no ambiente
        for (int i = 0; i < n.vl.size(); i++) {
            VarDecl v = n.vl.elementAt(i);
            Access acc = frame.allocLocal(true); // normalmente variáveis locais escapam
            varEnv.put(v.i.s, acc);
        }

        // 5. Traduzir o corpo do método (lista de statements)
        Tree.Stm stmResult = null;
        for (int i = 0; i < n.sl.size(); i++) {
            Statement s = n.sl.elementAt(i);
            s.accept(this);
            Tree.Stm stm = this.exp.unNx();
            stmResult = (stmResult == null) ? stm : new SEQ(stmResult, stm);
        }

        // 6. Guardar o resultado no campo methodBody e exp
        this.methodBody = stmResult;
        this.exp = new Nx(stmResult);
    }


    public void visit(Block n) {
        Tree.Stm result = null;

        for (int i = 0; i < n.sl.size(); i++) {
            n.sl.elementAt(i).accept(this);
            Tree.Stm s = this.exp.unNx();
            result = (result == null) ? s : new SEQ(result, s);
        }

        this.exp = new Nx(result);
    }

    public void visit(syntaxtree.Print n) {
        n.e.accept(this);
        Tree.Exp arg = this.exp.unEx();

        Tree.Exp call = new CALL(new NAME(new Label("_printint")), new Tree.ExpList(arg, null));
        this.exp = new Nx(new EXP1(call));
    }

    public void visit(Assign n) {
        String varName = n.i.s;
        Access acc = varEnv.get(varName);

        if (acc == null) {
            acc = classFields.get(varName);  // mudar para classFields

            if (acc == null) {
                throw new RuntimeException("Variável não declarada: " + varName);
            }

            Tree.Exp lhs = acc.exp(thisPtr);
            n.e.accept(this);
            Tree.Exp rhs = this.exp.unEx();
            this.exp = new Nx(new MOVE(lhs, rhs));
            return;
        }

        Tree.Exp lhs = acc.exp(new TEMP(frame.FP()));
        n.e.accept(this);
        Tree.Exp rhs = this.exp.unEx();
        this.exp = new Nx(new MOVE(lhs, rhs));
    }

    public void visit(Plus n) {
        n.e1.accept(this);
        Tree.Exp left = this.exp.unEx();

        n.e2.accept(this);
        Tree.Exp right = this.exp.unEx();

        this.exp = new Ex(new BINOP(BINOP.PLUS, left, right));
    }

    public void visit(IntegerLiteral n) {
        this.exp = new Ex(new CONST(n.i));
    }

    public void visit(IdentifierExp n) {
        Access acc = varEnv.get(n.s);

        if (acc != null) {
            // Variável local ou parâmetro
            Tree.Exp access = acc.exp(new Tree.TEMP(frame.FP()));
            this.exp = new Ex(access);
            return;
        }

        // Se não for local, tentar achar no campo da classe (classFields, por exemplo)
        Access fieldAcc = classFields.get(n.s);

        if (fieldAcc != null) {
            // Campo da classe: acessar via thisPtr
            Tree.Exp thisPtrExp = thisPtr;  // thisPtr deve ser um Temp armazenando o ponteiro do 'this'
            Tree.Exp access = fieldAcc.exp(thisPtrExp);
            this.exp = new Ex(access);
            return;
        }

        // Se não achou, lança erro
        throw new RuntimeException("Variável não declarada usada: " + n.s);
    }




    // --------------------------
    // MÉTODOS SIMPLES (A IMPLEMENTAR)
    // --------------------------

    public void visit(True n) {
        this.exp = new Ex(new CONST(1));
    }

    public void visit(False n) {
        this.exp = new Ex(new CONST(0));
    }

    public void visit(Minus n) {
        n.e1.accept(this);
        Tree.Exp left = this.exp.unEx();

        n.e2.accept(this);
        Tree.Exp right = this.exp.unEx();

        this.exp = new Ex(new BINOP(BINOP.MINUS, left, right));
    }

    public void visit(Times n) {
        n.e1.accept(this);
        Tree.Exp left = this.exp.unEx();

        n.e2.accept(this);
        Tree.Exp right = this.exp.unEx();

        this.exp = new Ex(new BINOP(BINOP.MUL, left, right));
    }

    public void visit(Not n) {
        n.e.accept(this);
        Tree.Exp operand = this.exp.unEx();

        this.exp = new Ex(new BINOP(BINOP.XOR, operand, new CONST(1)));
    }

    public void visit(This n) {
        // For now, return a placeholder - this would typically access the current object
        this.exp = new Ex(new CONST(0)); // TODO: implement proper this handling
    }

    public void visit(VarDecl n) {
        // Allocate local variable and add to environment
        Access varAccess = frame.allocLocal(true);
        varEnv.put(n.i.s, varAccess);
    }

    public void visit(Formal n) {
        // Map formal parameters - they are typically passed in registers or on stack
        // For now, just allocate a local access
        Access paramAccess = frame.allocLocal(false); // false for non-escaping
        varEnv.put(n.i.s, paramAccess);
    }

    // --------------------------
    // NÃO IMPLEMENTADOS (ainda)
    // --------------------------

    public void visit(Program n) {
        // Visit main class
        n.m.accept(this);
        
        // Visit all class declarations
        for (int i = 0; i < n.cl.size(); i++) {
            n.cl.elementAt(i).accept(this);
        }
    }

    public void visit(ClassDeclSimple n) {
        classFields = new HashMap<>(); // zera para nova classe

        for (int i = 0; i < n.vl.size(); i++) {
            VarDecl var = n.vl.elementAt(i);
            Access acc = frame.allocLocal(true); // ou false, dependendo do escape
            classFields.put(var.i.s, acc);          // salva como campo
        }

        for (int i = 0; i < n.ml.size(); i++) {
            n.ml.elementAt(i).accept(this);
        }
    }



    public void visit(ClassDeclExtends n) {
        classFields = new HashMap<>(); // zera para nova classe

        for (int i = 0; i < n.vl.size(); i++) {
            VarDecl var = n.vl.elementAt(i);
            Access acc = frame.allocLocal(true); // ou false, dependendo do escape
            classFields.put(var.i.s, acc);          // salva como campo
        }

        for (int i = 0; i < n.ml.size(); i++) {
            n.ml.elementAt(i).accept(this);
        }
    }

    public void visit(IntArrayType n) {
        // Array type - no translation needed
    }

    public void visit(BooleanType n) {
        // Boolean type - no translation needed
    }

    public void visit(IntegerType n) {
        // Integer type - no translation needed
    }

    public void visit(IdentifierType n) {
        // Identifier type - no translation needed
    }

    public void visit(If n) {
        n.e.accept(this);
        Tree.Exp condition = this.exp.unEx();
        
        n.s1.accept(this);
        Tree.Stm thenStm = this.exp.unNx();
        
        n.s2.accept(this);
        Tree.Stm elseStm = this.exp.unNx();
        
        Label t = new Label();
        Label f = new Label();
        Label join = new Label();
        
        Tree.Stm ifStm = new SEQ(
            new CJUMP(CJUMP.EQ, condition, new CONST(0), f, t),
            new SEQ(
                new LABEL(t),
                new SEQ(
                    thenStm,
                    new SEQ(
                        new JUMP(join),
                        new SEQ(
                            new LABEL(f),
                            new SEQ(
                                elseStm,
                                new LABEL(join)
                            )
                        )
                    )
                )
            )
        );
        
        this.exp = new Nx(ifStm);
    }
    public void visit(While n) {
        Label test = new Label();
        Label body = new Label();
        Label done = new Label();
        
        n.e.accept(this);
        Tree.Exp condition = this.exp.unEx();
        
        n.s.accept(this);
        Tree.Stm bodyStm = this.exp.unNx();
        
        Tree.Stm whileStm = new SEQ(
            new LABEL(test),
            new SEQ(
                new CJUMP(CJUMP.EQ, condition, new CONST(0), done, body),
                new SEQ(
                    new LABEL(body),
                    new SEQ(
                        bodyStm,
                        new SEQ(
                            new JUMP(test),
                            new LABEL(done)
                        )
                    )
                )
            )
        );
        
        this.exp = new Nx(whileStm);
    }

    public void visit(ArrayAssign n) {
        Access arrayAcc = varEnv.get(n.i.s);
        Tree.Exp array;

        if (arrayAcc != null) {
            array = arrayAcc.exp(new Tree.TEMP(frame.FP()));
        } else {
            arrayAcc = classFields.get(n.i.s);  // mudar para classFields
            if (arrayAcc != null) {
                array = arrayAcc.exp(thisPtr);
            } else {
                throw new RuntimeException("Variável não encontrada: " + n.i.s);
            }
        }

        // Index
        n.e1.accept(this);
        Tree.Exp index = this.exp.unEx();

        // Valor a ser atribuído
        n.e2.accept(this);
        Tree.Exp value = this.exp.unEx();

        // Endereço do elemento do array
        Tree.Exp offset = new BINOP(BINOP.MUL, index, new CONST(frame.wordSize()));
        Tree.Exp address = new BINOP(BINOP.PLUS, array, offset);

        // Atribuição
        this.exp = new Nx(new MOVE(new MEM(address), value));
    }


    public void visit(And n) {
        n.e1.accept(this);
        Tree.Exp left = this.exp.unEx();
        
        n.e2.accept(this);
        Tree.Exp right = this.exp.unEx();
        
        // Logical AND: both operands must be non-zero
        this.exp = new Ex(new BINOP(BINOP.AND, left, right));
    }
    public void visit(LessThan n) {
        n.e1.accept(this);
        Tree.Exp left = this.exp.unEx();
        
        n.e2.accept(this);
        Tree.Exp right = this.exp.unEx();
        
        // Less than comparison - use CJUMP for comparison
        Label t = new Label();
        Label f = new Label();
        Label join = new Label();
        
        Tree.Exp result = new ESEQ(
            new SEQ(
                new CJUMP(CJUMP.LT, left, right, t, f),
                new SEQ(
                    new LABEL(t),
                    new SEQ(
                        new MOVE(new TEMP(new Temp()), new CONST(1)),
                        new SEQ(
                            new JUMP(join),
                            new SEQ(
                                new LABEL(f),
                                new SEQ(
                                    new MOVE(new TEMP(new Temp()), new CONST(0)),
                                    new LABEL(join)
                                )
                            )
                        )
                    )
                )
            ),
            new TEMP(new Temp())
        );
        
        this.exp = new Ex(result);
    }
    public void visit(ArrayLookup n) {
        n.e1.accept(this);
        Tree.Exp array = this.exp.unEx();
        
        n.e2.accept(this);
        Tree.Exp index = this.exp.unEx();
        
        // Calculate array element address: array + (index * wordSize)
        Tree.Exp offset = new BINOP(BINOP.MUL, index, new CONST(frame.wordSize()));
        Tree.Exp address = new BINOP(BINOP.PLUS, array, offset);
        
        this.exp = new Ex(new MEM(address));
    }
    public void visit(ArrayLength n) {
        n.e.accept(this);
        Tree.Exp array = this.exp.unEx();
        
        // Array length is typically stored at offset -4 from array pointer
        Tree.Exp lengthAddr = new BINOP(BINOP.MINUS, array, new CONST(4));
        this.exp = new Ex(new MEM(lengthAddr));
    }
    public void visit(Call n) {
        // Get object expression (receiver)
        n.e.accept(this);
        Tree.Exp receiver = this.exp.unEx();
        
        // Build argument list
        Tree.ExpList args = null;
        for (int i = n.el.size() - 1; i >= 0; i--) {
            n.el.elementAt(i).accept(this);
            Tree.Exp arg = this.exp.unEx();
            args = new Tree.ExpList(arg, args);
        }
        
        // Add receiver as first argument
        args = new Tree.ExpList(receiver, args);
        
        // Create method call using the method name
        Tree.Exp call = new CALL(new NAME(new Label(n.i.s)), args);
        this.exp = new Ex(call);
    }
    public void visit(NewArray n) {
        n.e.accept(this);
        Tree.Exp size = this.exp.unEx();
        
        // Calculate total size needed: (size + 1) * wordSize (extra word for length)
        Tree.Exp totalSize = new BINOP(BINOP.MUL, 
            new BINOP(BINOP.PLUS, size, new CONST(1)), 
            new CONST(frame.wordSize()));
        
        // Call _halloc with the size
        Tree.Exp call = new CALL(new NAME(new Label("_halloc")), 
            new Tree.ExpList(totalSize, null));
        
        // Store the length at the beginning of the array
        Tree.Exp arrayPtr = new TEMP(new Temp());
        Tree.Stm allocStm = new MOVE(arrayPtr, call);
        Tree.Stm storeLength = new MOVE(new MEM(arrayPtr), size);
        
        this.exp = new Ex(new ESEQ(new SEQ(allocStm, storeLength), arrayPtr));
    }
    public void visit(NewObject n) {
        // For now, create a simple object allocation
        // In a full implementation, this would call the constructor
        Tree.Exp call = new CALL(new NAME(new Label("_halloc")), 
            new Tree.ExpList(new CONST(frame.wordSize()), null));
        
        this.exp = new Ex(call);
    }
    public void visit(Identifier n) {
        // For identifier nodes, we typically don't need to do anything
        // as they are handled in IdentifierExp
        // This method is mainly for completeness
    }

}
