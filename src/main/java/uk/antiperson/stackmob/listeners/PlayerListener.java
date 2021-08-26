package uk.antiperson.stackmob.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import uk.antiperson.stackmob.StackMob;
import uk.antiperson.stackmob.utils.StackingTool;

public class PlayerListener implements Listener {

    private final StackMob sm;
    public PlayerListener(StackMob sm) {
        this.sm = sm;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractAtEntityEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }
        if (!(event.getRightClicked() instanceof LivingEntity)) {
            return;
        }
        if (!sm.getItemTools().isStackingTool(event.getPlayer().getInventory().getItemInMainHand())) {
            return;
        }
        StackingTool stackingTool = new StackingTool(sm, event.getPlayer());
        if (event.getPlayer().isSneaking()) {
            stackingTool.shiftMode();
            return;
        } else {
            stackingTool.performAction((LivingEntity) event.getRightClicked());
        }
    }
}
