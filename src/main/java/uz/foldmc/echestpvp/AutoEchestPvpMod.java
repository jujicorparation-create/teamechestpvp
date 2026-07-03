package uz.foldmc.echestpvp;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class AutoEchestPvpMod implements ClientModInitializer {

    public static final String MOD_ID = "auto-echest-pvp";

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(EchestTrigger::onTick);
        System.out.println("[AutoEchestPvP] yuklandi. HP threshold: " + EchestTrigger.HP_THRESHOLD);
    }
}

