package uk.antiperson.stackmob.entity;

import org.apache.commons.lang.ObjectUtils.Null;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import uk.antiperson.stackmob.StackMob;
import uk.antiperson.stackmob.utils.Utilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class EntityManager {

    private final StackMob sm;
    private final EntityData ed;

    public EntityManager(StackMob sm) {
        this.sm = sm;
        ed = EntityData.loadData("./plugins/StackMob/stackmob.data", this);
    }

    public void remove(UUID uuid) {
        ed.remove(uuid);
    }

    public boolean isStackedEntity(LivingEntity entity) {
        return ed.isStackedEntity(entity);
    }

    public Collection<StackEntity> getStackEntities() {
        return ed.getStackEntities();
    }

    public StackEntity getStackEntity(LivingEntity entity) {
        return ed.getStackEntity(entity);
    }

    public EntityData getDataManager() {
        return ed;
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
            if (entity.getCustomName() != null) {
                continue;
            }
            if (!hasStackData(entity)) {
                continue;
            }
            sm.getEntityManager().registerStackedEntity((LivingEntity) entity);
        }
    }

    public void unregisterStackedEntities(Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {
            if (!(entity instanceof LivingEntity)) {
                continue;
            }
            StackEntity stackEntity = sm.getEntityManager().getDataManager().getStackEntity((LivingEntity) entity);
            if (stackEntity == null) {
                continue;
            }
            sm.getEntityManager().unregisterStackedEntity(stackEntity);
        }
    }

    public StackEntity registerStackedEntity(LivingEntity entity) {
        StackEntity stackEntity = new StackEntity(sm, entity);
        ed.addIfNot(entity.getUniqueId(), stackEntity);
        return stackEntity;
    }

    public void registerStackedEntity(StackEntity entity) {
        ed.addIfNot(entity.getEntity().getUniqueId(), entity);
    }

    public void unregisterStackedEntity(LivingEntity entity) {
        StackEntity stackEntity = ed.getStackEntity(entity);
        if (stackEntity == null) {
            throw new UnsupportedOperationException("Attempted to unregister entity that isn't stacked!");
        }
        unregisterStackedEntity(stackEntity);
    }

    public void unregisterStackedEntity(StackEntity stackEntity) {
        ed.remove(stackEntity.getEntity().getUniqueId());
    }

}
