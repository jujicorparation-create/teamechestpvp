package uz.foldmc.echestpvp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AutoEchestPvpMod implements ClientModInitializer {

    public static final String MOD_ID = "auto-echest-pvp";
    public static KeyBinding triggerKey;

    @Override
    public void onInitializeClient() {
        triggerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.auto-echest-pvp.trigger",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "key.categories.auto-echest-pvp"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(EchestTrigger::onTick);
        System.out.println("[AutoEchestPvP] yuklandi. Trigger: G tugmasi");
    }
                                                  }
