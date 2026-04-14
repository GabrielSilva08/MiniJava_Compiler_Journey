package semantic.SymbolTable;

import java.util.Dictionary;
import java.util.Hashtable;

public class Symbol {
    private String name;
    private Symbol(String n) { name = n; }

    private static Dictionary<String, Symbol> dict = new Hashtable<>();

    public String toString() { return name; }

    public static Symbol symbol(String n) {
        String u = n.intern();
        Symbol s = dict.get(u);
        if (s == null) {
            s = new Symbol(u);
            dict.put(u, s);
        }
        return s;
    }
} 