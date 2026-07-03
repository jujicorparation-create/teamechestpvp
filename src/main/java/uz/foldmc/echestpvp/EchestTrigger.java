package uz.foldmc.echestpvp;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.PlayerScreenHandler;

public class EchestTrigger {

    // ==== SOZLAMALAR ====
    public static final float HP_THRESHOLD = 8.0f; // 4 yurak - bosmasang shu HPda avtomatik ishlaydi
    private static final long COOLDOWN_MS = 2000; // tezlashtirildi (avval 5000)
    private static final int GUI_WAIT_TIMEOUT_TICKS = 60; // 3 sekund (avval 5)

    private enum State {
        IDLE,
        WAITING_GUI,
        DEPOSITING,
        DONE
    }

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
                boolean hpTrigger = player.getHealth() <= HP_THRESHOLD && cooldownOk;
                boolean manualTrigger = keyPressed && cooldownOk;

                if (hpTrigger || manualTrigger) {
                    lastTriggerTime = System.currentTimeMillis();
                    EchestDeposit.unequipArmorToInventory(client);
                    if (player.networkHandler != null) {
                        player.networkHandler.sendChatCommand("team echest");
                    }
                    state = State.WAITING_GUI;
                    waitCounter = 0;
                }
            }

            case WAITING_GUI -> {
                waitCounter++;
                if (client.currentScreen instanceof HandledScreen<?> screen
                        && !(screen.getScreenHandler() instanceof PlayerScreenHandler)) {
                    state = State.DEPOSITING;
                    waitCounter = 0;
                } else if (waitCounter >= GUI_WAIT_TIMEOUT_TICKS) {
                    System.out.println("[AutoEchestPvP] echest GUI ochilmadi, timeout.");
                    state = State.IDLE;
                }
            }

            case DEPOSITING -> {
                if (client.currentScreen instanceof HandledScreen<?> screen) {
                    EchestDeposit.depositAllToContainer(client, screen);
                }
                state = State.DONE;
                waitCounter = 0;
            }

            case DONE -> {
                client.setScreen(null);
                state = State.IDLE;
            }
        }
    }
            }
