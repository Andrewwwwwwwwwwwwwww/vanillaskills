package io.github.andrewwwwwwwwwwwwwww.vanillaskills.mixin;

import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Exposes ArmorStand's private setMarker so the bounty-board floating-text stand has no hitbox. */
@Mixin(ArmorStand.class)
public interface ArmorStandAccessor {

    @Invoker("setMarker")
    void vanillaskills$setMarker(boolean marker);
}
