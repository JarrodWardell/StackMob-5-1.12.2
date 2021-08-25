package uk.antiperson.stackmob.entity;

import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.bukkit.entity.LivingEntity;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.io.Serializable;


public class EntityData implements Serializable {
    private transient final EntityManager em;
    private final HashMap<UUID, StackEntity> stackEntities;

    public EntityData (EntityManager em) {
        stackEntities = new HashMap<>();
        this.em = em;
    }

    public EntityData (EntityManager em, HashMap<UUID, StackEntity> stackEntities) {
        this.stackEntities = stackEntities;
        this.em = em;
    }

    public HashMap<UUID, StackEntity> data() {
        return stackEntities;
    }

    public void addIfNot(UUID uuid, StackEntity se) {
        if (!stackEntities.containsKey(uuid)) stackEntities.put(uuid, se);
    }

    public void remove(UUID uuid) {
        stackEntities.remove(uuid);
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

    public boolean saveData(String filePath) {
        try {
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(new GZIPOutputStream(new FileOutputStream(filePath)));
            out.writeObject(this);
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static EntityData loadData(String filePath, EntityManager em) {
        if (!fileExists(filePath)) {
            return new EntityData(em);
        }
        try {
            BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream(filePath)));
            EntityData data = (EntityData) in.readObject();
            in.close();
            return new EntityData(em, data.data());
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
