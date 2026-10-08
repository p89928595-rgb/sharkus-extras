package de.sharkus.extras;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.List;

public class HudRenderer {
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public static void render(DrawContext ctx, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden) return;
        Config c = Config.get();
        TextRenderer tr = mc.textRenderer;
        int w = ctx.getScaledWindowWidth();
        int h = ctx.getScaledWindowHeight();

        if (c.appleSkin) appleSkin(ctx, mc, tr, w, h);
        if (c.healthIndicator) healthIndicator(ctx, mc, tr, w);
        if (c.armorStatus) armorStatus(ctx, mc, tr, h);
        if (c.teamTracker) teamTracker(ctx, mc, tr);
        if (c.effectTimer) effectTimer(ctx, mc, tr, w);
    }

    // ---------- AppleSkin ----------
    private static void appleSkin(DrawContext ctx, MinecraftClient mc, TextRenderer tr, int w, int h) {
        PlayerEntity p = mc.player;
        ItemStack stack = p.getMainHandStack();
        FoodComponent food = stack.get(DataComponentTypes.FOOD);
        if (food == null) {
            stack = p.getOffHandStack();
            food = stack.get(DataComponentTypes.FOOD);
        }
        int level = p.getHungerManager().getFoodLevel();
        float sat = p.getHungerManager().getSaturationLevel();
        int x = w / 2 + 10;
        int y = h - 62;

        String info = String.format("Hunger %d/20 | Sättigung %.1f", level, sat);
        ctx.drawTextWithShadow(tr, info, x, y, 0xFFFFFFFF);
        if (food != null) {
            int newLevel = Math.min(20, level + food.nutrition());
            float newSat = Math.min(newLevel, sat + food.saturation());
            String gain = String.format("+%d Hunger (%d), +%.1f Sättigung (%.1f)",
                    food.nutrition(), newLevel, food.saturation(), newSat);
            ctx.drawTextWithShadow(tr, gain, x, y - 10, 0xFF55FF55);
        }
    }

    // ---------- Health Indicator ----------
    private static void healthIndicator(DrawContext ctx, MinecraftClient mc, TextRenderer tr, int w) {
        if (!(mc.targetedEntity instanceof LivingEntity e) || !e.isAlive()) return;
        String s = String.format("♥ %.1f / %.1f", e.getHealth(), e.getMaxHealth());
        if (e.getAbsorptionAmount() > 0) s += String.format(" (+%.1f)", e.getAbsorptionAmount());
        float ratio = e.getHealth() / Math.max(1f, e.getMaxHealth());
        int color = ratio > 0.6f ? 0xFF55FF55 : ratio > 0.3f ? 0xFFFFFF55 : 0xFFFF5555;
        Text name = e.getDisplayName();
        ctx.drawCenteredTextWithShadow(tr, name, w / 2, 28, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(tr, s, w / 2, 40, color);
    }

    // ---------- Armor Status ----------
    private static void armorStatus(DrawContext ctx, MinecraftClient mc, TextRenderer tr, int h) {
        int x = 4;
        int y = h / 2 - 40;
        int row = 0;
        for (EquipmentSlot slot : ARMOR) {
            ItemStack s = mc.player.getEquippedStack(slot);
            if (s.isEmpty()) continue;
            int yy = y + row * 18;
            ctx.drawItem(s, x, yy);
            if (s.isDamageable()) {
                int left = s.getMaxDamage() - s.getDamage();
                int pct = Math.round(100f * left / s.getMaxDamage());
                int color = pct > 60 ? 0xFF55FF55 : pct > 30 ? 0xFFFFFF55 : 0xFFFF5555;
                ctx.drawTextWithShadow(tr, left + " (" + pct + "%)", x + 20, yy + 4, color);
            }
            row++;
        }
    }

    // ---------- Team Tracker ----------
    private static void teamTracker(DrawContext ctx, MinecraftClient mc, TextRenderer tr) {
        PlayerEntity me = mc.player;
        List<AbstractClientPlayerEntity> others = mc.world.getPlayers().stream()
                .filter(p -> p != me)
                .sorted(Comparator.comparingDouble(p -> p.squaredDistanceTo(me)))
                .limit(8).toList();
        if (others.isEmpty()) return;
        int x = 4, y = 4;
        ctx.drawTextWithShadow(tr, "Spieler in der Nähe:", x, y, 0xFFFFAA00);
        y += 10;
        for (AbstractClientPlayerEntity p : others) {
            boolean mate = me.getScoreboardTeam() != null && me.isTeammate(p);
            int dist = (int) Math.round(me.distanceTo(p));
            Text line = Text.empty()
                    .append(mate ? Text.literal("[Team] ").styled(st -> st.withColor(0x55FF55)) : Text.empty())
                    .append(p.getDisplayName())
                    .append(Text.literal(" " + dist + "m").styled(st -> st.withColor(0xAAAAAA)));
            ctx.drawTextWithShadow(tr, line, x, y, 0xFFFFFFFF);
            y += 10;
        }
    }

    // ---------- Effekt-Timer ----------
    private static void effectTimer(DrawContext ctx, MinecraftClient mc, TextRenderer tr, int w) {
        int y = 50;
        for (StatusEffectInstance e : mc.player.getStatusEffects()) {
            String time = e.isInfinite() ? "∞" : fmt(e.getDuration());
            String amp = e.getAmplifier() > 0 ? " " + (e.getAmplifier() + 1) : "";
            Text t = Text.empty()
                    .append(e.getEffectType().value().getName())
                    .append(amp + " - " + time);
            int tw = tr.getWidth(t);
            boolean low = !e.isInfinite() && e.getDuration() < 200;
            ctx.drawTextWithShadow(tr, t, w - tw - 4, y, low ? 0xFFFF5555 : 0xFFFFFFFF);
            y += 10;
        }
    }

    private static String fmt(int ticks) {
        int s = ticks / 20;
        return String.format("%d:%02d", s / 60, s % 60);
    }
}
