package io.github.andrewwwwwwwwwwwwwww.vanillaskills.mixin;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Reports a horse's real stats when you open its inventory.
 *
 * <p>Horse quality is otherwise invisible — two identical-looking horses can differ enormously in speed and
 * jump, and vanilla gives you no way to tell without racing them.
 *
 * <p><b>Why a message and not the screen title.</b> In 26.2 the horse screen is opened with
 * {@code ClientboundMountScreenOpenPacket(containerId, columns, entityId)}, which carries <b>no title</b> —
 * the client builds the caption from the horse itself. So there is no server-settable title to write into,
 * and the container-title trick used elsewhere in this mod is not available here. The remaining server-side
 * options were renaming the horse (permanent, and rejected) or telling the player directly; this does the
 * latter. Chat rather than the action bar, because the action bar belongs to the in-game HUD, which is not
 * drawn while a container screen is open.
 *
 * <p>Speed is converted into blocks per second and jump into blocks, since the raw attribute values are not
 * something anyone can reason about.
 */
@Mixin(ServerPlayer.class)
public class HorseStatsMixin {

    @Inject(method = "openHorseInventory", at = @At("HEAD"))
    private void vanillaskills$reportHorseStats(AbstractHorse horse, net.minecraft.world.Container container,
                                                CallbackInfo ci) {
        if (!GameplayConfig.HORSE_STATS) return;
        // Not every equine carries every attribute, and getAttributeValue throws on a missing one. A cosmetic
        // readout must never be able to break mounting or opening the screen, so anything unexpected here is
        // swallowed and the player simply gets no stat line.
        if (!horse.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED)
                || !horse.getAttributes().hasAttribute(Attributes.JUMP_STRENGTH)) {
            return;
        }

        double speed = horse.getAttributeValue(Attributes.MOVEMENT_SPEED) * 43.17;   // -> blocks/second
        double jump = horse.getAttributeValue(Attributes.JUMP_STRENGTH);
        double jumpBlocks = -0.1817333 * jump * jump * jump + 3.689713 * jump * jump + 2.128956 * jump - 0.343930;
        double health = horse.getMaxHealth();

        Component line = Component.empty()
                .append(Component.literal(horse.getName().getString() + ": ").withStyle(ChatFormatting.AQUA))
                .append(Component.literal(String.format("Speed %.2f b/s", speed)).withStyle(ChatFormatting.WHITE))
                .append(Component.literal("  |  ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(String.format("Jump %.2f blocks", jumpBlocks)).withStyle(ChatFormatting.WHITE))
                .append(Component.literal("  |  ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(String.format("%.0f HP", health)).withStyle(ChatFormatting.WHITE));

        ((ServerPlayer) (Object) this).sendSystemMessage(line);
    }
}
