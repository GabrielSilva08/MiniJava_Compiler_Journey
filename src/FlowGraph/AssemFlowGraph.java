package FlowGraph;

import Assem.*;
import Graph.*;
import Temp.*;
import java.util.HashMap;
import java.util.Map;

public class AssemFlowGraph extends FlowGraph {
    private Map<Node, Instr> nodeInstrMap = new HashMap<>();
    private Map<Instr, Node> instrNodeMap = new HashMap<>();

    // Construtor: transforma InstrList em grafo de fluxo
    public AssemFlowGraph(Assem.InstrList instrs) {
        Node prev = null;
        for (Assem.InstrList l = instrs; l != null; l = l.tail) {
            Node n = newNode();
            nodeInstrMap.put(n, l.head);
            instrNodeMap.put(l.head, n);

            // Fluxo sequencial: conecta anterior ao atual
            if (prev != null) {
                addEdge(prev, n);
            }
            prev = n;
        }

        // Adiciona arestas de salto (para instruções com jumps)
        for (Map.Entry<Node, Instr> entry : nodeInstrMap.entrySet()) {
            Instr instr = entry.getValue();
            Node node = entry.getKey();
            Targets jumps = instr.jumps();
            if (jumps != null && jumps.labels != null) {
                for (LabelList ll = jumps.labels; ll != null; ll = ll.tail) {
                    Node targetNode = findNodeByLabel(ll.head);
                    if (targetNode != null) {
                        addEdge(node, targetNode);
                    }
                }
            }
        }
    }

    // Retorna o Instr associado ao nó
    public Instr instr(Node n) {
        return nodeInstrMap.get(n);
    }

    // Busca nó pelo label (para saltos)
    private Node findNodeByLabel(Label label) {
        for (Map.Entry<Node, Instr> entry : nodeInstrMap.entrySet()) {
            Instr instr = entry.getValue();
            if (instr instanceof LABEL && ((LABEL)instr).label.equals(label)) {
                return entry.getKey();
            }
        }
        return null;
    }

    // Temporários definidos pela instrução associada ao nó
    @Override
    public TempList def(Node node) {
        Instr instr = instr(node);
        return instr != null ? instr.def() : null;
    }

    // Temporários usados pela instrução associada ao nó
    @Override
    public TempList use(Node node) {
        Instr instr = instr(node);
        return instr != null ? instr.use() : null;
    }

    // True se a instrução associada ao nó é um MOVE
    @Override
    public boolean isMove(Node node) {
        Instr instr = instr(node);
        return instr instanceof Assem.MOVE;
    }
}