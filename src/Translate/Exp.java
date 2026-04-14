package Translate;

import Tree.*;
import Temp.Label;
import Temp.Temp;
import java.util.List;

public abstract class Exp {
    public abstract Tree.Exp unEx();
    public abstract Tree.Stm unNx();
    public abstract Cx unCx();
}
