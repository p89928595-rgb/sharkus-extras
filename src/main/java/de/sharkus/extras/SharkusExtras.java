package de.sharkus.extras;

import de.sharkus.extras.mixin.OverlayTextureAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SharkusExtras implements ClientModInitializer {
    private static KeyBinding openKey;
    private static int lastAppliedColor = -1;

    @Override
    public void onInitializeClient() {
        Config.get();

        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.sharkusextras.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.sharkusextras"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) client.setScreen(new ConfigScreen());
            updateHitColor(client);
        });

        HudRenderCallback.EVENT.register(HudRenderer::render);

        // Wortfilter: blockierte Nachrichten werden gar nicht erst angezeigt
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signed, sender, params, ts) ->
                !isBlocked(message.getString()));
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) ->
                overlay || !isBlocked(message.getString()));
    }

    public static boolean isBlocked(String text) {
        Config c = Config.get();
        if (!c.wordFilter || c.blockedWords.isEmpty()) return false;
        String raw = text.toLowerCase();
        String stripped = raw.replaceAll("[^\\p{L}\\p{N}]", "");
        for (String w : c.blockedWords) {
            String word = w.toLowerCase().trim();
            if (word.isEmpty()) continue;
            if (raw.contains(word)) return true;
            String ws = word.replaceAll("[^\\p{L}\\p{N}]", "");
            // auch getarnte Schreibweisen wie "n.o.r.d" erwischen
            if (ws.length() >= 3 && stripped.contains(ws)) return true;
        }
        return false;
    }

    private static void updateHitColor(MinecraftClient client) {
        if (client.gameRenderer == null) return;
        Config c = Config.get();
        int rgb = c.hitColor ? c.currentHitColor() : 0xFF0000; // Vanilla = Rot
        if (rgb == lastAppliedColor) return;
        try {
            NativeImageBackedTexture tex = ((OverlayTextureAccessor) client.gameRenderer.getOverlayTexture()).sharkus$getTexture();
            NativeImage img = tex.getImage();
            if (img == null) return;
            int argb = (178 << 24) | rgb; // gleiche Deckkraft wie Vanilla
            for (int y = 0; y < 8; y++)
                for (int x = 0; x < 16; x++)
                    img.setColorArgb(x, y, argb);
            tex.upload();
            lastAppliedColor = rgb;
        } catch (Throwable t) {
            t.printStackTrace();
            lastAppliedColor = rgb;
        }
    }
}
