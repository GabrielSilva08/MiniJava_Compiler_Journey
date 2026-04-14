package semantic;

import semantic.SymbolTable.*;
import syntaxtree.*;
import visitor.Visitor;
import util.ErrorLogger;

public class TypeCheckVisitor implements Visitor {
    private Table symbolTable;
    private ClassInfo currentClass;
    private MethodInfo currentMethod;

    public TypeCheckVisitor(Table table) {
        this.symbolTable = table;
    }

    public void visit(Program n) {
        n.m.accept(this);
        for (int i = 0; i < n.cl.size(); i++) {
            n.cl.elementAt(i).accept(this);
        }
    }

    public void visit(MainClass n) {
        currentClass = (ClassInfo) symbolTable.get(Symbol.symbol(n.i1.s));
        currentMethod = currentClass.getMethod(Symbol.symbol("main"));
        n.s.accept(this);
        currentMethod = null;
        currentClass = null;
    }

    public void visit(ClassDeclSimple n) {
        currentClass = (ClassInfo) symbolTable.get(Symbol.symbol(n.i.s));
        for (int i = 0; i < n.ml.size(); i++) {
            n.ml.elementAt(i).accept(this);
        }
        currentClass = null;
    }

    public void visit(ClassDeclExtends n) {
        currentClass = (ClassInfo) symbolTable.get(Symbol.symbol(n.i.s));
        for (int i = 0; i < n.ml.size(); i++) {
            n.ml.elementAt(i).accept(this);
        }
        currentClass = null;
    }

    public void visit(MethodDecl n) {
        currentMethod = currentClass.getMethod(Symbol.symbol(n.i.s));
        for (int i = 0; i < n.sl.size(); i++) {
            n.sl.elementAt(i).accept(this);
        }
        Type exprType = evaluateExpr(n.e);
        if (!typeEquals(exprType, convertToType(currentMethod.returnType))) {
            ErrorLogger.logError("TIPO", "tipo de retorno incompatível em " + n.i.s);
        }
        currentMethod = null;
    }

    public void visit(Assign n) {
        Type varType = lookupVar(n.i);
        Type exprType = evaluateExpr(n.e);
        if (!typeEquals(varType, exprType)) {
            ErrorLogger.logError("TIPO", "tipo incompatível na atribuição a " + n.i.s);
        }
    }

    public void visit(Print n) {
        Type exprType = evaluateExpr(n.e);
        if (!(exprType instanceof IntegerType)) {
            ErrorLogger.logError("TIPO", "print espera expressão do tipo int");
        }
    }

    public void visit(If n) {
        Type condType = evaluateExpr(n.e);
        if (!(condType instanceof BooleanType)) {
            ErrorLogger.logError("TIPO", "if espera condição do tipo boolean");
        }
        n.s1.accept(this);
        n.s2.accept(this);
    }

    public void visit(While n) {
        Type condType = evaluateExpr(n.e);
        if (!(condType instanceof BooleanType)) {
            ErrorLogger.logError("TIPO", "while espera condição do tipo boolean");
        }
        n.s.accept(this);
    }

    public void visit(Block n) {
        for (int i = 0; i < n.sl.size(); i++) {
            n.sl.elementAt(i).accept(this);
        }
    }

    // ----------- Expressões -------------

