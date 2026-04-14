package semantic.SymbolTable;

public class VarInfo {
    public Symbol name;
    public Symbol type;

    public VarInfo(Symbol name, Symbol type) {
        this.name = name;
        this.type = type;
    }
}
