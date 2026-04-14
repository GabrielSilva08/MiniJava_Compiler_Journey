package Temp;

import java.util.HashMap;

public class DefaultMap implements TempMap {
	public String tempMap(Temp t) {
	   String s = table.get(t);
        return s != null ? s : t.toString();
	}

	public DefaultMap() {}
	private final HashMap<Temp, String> table = new HashMap<>();

    public void put(Temp t, String s) {
        table.put(t, s);
    }
}