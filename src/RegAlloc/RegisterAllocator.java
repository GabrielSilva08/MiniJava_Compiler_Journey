package RegAlloc;

import Temp.*;

import java.util.*;
import Graph.*;

public class RegisterAllocator {
    private InterferenceGraph graph;
    private int K; // número de registradores físicos
    private Stack<Temp> stack = new Stack<>();
    private Map<Temp, Integer> colors = new HashMap<>();
    private Set<Temp> spilled = new HashSet<>();
    private Map<Temp, Set<Temp>> preservedNeighbors = new HashMap<>();

    // Nomes reais dos registradores MIPS
    private static final String[] regNames = {
        "$zero", "$v0", "$v1", "$a0", "$a1", "$a2", "$a3",
        "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7",
        "$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7",
        "$t8", "$t9", "$k0", "$k1", "$gp", "$sp", "$fp", "$ra"
    };

    public RegisterAllocator(InterferenceGraph graph, int numRegisters) {
        this.graph = graph;
        this.K = numRegisters;
    }

    public void allocate() {
        preserveGraph();
        simplify();
        select();
    }

    private void preserveGraph() {
        for (Node node : graph.toList(graph.nodes())) {
            Temp temp = graph.gtemp(node);
            Set<Temp> neighbors = new HashSet<>();
        for (Node n : graph.toList(node.succ())) {
            neighbors.add(graph.gtemp(n));
        }
        preservedNeighbors.put(temp, neighbors);
        }
    }

    private void simplify() {
    while (true) {
        Node nodeToRemove = null;
        for (Node node : graph.toList(graph.nodes())) {
            if (graph.degree(node) < K) {
                nodeToRemove = node;
                break;
            }
        }

        if (nodeToRemove != null) {
            Temp temp = graph.gtemp(nodeToRemove);
            graph.rmNode(nodeToRemove);
            stack.push(temp);
        } else {
            break; // Ou escolhe spill
        }
    }
}


    private void select() {
        while (!stack.isEmpty()) {
            Temp node = stack.pop();
            Set<Integer> usedColors = new HashSet<>();
            Set<Temp> neighbors = preservedNeighbors.getOrDefault(node, Collections.emptySet());

            for (Temp neighbor : neighbors) {
                if (colors.containsKey(neighbor)) {
                    usedColors.add(colors.get(neighbor));
                }
            }

            int color = -1;
            for (int c = 0; c < K; c++) {
                if (!usedColors.contains(c)) {
                    color = c;
                    break;
                }
            }

            if (color == -1) {
                spilled.add(node);
            } else {
                colors.put(node, color);
            }
        }
    }

    public Map<Temp, Integer> getColoring() {
        return colors;
    }

    public Set<Temp> getSpilledTemps() {
        return spilled;
    }

    public void applyToFrameTempMap(DefaultMap tempMap) {
    if (!(tempMap instanceof DefaultMap dm)) {
        throw new IllegalArgumentException("TempMap precisa ser DefaultMap");
    }

    for (Map.Entry<Temp, Integer> entry : colors.entrySet()) {
        Temp temp = entry.getKey();
        int color = entry.getValue();
        if (color >= 0 && color < regNames.length) {
            dm.put(temp, regNames[color]);
        } else {
            dm.put(temp, temp.toString()); // fallback
        }
    }
}


    public void precolorFixedRegs(Map<Temp, Integer> colorMap) {
        colorMap.put(RegAllocConventions.ZERO, 0);
        colorMap.put(RegAllocConventions.RV, 1);

        for (int i = 0; i < RegAllocConventions.ARGREGS.length; i++)
            colorMap.put(RegAllocConventions.ARGREGS[i], 3 + i); // $a0 - $a3

        for (int i = 0; i < RegAllocConventions.CALLER_SAVES.length; i++)
            colorMap.put(RegAllocConventions.CALLER_SAVES[i], 7 + i); // $t0 - $t9

        for (int i = 0; i < RegAllocConventions.CALLEE_SAVES.length; i++)
            colorMap.put(RegAllocConventions.CALLEE_SAVES[i], 15 + i); // $s0 - $s7

        for (int i = 0; i < RegAllocConventions.TEMPREGS.length; i++)
            colorMap.put(RegAllocConventions.TEMPREGS[i], 23 + i);

        colorMap.put(RegAllocConventions.FP, 28);
        colorMap.put(RegAllocConventions.SP, 29);
        colorMap.put(RegAllocConventions.RA, 30);
        colorMap.put(RegAllocConventions.SYS, 31);
    }
}