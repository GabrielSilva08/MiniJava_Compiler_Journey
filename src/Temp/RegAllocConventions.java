package Temp;

import java.util.*;

public class RegAllocConventions {
    public static final Temp ZERO = new Temp();  // r0
    public static final Temp RV   = new Temp();  // r1

    public static final Temp[] ARGREGS = {
        new Temp(), new Temp(), new Temp(), new Temp(), new Temp(), new Temp()  // r2–r7
    };

    public static final Temp[] CALLER_SAVES = {
        new Temp(), new Temp(), new Temp(), new Temp(),
        new Temp(), new Temp(), new Temp(), new Temp()  // r8–r15
    };

    public static final Temp[] CALLEE_SAVES = {
        new Temp(), new Temp(), new Temp(), new Temp(),
        new Temp(), new Temp(), new Temp(), new Temp()  // r16–r23
    };

    public static final Temp[] TEMPREGS = {
        new Temp(), new Temp(), new Temp(), new Temp()  // r24–r27
    };

    public static final Temp FP  = new Temp();  // r28
    public static final Temp SP  = new Temp();  // r29
    public static final Temp RA  = new Temp();  // r30
    public static final Temp SYS = new Temp();  // r31 (reservado ou especial)

    public static final List<Temp> all() {
        List<Temp> regs = new ArrayList<>();
        regs.add(ZERO);
        regs.add(RV);
        regs.addAll(Arrays.asList(ARGREGS));
        regs.addAll(Arrays.asList(CALLER_SAVES));
        regs.addAll(Arrays.asList(CALLEE_SAVES));
        regs.addAll(Arrays.asList(TEMPREGS));
        regs.add(FP);
        regs.add(SP);
        regs.add(RA);
        regs.add(SYS);
        return regs;
    }
}
