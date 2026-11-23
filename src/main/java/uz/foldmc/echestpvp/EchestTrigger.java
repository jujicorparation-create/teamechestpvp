package uz.foldmc.echestpvp;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public final class EchestTrigger {
    // ---- Sozlamalar ----
    /** B bosilgandan keyin keyingi ishga tushirishgacha minimal vaqt (ms). */
    public static final long COOLDOWN_MS = 500;
    /** Echest GUI ochilmasa, necha tickdan keyin bekor qilinadi (100 tick = 5 sek). */
    public static final int GUI_WAIT_TIMEOUT_TICKS = 100;
    /**
     * Armor/offhand yechilgandan keyin komanda yuborishdan oldin kutiladigan tick.
     * 0 = darhol (eng tez). Armor/offhand echestga tushmay qolsa 1-2 ga ko'tar.
     */
    public static final int TICKS_AFTER_UNEQUIP = 1;
    /** Bir tickda nechta slot echestga ko'chiriladi (katta = tezroq). */
    public static final int CLICKS_PER_TICK = 12;

    // PlayerScreenHandler slotlari: 5-8 = armor, 45 = offhand
    private static final int ARMOR_FIRST = 5;
    private static final int ARMOR_LAST = 8;
    private static final int OFFHAND_SLOT = 45;

    private enum State { IDLE, WAIT_ARMOR, WAIT_GUI, MOVING }

    private static State state = State.IDLE;
    private static int ticks = 0;
    private static int moveIndex = 0;
    private static long lastTriggerMs = 0;

    private EchestTrigger() {}

    public static boolean isBusy() {
        return state != State.IDLE;
    }

    /** Jarayon ketayotganda joyida turish uchun harakat tugmalarini o'chiradi. */
    public static void lockMovement(MinecraftClient client) {
        GameOptions o = client.options;
        o.forwardKey.setPressed(false);
        o.backKey.setPressed(false);
        o.leftKey.setPressed(false);
        o.rightKey.setPressed(false);
        o.jumpKey.setPressed(false);
        o.sneakKey.setPressed(false);
        o.sprintKey.setPressed(false);
    }

    /** B bosilganda chaqiriladi. */
    public static void start(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null || client.getNetworkHandler() == null) return;
        if (state != State.IDLE || client.currentScreen != null) return;
        if (System.currentTimeMillis() - lastTriggerMs < COOLDOWN_MS) return;

        int syncId = player.playerScreenHandler.syncId;

        // 1) Armor + offhand ni inventarga yechish (shift-click)
        for (int slot = ARMOR_FIRST; slot <= ARMOR_LAST; slot++) {
            if (player.playerScreenHandler.getSlot(slot).hasStack()) {
                client.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.QUICK_MOVE, player);
            }
        }
        if (player.playerScreenHandler.getSlot(OFFHAND_SLOT).hasStack()) {
            client.interactionManager.clickSlot(syncId, OFFHAND_SLOT, 0, SlotActionType.QUICK_MOVE, player);
        }

        ticks = 0;
        if (TICKS_AFTER_UNEQUIP <= 0) {
            // 2) Darhol komanda
            client.getNetworkHandler().sendChatCommand("team echest");
            state = State.WAIT_GUI;
        } else {
            state = State.WAIT_ARMOR;
        }
    }

    public static void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.interactionManager == null || client.getNetworkHandler() == null) {
            state = State.IDLE;
            return;
        }

        switch (state) {
            case IDLE -> { }
            case WAIT_ARMOR -> {
                if (++ticks >= TICKS_AFTER_UNEQUIP) {
                    client.getNetworkHandler().sendChatCommand("team echest");
                    ticks = 0;
                    state = State.WAIT_GUI;
                }
            }
            case WAIT_GUI -> {
                // 3) GUI ochilishi bilan darhol ko'chirishni boshlaymiz
                if (client.currentScreen instanceof GenericContainerScreen screen) {
                    moveIndex = screen.getScreenHandler().getRows() * 9;
                    state = State.MOVING;
                    moveStep(client, player); // shu tickning o'zida boshlaymiz
                } else if (++ticks > GUI_WAIT_TIMEOUT_TICKS) {
                    abort(player, "Echest ochilmadi, bekor qilindi");
                }
            }
            case MOVING -> moveStep(client, player);
        }
    }

    /** 4) Hamma narsani (hotbar + inventar + yechilgan armor/offhand) echestga o'tkazish. */
    private static void moveStep(MinecraftClient client, ClientPlayerEntity player) {
        if (!(client.currentScreen instanceof GenericContainerScreen screen)) {
            abort(player, "Echest oynasi yopildi");
            return;
        }
        GenericContainerScreenHandler handler = screen.getScreenHandler();
        int clicks = 0;
        while (moveIndex < handler.slots.size() && clicks < CLICKS_PER_TICK) {
            if (handler.getSlot(moveIndex).hasStack()) {
                client.interactionManager.clickSlot(
                        handler.syncId, moveIndex, 0, SlotActionType.QUICK_MOVE, player);
                clicks++;
            }
            moveIndex++;
        }
        if (moveIndex >= handler.slots.size()) {
            // 5) Tugadi: GUI yopiladi, harakat qayta ochiladi
            player.closeHandledScreen();
            lastTriggerMs = System.currentTimeMillis();
            state = State.IDLE;
        }
    }

    private static void abort(ClientPlayerEntity player, String reason) {
        player.sendMessage(Text.literal("§c[AutoEchest] " + reason), true);
        lastTriggerMs = System.currentTimeMillis();
        state = State.IDLE;
    }
}
