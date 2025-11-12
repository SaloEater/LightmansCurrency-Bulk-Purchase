package com.saloeater.lc_bulk_purchase.client.gui;

import com.saloeater.lc_bulk_purchase.client.BulkTradeExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

/**
 * A popup widget overlay for entering bulk purchase quantity
 * This doesn't close the trader screen
 */
public class BulkPurchaseInputWidget implements Renderable, GuiEventListener, NarratableEntry {

    private static String lastInputValue = "1";

    private final Minecraft minecraft;
    private final int traderIndex;
    private final int tradeIndex;
    private final int mouseX;
    private final int mouseY;
    private final Runnable onClose;

    private EditBox quantityInput;
    private boolean visible = true;

    public BulkPurchaseInputWidget(int traderIndex, int tradeIndex, int mouseX, int mouseY, Runnable onClose) {
        this.minecraft = Minecraft.getInstance();
        this.traderIndex = traderIndex;
        this.tradeIndex = tradeIndex;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.onClose = onClose;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        // Input field dimensions
        int inputWidth = 80;
        int inputHeight = 20;

        // Position below mouse cursor, with bounds checking
        int inputX = Math.max(10, Math.min(mouseX, screenWidth - inputWidth - 10));
        int inputY = Math.max(10, Math.min(mouseY + 5, screenHeight - inputHeight - 10));

        // Create the input field
        quantityInput = new EditBox(
                minecraft.font,
                inputX,
                inputY,
                inputWidth,
                inputHeight,
                Component.literal("Quantity")
        );

        // Configure the input field
        quantityInput.setValue(lastInputValue);
        quantityInput.setMaxLength(9);
        quantityInput.setFilter(this::isValidInput);
        quantityInput.setBordered(true);
        quantityInput.setFocused(true);
    }

    private boolean isValidInput(String input) {
        if (input.isEmpty()) {
            return true;
        }
        try {
            int value = Integer.parseInt(input);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) return;

        // Draw semi-transparent overlay
        guiGraphics.fill(0, 0, minecraft.getWindow().getGuiScaledWidth(),
                        minecraft.getWindow().getGuiScaledHeight(), 0x80000000);

        // Render the input field
        quantityInput.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!visible) return false;

        // Handle Enter key - confirm purchase
        if (keyCode == 257) { // Enter key
            confirmPurchase();
            return true;
        }

        // Handle Escape key - close
        if (keyCode == 256) { // Escape key
            close();
            return true;
        }

        // Forward to input field
        return quantityInput.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!visible) return false;
        return quantityInput.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;
        return quantityInput.mouseClicked(mouseX, mouseY, button);
    }

    private void confirmPurchase() {
        String input = quantityInput.getValue().trim();

        if (input.isEmpty()) {
            close();
            return;
        }

        try {
            int quantity = Integer.parseInt(input);
            if (quantity > 0) {
                lastInputValue = input;
                BulkTradeExecutor.execute(traderIndex, tradeIndex, quantity, mouseX, mouseY);
                close();
            }
        } catch (NumberFormatException e) {
            close();
        }
    }

    private void close() {
        visible = false;
        onClose.run();
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setFocused(boolean focused) {
        quantityInput.setFocused(focused);
    }

    @Override
    public boolean isFocused() {
        return quantityInput.isFocused();
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.FOCUSED;
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
        quantityInput.updateNarration(output);
    }
}
