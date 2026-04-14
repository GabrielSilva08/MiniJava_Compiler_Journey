package Graph;

public class Node {
    int mykey;
    Graph mygraph;
    NodeList succs;
    NodeList preds;

    public Node(){}
    public Node(Graph g) {
	    mygraph=g; 
	    mykey= g.nodecount++;
	    NodeList p = new NodeList(this, null);
	    if (g.mylast==null)
	        g.mynodes=g.mylast=p;
	    else 
            g.mylast = g.mylast.tail = p;
    }
    public NodeList succ() {return succs;}
    public NodeList pred() {return preds;}
    public NodeList adj() {return cat(succ(), pred());}
    public int inDegree() {return len(pred());}
    public int outDegree() {return len(succ());}
    public int degree() {return inDegree()+outDegree();} 

    public boolean goesTo(Node n) {
	    return Graph.inList(n, succ());
    }

    public boolean comesFrom(Node n) {
	    return Graph.inList(n, pred());
    }

    public boolean adj(Node n) {
	    return goesTo(n) || comesFrom(n);
    }

    public String toString() {return String.valueOf(mykey);}

    NodeList cat(NodeList a, NodeList b) {
        if (a==null) return b;
	    else return new NodeList(a.head, cat(a.tail,b));
    }

    int len(NodeList l) {
	    int i=0;
	    for(NodeList p=l; p!=null; p=p.tail) i++;
	    return i;
    }
}
