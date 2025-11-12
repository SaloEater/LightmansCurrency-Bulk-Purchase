package com.saloeater.lc_bulk_purchase.client.gui;

import com.saloeater.lc_bulk_purchase.client.BulkTradeExecutor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class BulkPurchaseInputScreen extends Screen {

    private static String lastInputValue = "1"; // Remember last entered value

    private final int traderIndex;
    private final int tradeIndex;
    private final int mouseX;
    private final int mouseY;

    private EditBox quantityInput;

    public BulkPurchaseInputScreen(int traderIndex, int tradeIndex, int mouseX, int mouseY) {
        super(Component.literal("Bulk Purchase"));
        this.traderIndex = traderIndex;
        this.tradeIndex = tradeIndex;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    @Override
    protected void init() {
        super.init();

        // Input field dimensions
        int inputWidth = 80;
        int inputHeight = 20;

        // Position below mouse cursor, with bounds checking
        int inputX = Math.max(10, Math.min(mouseX, this.width - inputWidth - 10));
        int inputY = Math.max(10, Math.min(mouseY + 5, this.height - inputHeight - 10));

        // Create the input field
        quantityInput = new EditBox(
                this.font,
                inputX,
                inputY,
                inputWidth,
                inputHeight,
                Component.literal("Quantity")
        );

        // Configure the input field
        quantityInput.setValue(lastInputValue);
        quantityInput.setMaxLength(9); // Max ~999,999,999
        quantityInput.setFilter(this::isValidInput); // Only allow positive integers
        quantityInput.setBordered(true);

        // Add to screen
        this.addRenderableWidget(quantityInput);

        // Set focus to input field
        this.setInitialFocus(quantityInput);
    }

    /**
     * Filter to only allow positive integers
     */
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Handle Enter key - start bulk purchase
        if (keyCode == 257) { // Enter key
            confirmPurchase();
            return true;
        }

        // Handle Escape key - close screen
        if (keyCode == 256) { // Escape key
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void confirmPurchase() {
        String input = quantityInput.getValue().trim();

        if (input.isEmpty()) {
            this.onClose();
            return;
        }

        try {
            int quantity = Integer.parseInt(input);
            if (quantity > 0) {
                // Remember this value for next time
                lastInputValue = input;

                // Start bulk execution
                BulkTradeExecutor.execute(traderIndex, tradeIndex, quantity, mouseX, mouseY);

                // Close this screen and return to trader screen
                this.onClose();
            }
        } catch (NumberFormatException e) {
            // Invalid input, just close
            this.onClose();
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Draw a semi-transparent background
        guiGraphics.fill(0, 0, this.width, this.height, 0x80000000);

        // Render widgets (including the input field)
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Don't pause the game
    }
}
