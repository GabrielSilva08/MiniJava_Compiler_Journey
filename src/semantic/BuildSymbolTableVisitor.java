package semantic;

import java.beans.Expression;
import semantic.SymbolTable.*;
import syntaxtree.*;
import visitor.Visitor;
// import java.util.Hashtable;

public class BuildSymbolTableVisitor implements Visitor {

    private Table table = new Table();
    private ClassInfo currentClass = null;
    private MethodInfo currentMethod = null;

    public Table getSymbolTable() {
        return table;
    }

    
    public void visit(Program n) {
        n.m.accept(this);
        for (int i = 0; i < n.cl.size(); i++) {
            n.cl.elementAt(i).accept(this);
        }
    }

    public void visit(MainClass n) {
        Symbol className = Symbol.symbol(n.i1.s);
        // System.out.println("Visiting main class: " + className);
        ClassInfo ci = new ClassInfo(className, null);
        table.put(className, ci);
        currentClass = ci;

        // Método main
        Symbol mainMethodName = Symbol.symbol("main");
        MethodInfo mi = new MethodInfo(mainMethodName, Symbol.symbol("void"));
        ci.addMethod(mainMethodName, mi);
        currentMethod = mi;

        // Parâmetro "String[] args"
        n.s.accept(this);

        // System.out.println("Visiting main finished ");

        currentMethod = null;
        currentClass = null;
    }

    public void visit(ClassDeclSimple n) {
        Symbol className = Symbol.symbol(n.i.toString());
        // System.out.println("Visiting class: " + className);
        ClassInfo ci = new ClassInfo(className, null);
        table.put(className, ci);
        currentClass = ci;

        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.elementAt(i).accept(this);
        }
        for (int i = 0; i < n.ml.size(); i++) {
            n.ml.elementAt(i).accept(this);
        }
        // System.out.println("Finished visiting class: " + className);
  
        currentClass = null;
    }

    public void visit(ClassDeclExtends n) {
        Symbol className = Symbol.symbol(n.i.toString());
        // System.out.println("Visiting subclass: " + className);
        Symbol superClassName = Symbol.symbol(n.j.toString());
        ClassInfo ci = new ClassInfo(className, superClassName);
        table.put(className, ci);
        currentClass = ci;

        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.elementAt(i).accept(this);
        }
        for (int i = 0; i < n.ml.size(); i++) {
            n.ml.elementAt(i).accept(this);
        }

        // System.out.println("Finished visiting subclass: " + className);
        currentClass = null;
    }

    public void visit(MethodDecl n) {
        Symbol methodName = Symbol.symbol(n.i.toString());
        // System.out.println("Visiting method: " + methodName);
        Symbol returnType = Symbol.symbol(n.t.toString());
        MethodInfo mi = new MethodInfo(methodName, returnType);
        currentClass.addMethod(methodName, mi);
        currentMethod = mi;

        for (int i = 0; i < n.fl.size(); i++) {
            Formal f = n.fl.elementAt(i);
            Symbol paramName = Symbol.symbol(f.i.s);
            Symbol paramType = Symbol.symbol(f.t.toString());
            mi.addParameter(paramName, paramType);
        }

        for (int i = 0; i < n.vl.size(); i++) {
            n.vl.elementAt(i).accept(this);
        }

        for (int i = 0; i < n.sl.size(); i++) {
            n.sl.elementAt(i).accept(this);
        }

        n.e.accept(this);
        // System.out.println("Finished visiting method: " + methodName);
        currentMethod = null;
    }

    public void visit(VarDecl n) {
        Symbol varName = Symbol.symbol(n.i.s);
        // System.out.println("Visiting variable declaration: " + varName);
        Symbol varType = Symbol.symbol(n.t.toString());
        VarInfo vi = new VarInfo(varName, varType);

        if (currentMethod != null) {
            currentMethod.addLocalVar(varName, vi);
        } else if (currentClass != null) {
            currentClass.addField(varName, vi);
        }

        // System.out.println("Finished visiting variable declaration: " + varName);
    }

    // Para serem implementados com TypeVerifier

    public void visit(Identifier n) {}
    public void visit(IntegerLiteral n) {}
    public void visit(BooleanType n) {}
    public void visit(IntegerType n) {}
    public void visit(Statement n) {}
    public void visit(Type n) {}
    public void visit(Expression n) {}
    public void visit(Formal n) {}
    public void visit(This n) {}
    public void visit(NewArray n) {}
    public void visit(NewObject n) {}
    public void visit(Not n) {}
    public void visit(And n) {}
    public void visit(Plus n) {}
    public void visit(Minus n) {}
    public void visit(Times n) {}
    public void visit(ArrayLookup n) {}
    public void visit(ArrayLength n) {}
    public void visit(Call n) {}
    public void visit(Block n) {}
    public void visit(If n) {}
    public void visit(While n) {}
    public void visit(Print n) {}
    public void visit(Assign n) {}
    public void visit(ArrayAssign n) {}
    public void visit(IdentifierExp n) {}
    public void visit(False n) {}
    public void visit(True n) {}
    public void visit(LessThan n) {}
    public void visit(IdentifierType n) {}
    public void visit(IntArrayType n) {}
}
