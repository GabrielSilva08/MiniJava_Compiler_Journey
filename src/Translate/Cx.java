package Translate;

import Tree.*;
import Temp.Label;

public class Cx extends Exp {
    public interface Gen {
        CJUMP make(Label t, Label f);
    }

    Gen gen;

    public Cx(Gen g) {
        gen = g;
    }

    public Tree.Exp unEx() {
        Temp.Temp r = new Temp.Temp();
        Label t = new Label();
        Label f = new Label();
        Label join = new Label();

        return new ESEQ(
            new SEQ(
                new MOVE(new TEMP(r), new CONST(1)),
                new SEQ(
                    gen.make(t, f),
                    new SEQ(
                        new LABEL(f),
                        new SEQ(
                            new MOVE(new TEMP(r), new CONST(0)),
                            new SEQ(
                                new LABEL(t),
                                new LABEL(join)
                            )
                        )
                    )
                )
            ),
            new TEMP(r)
        );
    }

    public Tree.Stm unNx() {
        Label t = new Label();
        Label f = new Label();
        return new SEQ(
            gen.make(t, f),
            new SEQ(new LABEL(t), new LABEL(f))
        );
    }

    public Cx unCx() {
        return this;
    }
}
