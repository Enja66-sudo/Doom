package com.doomupdated;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public final class DoomScreen extends Screen {
    private static final int PANEL = 0xF0161724;
    private static final int PANEL_INNER = 0xFF202236;
    private static final int ACCENT = 0xFF9D72FF;
    private static final int TEXT = 0xFFF4F2FF;
    private static final int MUTED = 0xFFAAA8C4;
    private String category = "Combat";
    private boolean glossy = true;
    private boolean showCps = false;
    private boolean showKeystrokes = false;

    public DoomScreen() { super(Text.literal("Doom Updated")); }

    @Override
    protected void init() {
        clearChildren();
        int left = width / 2 - 180;
        int top = height / 2 - 115;
        addDrawableChild(ButtonWidget.builder(Text.literal("Combat"), b -> { category = "Combat"; rebuild(); })
                .dimensions(left + 14, top + 48, 94, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Visuals"), b -> { category = "Visuals"; rebuild(); })
                .dimensions(left + 14, top + 75, 94, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Obv"), b -> { category = "Obv"; rebuild(); })
                .dimensions(left + 14, top + 102, 94, 22).build());
        if (category.equals("Visuals")) {
            addDrawableChild(ButtonWidget.builder(toggle("Glossy GUI", glossy), b -> { glossy = !glossy; rebuild(); })
                    .dimensions(left + 126, top + 55, 218, 24).build());
            addDrawableChild(ButtonWidget.builder(toggle("CPS display", showCps), b -> { showCps = !showCps; rebuild(); })
                    .dimensions(left + 126, top + 85, 218, 24).build());
            addDrawableChild(ButtonWidget.builder(toggle("Keystrokes display", showKeystrokes), b -> { showKeystrokes = !showKeystrokes; rebuild(); })
                    .dimensions(left + 126, top + 115, 218, 24).build());
        } else if (category.equals("Combat")) {
            addDrawableChild(ButtonWidget.builder(toggle("Hit Delay", HitDelay.enabled), b -> { HitDelay.toggle(client); rebuild(); })
                    .dimensions(left + 126, top + 55, 218, 24).build());
            addDrawableChild(new DelaySlider(left + 126, top + 85, 218, 24));
            addDrawableChild(ButtonWidget.builder(Text.literal("Toggle-Taste: Numpad 1 (aenderbar)"), b -> {})
                    .dimensions(left + 126, top + 115, 218, 24).build());
        } else {
            addDrawableChild(ButtonWidget.builder(Text.literal("Obv category enabled"), b -> {}).dimensions(left + 126, top + 55, 218, 24).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("More settings can be added here"), b -> {}).dimensions(left + 126, top + 85, 218, 24).build());
        }
    }

    private Text toggle(String name, boolean enabled) {
        return Text.literal(name + "  " + (enabled ? "ON" : "OFF"));
    }

    private void rebuild() { clearAndInit(); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int left = width / 2 - 180;
        int top = height / 2 - 115;
        context.fill(left, top, left + 360, top + 230, PANEL);
        context.fill(left, top, left + 360, top + 3, ACCENT);
        context.fill(left + 8, top + 8, left + 352, top + 39, PANEL_INNER);
        context.drawText(textRenderer, "DOOM UPDATED", left + 18, top + 17, TEXT, false);
        context.drawText(textRenderer, "1.21.11  •  " + category, left + 215, top + 17, MUTED, false);
        context.fill(left + 116, top + 45, left + 118, top + 216, ACCENT);
        context.drawText(textRenderer, "MENU", left + 20, top + 40, MUTED, false);
        context.drawText(textRenderer, "SETTINGS", left + 126, top + 40, MUTED, false);
        super.render(context, mouseX, mouseY, delta);
        context.drawText(textRenderer, "Right Shift: toggle menu  •  Esc: close", left + 16, top + 214, MUTED, false);
    }

    @Override
    public boolean shouldPause() { return false; }

    private static final class DelaySlider extends SliderWidget {
        DelaySlider(int x, int y, int w, int h) {
            super(x, y, w, h, Text.empty(), HitDelay.delayMs / (double) HitDelay.MAX_MS);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal("Hit delay: " + HitDelay.delayMs + " ms"));
        }

        @Override
        protected void applyValue() {
            HitDelay.delayMs = (int) Math.round(value * HitDelay.MAX_MS / 5.0) * 5;
            HitDelay.save();
        }
    }
}
