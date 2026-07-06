package uz.foldmc.echestpvp;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.PlayerScreenHandler;

public class EchestTrigger {

    private static final long COOLDOWN_MS = 1500;
    private static final int GUI_WAIT_TIMEOUT_TICKS = 60; // 3 sekund

    // Ortiqcha DEPOSITING va DONE holatlarini o'chirdik!
    private enum State { IDLE, WAITING_GUI }

    private static State state = State.IDLE;
    private static long lastTriggerTime = 0;
    private static int waitCounter = 0;

    public static void onTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        boolean keyPressed = AutoEchestPvpMod.triggerKey != null && AutoEchestPvpMod.triggerKey.wasPressed();

        switch (state) {
            case IDLE -> {
                if (client.currentScreen != null) return;

                boolean cooldownOk = System.currentTimeMillis() - lastTriggerTime >= COOLDOWN_MS;
                boolean manualTrigger = keyPressed && cooldownOk;

                if (manualTrigger) {
                    lastTriggerTime = System.currentTimeMillis();
                    
                    // 1. Zirhlarni zudlik bilan yechish (0-tick)
                    EchestDeposit.unequipArmorToInventory(client);
                    
                    // 2. Buyruqni to'g'ridan-to'g'ri sendCommand orqali tezkor yuborish
                    if (player.networkHandler != null) {
                        player.networkHandler.sendCommand("team echest");
                    }
                    state = State.WAITING_GUI;
                    waitCounter = 0;
                }
            }

            case WAITING_GUI -> {
                waitCounter++;
                if (client.currentScreen instanceof HandledScreen<?> screen
                        && !(screen.getScreenHandler() instanceof PlayerScreenHandler)) {
                    
                    // CHAOMOO TEZLIGIDA: Ekran aniqlangan soniyaning o'zida narsalarni echestga otadi
                    EchestDeposit.depositAllToContainer(client, screen);
                    
                    // Va hech qanday keyingi tickni kutmasdan ekran shu zaxoti yopiladi
                    screen.close();
                    client.setScreen(null);
                    
                    state = State.IDLE;
                    waitCounter = 0;
                } else if (waitCounter >= GUI_WAIT_TIMEOUT_TICKS) {
                    System.out.println("[AutoEchestPvP] echest GUI ochilmadi, timeout.");
                    state = State.IDLE;
                }
            }
        }
    }
}
