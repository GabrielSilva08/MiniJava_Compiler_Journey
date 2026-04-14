package RegAlloc;

import FlowGraph.*;
import Graph.*;
import Temp.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Liveness extends InterferenceGraph {
    private Map<Node, Set<Temp>> in = new HashMap<>();
    private Map<Node, Set<Temp>> out = new HashMap<>();
    private Map<Temp, Node> tempNodeMap = new HashMap<>();
    private Map<Node, Temp> nodeTempMap = new HashMap<>();
    private MoveList moveList = null;

    // Construtor: recebe o grafo de fluxo e constrói o grafo de interferência
    public Liveness(FlowGraph flow) {
        // Inicializa conjuntos in/out
        for (NodeList nl = flow.nodes(); nl != null; nl = nl.tail) {
            in.put(nl.head, new HashSet<>());
            out.put(nl.head, new HashSet<>());
        }

        // Iterativamente computa in/out
        boolean changed;
        do {
            changed = false;
            for (NodeList nl = flow.nodes(); nl != null; nl = nl.tail) {
                Node n = nl.head;
                Set<Temp> inOld = new HashSet<>(in.get(n));
                Set<Temp> outOld = new HashSet<>(out.get(n));

                // in[n] = use[n] UNION (out[n] - def[n])
                Set<Temp> useSet = toSet(flow.use(n));
                Set<Temp> defSet = toSet(flow.def(n));
                Set<Temp> outSet = out.get(n);

                Set<Temp> inSet = new HashSet<>(useSet);
                Set<Temp> outMinusDef = new HashSet<>(outSet);
                outMinusDef.removeAll(defSet);
                inSet.addAll(outMinusDef);
                in.put(n, inSet);

                // out[n] = UNION in[s] for all successors s of n
                Set<Temp> outNew = new HashSet<>();
                for (NodeList succ = n.succ(); succ != null; succ = succ.tail) {
                    outNew.addAll(in.get(succ.head));
                }
                out.put(n, outNew);

                if (!inOld.equals(inSet) || !outOld.equals(outNew)) {
                    changed = true;
                }
            }
        } while (changed);

        // Cria nó para cada temporário
        for (NodeList nl = flow.nodes(); nl != null; nl = nl.tail) {
            Node n = nl.head;
            TempList defList = flow.def(n);
            for (TempList tl = defList; tl != null; tl = tl.tail) {
                if (!tempNodeMap.containsKey(tl.head)) {
                    Node tempNode = newNode();
                    tempNodeMap.put(tl.head, tempNode);
                    nodeTempMap.put(tempNode, tl.head);
                }
            }
            TempList useList = flow.use(n);
            for (TempList tl = useList; tl != null; tl = tl.tail) {
                if (!tempNodeMap.containsKey(tl.head)) {
                    Node tempNode = newNode();
                    tempNodeMap.put(tl.head, tempNode);
                    nodeTempMap.put(tempNode, tl.head);
                }
            }
        }

        // Construção do grafo de interferência
        for (NodeList nl = flow.nodes(); nl != null; nl = nl.tail) {
            Node n = nl.head;
            TempList defList = flow.def(n);
            Set<Temp> outSet = out.get(n);

            if (flow.isMove(n)) {
                // MOVE: a <- c
                Temp a = (defList != null) ? defList.head : null;
                TempList useList = flow.use(n);
                Temp c = (useList != null) ? useList.head : null;
                for (Temp b : outSet) {
                    if (a != null && b != null && !b.equals(c)) {
                        addEdge(tempNodeMap.get(a), tempNodeMap.get(b));
                    }
                }
                // Adiciona à lista de moves
                if (a != null && c != null) {
                    moveList = new MoveList(tempNodeMap.get(c), tempNodeMap.get(a), moveList);
                }
            } else {
                // Não-MOVE: para cada variável definida, conecta com todas as live-out
                for (TempList tl = defList; tl != null; tl = tl.tail) {
                    Temp a = tl.head;
                    for (Temp b : outSet) {
                        if (a != null && b != null && !a.equals(b)) {
                            addEdge(tempNodeMap.get(a), tempNodeMap.get(b));
                        }
                    }
                }
            }
        }
    }

    // Utilitário: converte TempList para Set<Temp>
    private Set<Temp> toSet(TempList tl) {
        Set<Temp> set = new HashSet<>();
        for (; tl != null; tl = tl.tail) {
            set.add(tl.head);
        }
        return set;
    }

    @Override
    public Node tnode(Temp temp) {
        return tempNodeMap.get(temp);
    }

    @Override
    public Temp gtemp(Node node) {
        return nodeTempMap.get(node);
    }

    @Override
    public MoveList moves() {
        return moveList;
    }

    // Imprime a matriz de adjacência do grafo de interferência
    public void printMatrix() {
        java.util.List<Temp> temps = new java.util.ArrayList<>(tempNodeMap.keySet());

        // Cabeçalho
        System.out.print("      ");
        for (Temp col : temps) {
            System.out.print(col + " ");
        }
        System.out.println();

        // Linhas
        for (Temp row : temps) {
            System.out.print(row + " | ");
            Node rowNode = tempNodeMap.get(row);
            for (Temp col : temps) {
                Node colNode = tempNodeMap.get(col);
                boolean connected = rowNode != null && colNode != null && rowNode.adj(colNode);
                System.out.print((connected ? "1" : "0") + " ");
            }
            System.out.println();
        }
    }
}