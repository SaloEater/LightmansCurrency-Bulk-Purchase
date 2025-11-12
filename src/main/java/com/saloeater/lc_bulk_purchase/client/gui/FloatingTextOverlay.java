package com.saloeater.lc_bulk_purchase.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.ArrayList;
import java.util.List;

public class FloatingTextOverlay implements IGuiOverlay {
    public static final FloatingTextOverlay INSTANCE = new FloatingTextOverlay();

    private static class FloatingText {
        String text;
        int x, y;
        long startTime;
        int color;

        FloatingText(String text, int x, int y, int color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.startTime = System.currentTimeMillis();
            this.color = color;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - startTime > 2000; // 2 second lifespan
        }

        int getCurrentY() {
            long elapsed = System.currentTimeMillis() - startTime;
            // Rise at 30 pixels per second (60 pixels over 2 seconds)
            return y - (int)(elapsed * 0.03);
        }

        int getCurrentAlpha() {
            long elapsed = System.currentTimeMillis() - startTime;
            float progress = Math.min(elapsed / 2000f, 1.0f); // Clamp to 1.0
            // Fade from full alpha to 0
            return (int)(255 * (1.0f - progress));
        }
    }

    private final List<FloatingText> activeTexts = new ArrayList<>();

    /**
     * Add a new floating text at the specified screen position
     */
    public void addFloatingText(String text, int screenX, int screenY, int color) {
        synchronized (activeTexts) {
            activeTexts.add(new FloatingText(text, screenX, screenY, color));
        }
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        synchronized (activeTexts) {
            // Remove expired texts
            activeTexts.removeIf(FloatingText::isExpired);

            // Render all active floating texts
            for (FloatingText text : activeTexts) {
                int alpha = text.getCurrentAlpha();
                if (alpha <= 0) {
                    continue; // Skip fully transparent text
                }

                // Combine RGB color with alpha
                int colorWithAlpha = (text.color & 0xFFFFFF) | (alpha << 24);

                // Draw the text
                guiGraphics.drawString(
                        gui.getFont(),
                        text.text,
                        text.x,
                        text.getCurrentY(),
                        colorWithAlpha
                );
            }
        }
    }
}
