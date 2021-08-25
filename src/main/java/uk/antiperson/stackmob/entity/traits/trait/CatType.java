package uk.antiperson.stackmob.entity.traits.trait;

import org.bukkit.entity.Ocelot;
import org.bukkit.entity.LivingEntity;
import uk.antiperson.stackmob.entity.traits.Trait;
import uk.antiperson.stackmob.entity.traits.TraitMetadata;

@TraitMetadata(entity = Ocelot.class, path = "cat-type")
public class CatType implements Trait {

    @Override
    public boolean checkTrait(LivingEntity first, LivingEntity nearby) {
        return ((Ocelot) first).getCatType() != ((Ocelot) nearby).getCatType();
    }

    @Override
    public void applyTrait(LivingEntity spawned, LivingEntity dead) {
        ((Ocelot) spawned).setCatType(((Ocelot) dead).getCatType());
    }
}
