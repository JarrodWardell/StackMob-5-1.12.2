package uk.antiperson.stackmob.entity;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import uk.antiperson.stackmob.StackMob;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import java.io.Serializable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.Map;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

/*
    FIXME Data Storage
    The stored data may include entities that have been removed in ways the plugin cannot recognize, leading to pointless entries in the save file and loaded into ram.
    Probably the best fix for this would be to use sm.getServer().getEntity(uuid) != null to check if the entity still exists, but I'm uncertain on if this works for unloaded mobs, and how much of an impact on performance this has.
*/

public class EntityManager implements Serializable {

    private transient final StackMob sm;
    private transient final HashMap<UUID, StackEntity> stackEntities;
    private final HashMap<UUID, Integer> stackValues;

    public EntityManager (StackMob sm) {
        this(sm, new HashMap<>());
    }

    public EntityManager (StackMob sm, HashMap<UUID, Integer> stackValues) {
        stackEntities = new HashMap<>();
        this.stackValues = stackValues;
        this.sm = sm;
    }

    public HashMap<UUID, Integer> data() {
        return stackValues;
    }

    private void addIfNot(UUID uuid, StackEntity se) {
        if (!stackEntities.containsKey(uuid)) {
            stackEntities.put(uuid, se);
            if (stackValues.containsKey(uuid)) se.setSize(stackValues.get(uuid));
            else stackValues.put(uuid, se.getSize());
        }
    }

    public boolean isStackedEntity(LivingEntity entity) {
        return stackEntities.containsKey(entity.getUniqueId());
    }

    public Collection<StackEntity> getStackEntities() {
        return stackEntities.values();
    }

    public StackEntity getStackEntity(LivingEntity entity) {
        return stackEntities.get(entity.getUniqueId());
    }

    public void registerAllEntities() {
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                registerStackedEntities(chunk);
            }
        }
    }

    public void unregisterAllEntities() {
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                unregisterStackedEntities(chunk);
            }
        }
    }

    public boolean hasStackData(Entity entity) {
        return !entity.getMetadata(sm.getStackKey()).isEmpty();
    }

    public void registerStackedEntities(Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {
            if (!(entity instanceof LivingEntity)) {
                continue;
            }
            if (!sm.getEntityManager().data().containsKey(entity.getUniqueId())) {
                if (entity.getCustomName() != null) {
                    continue;
                }
                /*if (!hasStackData(entity)) {
                    //continue;
                }*/
            }
            sm.getEntityManager().registerStackedEntity((LivingEntity) entity);
        }
    }

    public void unregisterStackedEntities(Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {
            if (!(entity instanceof LivingEntity)) {
                continue;
            }
            StackEntity stackEntity = sm.getEntityManager().getStackEntity((LivingEntity) entity);
            if (stackEntity == null) {
                continue;
            }
            sm.getEntityManager().unregisterStackedEntity(stackEntity);
        }
    }

    public StackEntity registerStackedEntity(LivingEntity entity) {
        StackEntity stackEntity = new StackEntity(sm, entity);
        addIfNot(entity.getUniqueId(), stackEntity);
        return stackEntity;
    }

    public void registerStackedEntity(StackEntity entity) {
        addIfNot(entity.getEntity().getUniqueId(), entity);
    }

    public void unregisterStackedEntity(LivingEntity entity) {
        StackEntity stackEntity = getStackEntity(entity);
        if (stackEntity == null) {
            throw new UnsupportedOperationException("Attempted to unregister entity that isn't stacked!");
        }
        unregisterStackedEntity(stackEntity);
    }

    public void unregisterStackedEntity(StackEntity stackEntity) {
        UUID uuid = stackEntity.getEntity().getUniqueId();
        stackEntities.remove(uuid);
        stackValues.remove(uuid);
    }

    public boolean saveData(String filePath) {
        sm.getLogger().info("Saving stack values...");
        for (Map.Entry<UUID, StackEntity> entry : stackEntities.entrySet()) stackValues.put(entry.getKey(), entry.getValue().getSize()); // set the hashmap to have the updated amount of entities
        
        try {
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(new GZIPOutputStream(new FileOutputStream(filePath)));
            out.writeObject(this);
            out.close();
            sm.getLogger().info("Saved!");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static EntityManager loadData(String filePath, StackMob sm) {
        sm.getLogger().info("Loading stack values...");
        if (!fileExists(filePath)) {
            sm.getLogger().info("No stack values, creating blank.");
            return new EntityManager(sm);
        }
        try {
            BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream(filePath)));
            EntityManager data = (EntityManager) in.readObject();
            in.close();
            sm.getLogger().info("Loaded " + data.data().size() + " stack values.");
            return new EntityManager(sm, data.data());
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean fileExists(String filePath) {
        File f = new File(filePath);
        return (f.isFile() && f.canRead());
    }
}
