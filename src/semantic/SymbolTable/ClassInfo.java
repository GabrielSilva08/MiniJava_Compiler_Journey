package semantic.SymbolTable;

import java.util.*;

public class ClassInfo {
    public Symbol className;
    public Symbol superClassName; // pode ser null se não houver herança
    public Hashtable<Symbol, VarInfo> fields = new Hashtable<>();
    public Hashtable<Symbol, MethodInfo> methods = new Hashtable<>();

    public ClassInfo(Symbol className, Symbol superClassName) {
        this.className = className;
        this.superClassName = superClassName;
    }

    public void addField(Symbol varName, VarInfo varInfo) {
        fields.put(varName, varInfo);
    }

    public void addMethod(Symbol methodName, MethodInfo methodInfo) {
        methods.put(methodName, methodInfo);
    }

    public VarInfo getField(Symbol varName) {
        return fields.get(varName);
    }

    public MethodInfo getMethod(Symbol methodName) {
        return methods.get(methodName);
    }
}