package Tree;
import Temp.Temp;
import Temp.Label;
public class EXP1 extends Stm {
  public Exp exp; 
  public EXP1(Exp e) {exp=e;}
  public ExpList kids() {return new ExpList(exp,null);}
  public Stm build(ExpList kids) {
    return new EXP1(kids.head);
  }
}

