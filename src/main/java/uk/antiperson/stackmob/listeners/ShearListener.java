package uk.antiperson.stackmob.listeners;

import org.bukkit.Material;
//import org.bukkit.Tag;
//import org.bukkit.block.Dispenser;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.ItemMeta;
import uk.antiperson.stackmob.StackMob;
import uk.antiperson.stackmob.entity.Drops;
import uk.antiperson.stackmob.entity.StackEntity;
//import org.bukkit.inventory.ItemStack;
/*
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
*/

@ListenerMetadata(config = "events.shear.enabled")
public class ShearListener implements Listener {

    private final StackMob sm;

    public ShearListener(StackMob sm) {
        this.sm = sm;
    }

    @EventHandler
    public void onShearSheep(PlayerShearEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        EquipmentSlot equipmentSlot = findShears(event.getPlayer());
        if (equipmentSlot == null) {
            sm.getLogger().info("A player just managed to shear an entity while not holding shears.");
            return;
        }
        ItemStack is = shearLogic((LivingEntity) event.getEntity(), event.getPlayer().getInventory().getItem(equipmentSlot));
        if (is == null) {
            return;
        }
        event.getPlayer().getInventory().setItem(equipmentSlot, is);
    }

    /*@EventHandler
    public void onShearSheep(BlockShearEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        ItemStack is = shearLogic((LivingEntity) event.getEntity(), event.getTool());
        int durability = ((Damageable) event.getTool().getItemMeta()).getDamage();
        int maxDurability = event.getTool().getType().getMaxDurability();
        if (is == null || (maxDurability - durability) == 1) {
            return;
        }
        sm.getServer().getScheduler().runTask(sm, () -> {
            Dispenser dispenser = (Dispenser) event.getBlock().getState();
            dispenser.getInventory().setItem(dispenser.getInventory().first(event.getTool()), is);
        });
    }*/

    private EquipmentSlot findShears(Player player) {
        EquipmentSlot hand = checkSlot(player, EquipmentSlot.HAND);
        EquipmentSlot offHand = checkSlot(player, EquipmentSlot.OFF_HAND);
        return hand == null ? offHand : hand;
    }

    private EquipmentSlot checkSlot(Player player, EquipmentSlot slot) {
        if (player.getInventory().getItem(slot).getType() == Material.SHEARS) {
            return slot;
        }
        return null;
    }

    private ItemStack shearLogic(LivingEntity entity, ItemStack item) {
        if (!((entity instanceof Sheep) || (entity instanceof MushroomCow))) {
            return null;
        }
        StackEntity stackEntity = sm.getEntityManager().getStackEntity(entity);
        if (stackEntity == null || stackEntity.isSingle()) {
            return null;
        }
        ListenerMode shear = sm.getMainConfig().getListenerMode(entity.getType(), "shear");
        if (shear == ListenerMode.SPLIT) {
            StackEntity slice = stackEntity.slice();
            if (slice.getEntity() instanceof Sheep) {
                ((Sheep) slice.getEntity()).setSheared(false);
            }
            return null;
        }
        int limit = sm.getMainConfig().getEventMultiplyLimit(entity.getType(), "shear", stackEntity.getSize());
        short health = item.getType().getMaxDurability();
        short amount = (short) Math.min(health, limit);
        
        stackEntity.splitIfNotEnough(amount);
        short damage = (short) (health - amount);
        if (damage > 0) {
            item.setDurability((short) (item.getDurability() - amount));
        } else {
            item = new ItemStack(Material.AIR);
        }
        if (entity instanceof Sheep) {
            sm.getServer().getScheduler().runTaskLater(sm, new Runnable() {
                public void run() {
                    for (Entity nearby : entity.getNearbyEntities(4, 4, 4)) {
                        if (nearby instanceof Item && ((Item) nearby).getItemStack().getType() == Material.WOOL) {
                            int total = 0;
                            for (int i = amount; i > 0; i--) {
                                total += (int)(Math.random() * ((3 - 1) + 1)) + 1; // since loot tables aren't here, i need to generate the wool amounts myself :(
                            }
                            while (total > 64) {
                                ((Item) nearby).getItemStack().clone().setAmount(64);
                                total -= 64;
                            }
                            ((Item) nearby).getItemStack().setAmount(total);
                            return;
                        }
                    }
                }
            }, 4);
            return item;
        }
        MushroomCow mushroomCow = (MushroomCow) entity;
        ItemStack mushrooms = new ItemStack(Material.RED_MUSHROOM, 1);
        Drops.dropItem(mushroomCow.getLocation(), mushrooms, (amount - 1) * 5, true);
        return item;
    }
}
