package FlowGraph;

import Graph.Node;
import Graph.NodeList;
import Temp.TempList;

public abstract class FlowGraph extends Graph.Graph {
	// The set of temporaries defined by this instruction or basic block 
	public abstract TempList def(Node node);

	// The set of temporaries used by this instruction or basic block 
	public abstract TempList use(Node node);

	// True if this node represents a move instruction, i.e. one that can be deleted if def=use.
	public abstract boolean isMove(Node node);

	// Print a human-readable dump for debugging.
    public void show(java.io.PrintStream out) {
		for (NodeList p=nodes(); p!=null; p=p.tail) {
	  		Node n = p.head;
	  		out.print(n.toString());
	  		out.print(": ");
	  		for(TempList q=def(n); q!=null; q=q.tail) {
	     		out.print(q.head.toString());
	     		out.print(" ");
	  		}
	  		out.print(isMove(n) ? "<= " : "<- ");
	  		for(TempList q=use(n); q!=null; q=q.tail) {
	     		out.print(q.head.toString());
	     		out.print(" ");
	  		}
	  		out.print("; goto ");
	  		for(NodeList q=n.succ(); q!=null; q=q.tail) {
	     		out.print(q.head.toString());
	     		out.print(" ");
	  		}
	  		out.println();
		}
    }
}
