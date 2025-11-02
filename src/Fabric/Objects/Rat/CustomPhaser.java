package Fabric.Objects.Rat;

import java.util.concurrent.Phaser;

public class CustomPhaser extends Phaser {

    private final Runnable action;

    public CustomPhaser(int phase, Runnable action) {
        super(phase);
        this.action = action;
    }

    @Override
    protected boolean onAdvance(int phase, int registeredParties) {
        if (registeredParties > 0 && action != null) {
            action.run();
        }

        return registeredParties == 0;
    }
}
