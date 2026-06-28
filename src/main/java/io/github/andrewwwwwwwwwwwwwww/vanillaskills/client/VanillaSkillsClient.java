package io.github.andrewwwwwwwwwwwwwww.vanillaskills.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Client-only entrypoint. Registers two rebindable keys (Options → Controls → "VanillaSkills") that
 * open the Skill Tree and the Bounty Board.
 *
 * <p>The GUIs are server-side chest menus, so the keys simply run the server's {@code /skill} and
 * {@code /quests} commands. They only fire when the connected server actually declares those commands
 * (the command tree is synced to the client) — i.e. only on a server running VanillaSkills — so on a
 * vanilla / non-mod server the keys do nothing instead of spamming "Unknown command".
 */
public class VanillaSkillsClient implements ClientModInitializer {

    private KeyMapping openSkills;
    private KeyMapping openQuests;

    @Override
    public void onInitializeClient() {
        // Load global client preferences (e.g. the opt-in narrator disable).
        ClientConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("vanillaskills", "keybinds"));

        // Defaults: ] for the skill tree, [ for the bounty board — both unbound in vanilla, unlike the
        // commonly-used B/V. Players can rebind under Options -> Controls -> VanillaSkills.
        openSkills = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vanillaskills.open_skills", GLFW.GLFW_KEY_RIGHT_BRACKET, category));
        openQuests = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.vanillaskills.open_quests", GLFW.GLFW_KEY_LEFT_BRACKET, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSkills.consumeClick()) runServerCommand(client, "skill");
            while (openQuests.consumeClick()) runServerCommand(client, "quests");
        });
    }

    private static void runServerCommand(Minecraft client, String command) {
        if (client.player == null) return;
        ClientPacketListener connection = client.getConnection();
        if (connection == null) return;
        // Only send if the connected server declares this command (i.e. it's running VanillaSkills).
        if (connection.getCommands().getRoot().getChild(command) == null) return;
        connection.sendCommand(command);
    }
}