    private Type evaluateExpr(Exp e) {
        if (e instanceof IntegerLiteral) return new IntegerType();

        if (e instanceof True || e instanceof False) return new BooleanType();

        if (e instanceof IdentifierExp) return lookupVar(new Identifier(((IdentifierExp)e).s));

        if (e instanceof This) return new IdentifierType(currentClass.className.toString());

        if (e instanceof Plus){
            Plus p = (Plus) e; 
            Type t1 = evaluateExpr(p.e1);
            Type t2 = evaluateExpr(p.e2);
            if (!(t1 instanceof IntegerType && t2 instanceof IntegerType)) {
                ErrorLogger.logError("TIPO", "operação aritmética exige operandos inteiros");
            }
            return new IntegerType();
        }

        if (e instanceof Minus){
            Minus p = (Minus) e; 
            Type t1 = evaluateExpr(p.e1);
            Type t2 = evaluateExpr(p.e2);
            if (!(t1 instanceof IntegerType && t2 instanceof IntegerType)) {
                ErrorLogger.logError("TIPO", "operação aritmética exige operandos inteiros");
            }
            return new IntegerType();
        }

        if (e instanceof Times){
            Times p = (Times) e; 
            Type t1 = evaluateExpr(p.e1);
            Type t2 = evaluateExpr(p.e2);
            if (!(t1 instanceof IntegerType && t2 instanceof IntegerType)) {
                ErrorLogger.logError("TIPO", "operação aritmética exige operandos inteiros");
            }
            return new IntegerType();
        }

        if (e instanceof ArrayLookup){
            ArrayLookup al = (ArrayLookup) e;
            Type t1 = evaluateExpr(al.e1);
            Type t2 = evaluateExpr(al.e2);
            if (!(t1 instanceof IntArrayType && t2 instanceof IntegerType)) {
                ErrorLogger.logError("TIPO", "acesso ao array exige int[] e índice inteiro");
            }
            return new IntegerType();
        }

        if (e instanceof LessThan){
            LessThan lt = (LessThan) e;
            Type t1 = evaluateExpr(lt.e1);
            Type t2 = evaluateExpr(lt.e2);
            if (!(t1 instanceof IntegerType && t2 instanceof IntegerType)) {
                ErrorLogger.logError("TIPO", "operador '<' exige operandos inteiros");
            }
            return new BooleanType();
        }   

        if (e instanceof And){
            And and = (And) e;
            Type t1 = evaluateExpr(and.e1);
            Type t2 = evaluateExpr(and.e2);
            if (!(t1 instanceof BooleanType && t2 instanceof BooleanType)) {
                ErrorLogger.logError("TIPO", "operador '&&' exige operandos booleanos");
            }
            return new BooleanType();
        }

        if (e instanceof Call) {
            Call c = (Call) e;
            Type objType = evaluateExpr(c.e);
            if (!(objType instanceof IdentifierType)) {
                ErrorLogger.logError("TIPO", "chamada de método em tipo não-objeto");
                return new IntegerType(); // fallback
            }

            ClassInfo classInfo = (ClassInfo) symbolTable.get(Symbol.symbol(((IdentifierType) objType).s));
            if (classInfo == null) {
                ErrorLogger.logError("TIPO", "classe " + ((IdentifierType) objType).s + " não declarada");
                return new IntegerType(); // fallback
            }

            MethodInfo method = classInfo.getMethod(Symbol.symbol(c.i.s));
            if (method == null) {
                ErrorLogger.logError("TIPO", "método " + c.i.s + " não encontrado em " + objType);
                return new IntegerType(); // fallback
            }

            if (method.parameters.size() != c.el.size()) {
                ErrorLogger.logError("TIPO", "número de argumentos incompatível na chamada de " + c.i.s);
            }

            // Verifica tipos dos argumentos
            int i = 0;
            for (Symbol paramName : method.parameters.keySet()) {
                Type expected = convertToType(method.parameters.get(paramName));
                Type actual = evaluateExpr(c.el.elementAt(i));
                if (!typeEquals(expected, actual)) {
                    ErrorLogger.logError("TIPO", "tipo do argumento " + (i+1) + " incompatível em chamada de " + c.i.s);
                }
                i++;
            }
            return convertToType(method.returnType);
        }

        if (e instanceof Not){
            Type operandType = evaluateExpr(((Not) e).e);
            if (!(operandType instanceof BooleanType))
                ErrorLogger.logError("TIPO", "operador '!' espera boolean, mas recebeu " + operandType.getClass().getSimpleName());
            return new BooleanType();
        }

        if (e instanceof NewArray) {
            NewArray na = (NewArray) e;
            Type size = evaluateExpr(na.e);
            if (!(size instanceof IntegerType)) {
                ErrorLogger.logError("TIPO", "tamanho do array precisa ser int");
            }
            return new IntArrayType();
        }

        if (e instanceof NewObject) {
            NewObject no = (NewObject) e;
            return new IdentifierType(no.i.s);
        }

        ErrorLogger.logWarning("expressão " + e.getClass().getSimpleName() + " não reconhecida. Assumindo tipo int");
        return new IntegerType(); // fallback, ajustável
    }

    private Type lookupVar(Identifier id){
        Symbol name = Symbol.symbol(id.s);

        if (currentMethod != null && currentMethod.localVars.containsKey(name)) {
            return convertToType(currentMethod.localVars.get(name).type);
        }
        if (currentMethod != null && currentMethod.parameters.containsKey(name)) {
            return convertToType(currentMethod.parameters.get(name));
        }
        if (currentClass.fields.containsKey(name)) {
            return convertToType(currentClass.fields.get(name).type);
        }
        ErrorLogger.logError("TIPO", "variável " + id.s + " não declarada");
        return new IntegerType();
    }

    private Type convertToType(Symbol type){
        switch (type.toString()) {
            case "int": return new IntegerType();
            case "boolean": return new BooleanType();
            case "int[]": return new IntArrayType();
            default: return new IdentifierType(type.toString());
        }
    }

    private boolean typeEquals(Type t1, Type t2) {
        if (t1.getClass() == t2.getClass()) return true;
        if (t1 instanceof IdentifierType && t2 instanceof IdentifierType) {
            return ((IdentifierType)t1).s.equals(((IdentifierType)t2).s);
        }
        return false;
    }

    // ------ Métodos visit restantes (placeholders) ------
    public void visit(VarDecl n) {}
    public void visit(Formal n) {}
    public void visit(Identifier n) {}
    public void visit(BooleanType n) {}
    public void visit(IntegerType n) {}
    public void visit(IntArrayType n) {}
    public void visit(IdentifierType n) {}
    public void visit(And n) {}
    public void visit(LessThan n) {}
    public void visit(Plus n) {}
    public void visit(Minus n) {}
    public void visit(Times n) {}
    public void visit(ArrayLookup n) {}
    public void visit(ArrayLength n) {}
    public void visit(Call n) {}
    public void visit(IntegerLiteral n) {}
    public void visit(True n) {}
    public void visit(False n) {}
    public void visit(IdentifierExp n) {}
    public void visit(This n) {}
    public void visit(NewArray n) {}
    public void visit(NewObject n) {}
    public void visit(Not n) {}
    public void visit(ArrayAssign n) {}
}
