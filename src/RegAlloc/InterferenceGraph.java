package RegAlloc;
import Graph.Node;
import java.util.ArrayList;
import java.util.List;
import Graph.NodeList;
import Graph.Graph;

abstract public class InterferenceGraph extends Graph {
   abstract public Node tnode(Temp.Temp temp);
   abstract public Temp.Temp gtemp(Node node);
   abstract public MoveList moves();
   public int spillCost(Node node) {return 1;}
   public int degree(Node n) {
      int d = 0;
      for (NodeList p = n.succ(); p != null; p = p.tail)
         d++;
      return d;
   }
   public List<Node> toList(NodeList nodeList) {
      List<Node> list = new ArrayList<>();
      while (nodeList != null) {
         list.add(nodeList.head);
         nodeList = nodeList.tail;
      }
      return list;
   }

}