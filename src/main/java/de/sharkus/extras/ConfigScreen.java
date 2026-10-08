package de.sharkus.extras;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ConfigScreen extends Screen {
    private TextFieldWidget wordField;

    public ConfigScreen() {
        super(Text.literal("Sharkus Extras"));
    }

    private static Text label(String name, boolean on) {
        return Text.literal(name + ": " + (on ? "§aAN" : "§cAUS"));
    }

    private void toggle(String name, int x, int y, BooleanSupplier get, Consumer<Boolean> set) {
        addDrawableChild(ButtonWidget.builder(label(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(label(name, get.getAsBoolean()));
            Config.save();
        }).dimensions(x, y, 150, 20).build());
    }

    @Override
    protected void init() {
        Config c = Config.get();
        int L = width / 2 - 155, R = width / 2 + 5;
        int y = 30;

        toggle("AppleSkin", L, y, () -> c.appleSkin, v -> c.appleSkin = v);
        toggle("Health Indicator", R, y, () -> c.healthIndicator, v -> c.healthIndicator = v);
        y += 24;
        toggle("Armor Status", L, y, () -> c.armorStatus, v -> c.armorStatus = v);
        toggle("Team Tracker", R, y, () -> c.teamTracker, v -> c.teamTracker = v);
        y += 24;
        toggle("Effekt-Timer", L, y, () -> c.effectTimer, v -> c.effectTimer = v);
        toggle("Wortfilter", R, y, () -> c.wordFilter, v -> c.wordFilter = v);
        y += 24;
        toggle("Hit Color", L, y, () -> c.hitColor, v -> c.hitColor = v);
        addDrawableChild(ButtonWidget.builder(Text.literal("Farbe: " + Config.COLOR_NAMES[c.hitColorIndex]), b -> {
            c.hitColorIndex = (c.hitColorIndex + 1) % Config.COLORS.length;
            b.setMessage(Text.literal("Farbe: " + Config.COLOR_NAMES[c.hitColorIndex]));
            Config.save();
        }).dimensions(R, y, 150, 20).build());
        y += 34;

        // Wortfilter-Liste
        wordField = new TextFieldWidget(textRenderer, L, y, 230, 20, Text.literal("Wort"));
        wordField.setPlaceholder(Text.literal("Wort zum Blockieren eingeben..."));
        wordField.setMaxLength(40);
        addDrawableChild(wordField);
        addDrawableChild(ButtonWidget.builder(Text.literal("Hinzufügen"), b -> addWord())
                .dimensions(L + 235, y, 75, 20).build());
        y += 26;

        int i = 0;
        for (String word : c.blockedWords) {
            if (i >= 12) break;
            int x = (i % 2 == 0) ? L : R;
            int yy = y + (i / 2) * 22;
            final String wd = word;
            addDrawableChild(ButtonWidget.builder(Text.literal("§c✖ §f" + word), b -> {
                c.blockedWords.remove(wd);
                Config.save();
                clearAndInit();
            }).dimensions(x, yy, 150, 20).build());
            i++;
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Fertig"), b -> close())
                .dimensions(width / 2 - 50, height - 28, 100, 20).build());
    }

    private void addWord() {
        String w = wordField.getText().trim();
        if (w.isEmpty()) return;
        Config c = Config.get();
        if (c.blockedWords.stream().noneMatch(x -> x.equalsIgnoreCase(w))) c.blockedWords.add(w);
        Config.save();
        clearAndInit();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == 257 || keyCode == 335) && wordField != null && wordField.isFocused()) {
            addWord();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, "§b§lSharkus Extras", width / 2, 12, 0xFFFFFFFF);
    }

    @Override
    public void close() {
        Config.save();
        super.close();
    }
}
