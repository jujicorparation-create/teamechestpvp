package net.egoist.combatsaver.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class MatrixBypassFreeFly {
    public static KeyBinding flyKey;
    private static boolean isFlyActive = false;
    
    // Matrix tekshirmagani uchun tezlikni bemalol 0.8 qilsak ham bo'ladi!
    private static final double FLY_SPEED = 0.8; 

    public static void register() {
        flyKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.egoist.bypassfly",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.egoist"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (flyKey.wasPressed()) {
                isFlyActive = !isFlyActive;
                if (isFlyActive) {
                    client.player.sendMessage(Text.literal("§a[EgoistFly] Matrix Bypass faollashdi!"), true);
                } else {
                    client.player.sendMessage(Text.literal("§c[EgoistFly] O'chirildi!"), true);
                    if (!client.player.isCreative()) {
                        client.player.getAbilities().allowFlying = false;
                        client.player.getAbilities().flying = false;
                    }
                    client.player.setVelocity(0, 0, 0);
                }
            }

            if (isFlyActive) {
                // Server Matrix tekshiruvini chetlab o'tish uchun asosiy kod:
                client.player.getAbilities().allowFlying = true;
                client.player.getAbilities().flying = true;

                double x = 0, y = 0, z = 0;
                float yaw = client.player.getYaw();
                float pitch = client.player.getPitch();

                // W tugmasi - Kamera qaragan tomonga (X, Y, Z) uchish
                if (client.options.forwardKey.isPressed()) {
                    Vec3d look = Vec3d.fromPolar(pitch, yaw);
                    x += look.x * FLY_SPEED;
                    y += look.y * FLY_SPEED;
                    z += look.z * FLY_SPEED;
                }
                // S tugmasi - Orqaga uchish
                if (client.options.backKey.isPressed()) {
                    Vec3d look = Vec3d.fromPolar(pitch, yaw);
                    x -= look.x * FLY_SPEED;
                    y -= look.y * FLY_SPEED;
                    z -= look.z * FLY_SPEED;
                }

                // Qo'shimcha boshqaruv: Probel va Shift
                if (client.options.jumpKey.isPressed()) {
                    y = FLY_SPEED;
                }
                if (client.options.sneakKey.isPressed()) {
                    y = -FLY_SPEED;
                }

                // Vektor tezligini o'rnatish
                client.player.setVelocity(x, y, z);
                
                // Yiqilish ziyonini bloklash
                client.player.fallDistance = 0;
            }
        });
    }
}
