package semantic.SymbolTable;

import java.util.*;

public class Table {
    private Stack<Hashtable<Symbol, Object>> scopes;

    public Table() {
        scopes = new Stack<>();
        beginScope(); // global scope
    }

    public void beginScope() {
        scopes.push(new Hashtable<>());
    }

    public void endScope() {
        if (!scopes.isEmpty()) {
            scopes.pop();
        }
    }

    public void put(Symbol key, Object value) {
        if (!scopes.isEmpty()) {
            scopes.peek().put(key, value);
        }
    }

    public Object get(Symbol key) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            Object val = scopes.get(i).get(key);
            if (val != null) return val;
        }
        return null;
    }

    public Enumeration<Symbol> keys() {
        if (!scopes.isEmpty()) {
            return scopes.peek().keys();
        }
        return null;
    }
}
