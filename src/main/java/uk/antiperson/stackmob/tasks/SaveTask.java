package uk.antiperson.stackmob.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import uk.antiperson.stackmob.StackMob;

public class SaveTask extends BukkitRunnable {

    private final StackMob sm;

    public SaveTask(StackMob sm) {
        this.sm = sm;
    }

    public void run() {
        sm.getEntityManager().getDataManager().saveData("./plugins/StackMob/stackmob.data");
    }

}
