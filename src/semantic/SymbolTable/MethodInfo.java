package semantic.SymbolTable;

import java.util.*;

public class MethodInfo {
    public Symbol methodName;
    public Symbol returnType;
    public LinkedHashMap<Symbol, Symbol> parameters = new LinkedHashMap<>();
    public Hashtable<Symbol, VarInfo> localVars = new Hashtable<>();

    public MethodInfo(Symbol methodName, Symbol returnType) {
        this.methodName = methodName;
        this.returnType = returnType;
    }

    public void addParameter(Symbol paramName, Symbol paramType) {
        parameters.put(paramName, paramType);
    }

    public void addLocalVar(Symbol varName, VarInfo varInfo) {
        localVars.put(varName, varInfo);
    }

    public VarInfo getLocalVar(Symbol varName) {
        return localVars.get(varName);
    }
}
