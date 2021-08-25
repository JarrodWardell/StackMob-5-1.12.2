package uk.antiperson.stackmob.entity.traits;

import org.bukkit.entity.LivingEntity;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface TraitMetadata {
    Class<? extends LivingEntity> entity();
    String path();
}
