package uk.antiperson.stackmob.utils;

import net.minecraft.server.v1_12_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_12_R1.DataWatcher;
import net.minecraft.server.v1_12_R1.DataWatcherObject;
import net.minecraft.server.v1_12_R1.DataWatcherRegistry;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class NMSHelper {

    public static void sendPacket(Player player, Entity entity, boolean tagVisible) {
        CraftEntity craftEntity = (CraftEntity) entity;
        DataWatcher watcher = new DataWatcher(craftEntity.getHandle());
        watcher.register(new DataWatcherObject<>(3, DataWatcherRegistry.i), tagVisible);
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(craftEntity.getHandle().getId(), watcher, true);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}