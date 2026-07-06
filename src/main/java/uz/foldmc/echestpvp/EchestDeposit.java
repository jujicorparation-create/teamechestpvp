package uz.foldmc.echestpvp;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ItemStack;

public class EchestDeposit {

    private static final int[] ARMOR_SLOTS = {5, 6, 7, 8};

    /**
     * Kiyib turgan armorni asosiy inventarga paket orqali bir lahzada o'tkazadi.
     */
    public static void unequipArmorToInventory(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        ClientPlayerInteractionManager im = client.interactionManager;
        if (player == null || im == null) return;

        int syncId = player.playerScreenHandler.syncId;

        for (int slot : ARMOR_SLOTS) {
            ItemStack stack = player.playerScreenHandler.getSlot(slot).getStack();
            if (!stack.isEmpty()) {
                im.clickSlot(syncId, slot, 0, SlotActionType.QUICK_MOVE, player);
            }
        }
    }

    /**
     * Ochiq container (echest) ichiga barcha itemlarni 0-tickda yuklaydi va tugatadi.
     */
    public static void depositAllToContainer(MinecraftClient client, HandledScreen<?> screen) {
        ClientPlayerEntity player = client.player;
        ClientPlayerInteractionManager im = client.interactionManager;
        if (player == null || im == null) return;

        ScreenHandler handler = screen.getScreenHandler();
        int totalSlots = handler.slots.size();
        
        int playerInvStart = totalSlots - 36;
        if (playerInvStart < 0) playerInvStart = 0;

        // Barcha slotlar bitta tick ichida for-loop orqali bosib chiqiladi
        for (int i = playerInvStart; i < totalSlots; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (!stack.isEmpty()) {
                im.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, player);
            }
        }
    }
}
