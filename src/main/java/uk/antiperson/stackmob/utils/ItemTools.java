package uk.antiperson.stackmob.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
//import org.bukkit.persistence.PersistentDataType;
import uk.antiperson.stackmob.StackMob;

import java.util.Arrays;

public class ItemTools {

    private final StackMob sm;
    public ItemTools(StackMob sm) {
        this.sm = sm;
    }

    public ItemStack createStackingTool() {
        ItemStack is = new ItemStack(Material.BONE, 1);
        is.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 100);
        ItemMeta itemMeta = is.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GOLD + "The Stick Of Stacking");
        itemMeta.setLore(Arrays.asList(ChatColor.GREEN + "A useful tool for modifying stacked mobs.",
                ChatColor.GOLD + "Right click to perform action" ,
                ChatColor.GOLD + "Shift-right click to change mode."));
        is.setItemMeta(itemMeta);
        NBTHelper.setNBTInt(is, sm.getToolKey(), 1);
        return is;
    }

    public void giveStackingTool(Player player) {
        player.getInventory().addItem(createStackingTool());
    }

    public boolean isStackingTool(ItemStack is) {
        /*if (is.getItemMeta() == null) {
            return false;
        }*/
        return NBTHelper.hasNBT(is, sm.getToolKey());
    }

}
