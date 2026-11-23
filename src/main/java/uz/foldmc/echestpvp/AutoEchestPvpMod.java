package uz.foldmc.echestpvp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AutoEchestPvpMod implements ClientModInitializer {
    public static KeyBinding triggerKey;

    @Override
    public void onInitializeClient() {
        // B tugmasi (Controls'dan o'zgartirish mumkin)
        triggerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.auto-echest-pvp.trigger",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.categories.auto-echest-pvp"
        ));

        // Tick boshida: jarayon ketayotgan bo'lsa harakat tugmalarini o'chiramiz
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (EchestTrigger.isBusy()) {
                EchestTrigger.lockMovement(client);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (triggerKey.wasPressed()) {
                EchestTrigger.start(client);
            }
            EchestTrigger.tick(client);
        });
    }
}
