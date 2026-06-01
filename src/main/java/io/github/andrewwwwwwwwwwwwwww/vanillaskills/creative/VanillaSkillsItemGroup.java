package io.github.andrewwwwwwwwwwwwwww.vanillaskills.creative;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.Alloys;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.ArmorPiece;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.ArmorTier;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.ArmorTiers;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.recipe.FortuneTemplate;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;

/**
 * A creative-inventory tab holding all of the mod's custom items (alloys, armor, the Fortune
 * template). Note: the creative menu is built client-side, so this tab only appears for players
 * who have the mod on their client (singleplayer, or anyone who installs it) — on a dedicated
 * server with vanilla clients it won't show, but the items are still obtainable by crafting.
 */
public final class VanillaSkillsItemGroup {
    private VanillaSkillsItemGroup() {}

    public static void register() {
        CreativeModeTab tab = FabricCreativeModeTab.builder()
                .title(Component.literal("VanillaSkills"))
                .icon(Alloys::roseGoldIngot)
                .displayItems((params, output) -> {
                    output.accept(Alloys.roseGoldIngot());
                    output.accept(Alloys.steelIngot());
                    output.accept(FortuneTemplate.create());
                    for (ArmorTier tier : ArmorTiers.TIERS) {
                        for (ArmorPiece piece : ArmorPiece.values()) {
                            output.accept(tier.create(piece));
                        }
                    }
                    for (io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool.ToolTier tier
                            : io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool.ToolTiers.TIERS) {
                        for (io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool.ToolKind kind
                                : io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool.ToolKind.values()) {
                            output.accept(tier.create(kind));
                        }
                    }
                })
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath("vanillaskills", "items"), tab);
    }
}
