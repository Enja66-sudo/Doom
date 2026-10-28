package com.doomupdated;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class DoomUpdated implements ClientModInitializer {
    private static KeyBinding openMenu;
    private static KeyBinding toggleHitDelay;

    @Override
    public void onInitializeClient() {
        HitDelay.init();
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.doomupdated.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.MISC
        ));
        // Standard: Numpad 1 (normale Taste 1 waere schon die Hotbar). In den Steuerungen aenderbar.
        toggleHitDelay = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.doomupdated.toggle_hitdelay",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_1,
                KeyBinding.Category.MISC
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new DoomScreen());
            }
            while (toggleHitDelay.wasPressed()) {
                if (client.currentScreen == null) HitDelay.toggle(client);
            }
        });
    }
}
