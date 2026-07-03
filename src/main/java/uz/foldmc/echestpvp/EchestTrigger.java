package uz.foldmc.echestpvp;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.PlayerScreenHandler;

public class EchestTrigger {

    // ==== SOZLAMALAR ====
    public static final float HP_THRESHOLD = 6.0f; // 3 yurak
    private static final long COOLDOWN_MS = 5000; // trigger orasidagi minimal vaqt
    private static final int GUI_WAIT_TIMEOUT_TICKS = 100; // 5 sekund (20 tick/sek)
    private static final int TICKS_AFTER_COMMAND = 4; // command yuborilgach armor sync bo'lishini kutish

    private enum State {
        IDLE,
        ARMOR_UNEQUIPPED,
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

        switch (state) {
            case IDLE -> {
                if (client.currentScreen != null) return; // biror GUI ochiq bo'lsa tegmaymiz
                if (player.getHealth() <= HP_THRESHOLD
                        && System.currentTimeMillis() - lastTriggerTime >= COOLDOWN_MS) {
                    lastTriggerTime = System.currentTimeMillis();
                    EchestDeposit.unequipArmorToInventory(client);
                    state = State.ARMOR_UNEQUIPPED;
                    waitCounter = 0;
                }
            }

            case ARMOR_UNEQUIPPED -> {
                waitCounter++;
                if (waitCounter >= TICKS_AFTER_COMMAND) {
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
                    // echest ochilmadi (lag yoki boshqa sabab) - qayta urinish uchun reset
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
                waitCounter++;
                if (waitCounter >= 2) { // 1-2 tick GUI'ni ko'rsatib turamiz, keyin yopamiz
                    client.setScreen(null);
                    state = State.IDLE;
                }
            }
        }
    }
}

