package uz.foldmc.echestpvp;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ItemStack;

public class EchestDeposit {

    // PlayerScreenHandler (syncId 0) slot indexlari:
    // 5 = helmet, 6 = chestplate, 7 = leggings, 8 = boots
    private static final int[] ARMOR_SLOTS = {5, 6, 7, 8};

    /**
     * Kiyib turgan armorni asosiy inventarga o'tkazadi (player o'z inventarida,
     * hech qanday tashqi GUI ochmasdan - syncId 0 doim mavjud).
     */
    public static void unequipArmorToInventory(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        ClientPlayerInteractionManager im = client.interactionManager;
        if (player == null || im == null) return;

        for (int slot : ARMOR_SLOTS) {
            ItemStack stack = player.playerScreenHandler.getSlot(slot).getStack();
            if (!stack.isEmpty()) {
                im.clickSlot(player.playerScreenHandler.syncId, slot, 0,
                        SlotActionType.QUICK_MOVE, player);
            }
        }
    }

    /**
     * Ochiq container (echest) ichidagi player inventory qismidagi
     * barcha itemlarni shift-click orqali containerga o'tkazadi.
     * Slot indexlari: 0..containerSize-1 = container, keyin player inventar.
     */
    public static void depositAllToContainer(MinecraftClient client, HandledScreen<?> screen) {
        ClientPlayerEntity player = client.player;
        ClientPlayerInteractionManager im = client.interactionManager;
        if (player == null || im == null) return;

        ScreenHandler handler = screen.getScreenHandler();
        int totalSlots = handler.slots.size();
        // Container hajmini handler.slots ichidan hisoblaymiz: oxirgi 36 slot
        // (27 inv + 9 hotbar) player inventoryga tegishli, qolgani container.
        int playerInvStart = totalSlots - 36;
        if (playerInvStart < 0) playerInvStart = 0;

        for (int i = playerInvStart; i < totalSlots; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (!stack.isEmpty()) {
                im.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, player);
            }
        }
    }
}
