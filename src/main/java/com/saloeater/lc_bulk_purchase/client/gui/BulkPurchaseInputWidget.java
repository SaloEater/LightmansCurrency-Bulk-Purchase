package com.saloeater.lc_bulk_purchase.client.gui;

import com.saloeater.lc_bulk_purchase.client.BulkTradeExecutor;
import io.github.lightman314.lightmanscurrency.api.misc.IEasyTickable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
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
public class BulkPurchaseInputWidget implements Renderable, GuiEventListener, NarratableEntry, IEasyTickable {

    private static String lastInputValue = "1";

    private final Minecraft minecraft;
    private final int traderIndex;
    private final int tradeIndex;
    private final Runnable onClose;

    private EditBox quantityInput;
    private Button confirmButton;
    private boolean visible = true;

    public BulkPurchaseInputWidget(int traderIndex, int tradeIndex, int mouseX, int mouseY, Runnable onClose) {
        this.minecraft = Minecraft.getInstance();
        this.traderIndex = traderIndex;
        this.tradeIndex = tradeIndex;
        this.onClose = onClose;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        // Input field dimensions
        int inputWidth = 40;
        int inputHeight = 20;
        int buttonSize = 20; // Square button
        int spacing = 2; // Space between input and button

        // Total width needed
        int totalWidth = inputWidth + spacing + buttonSize;

        // Position below mouse cursor, with bounds checking
        int inputX = Math.max(10, Math.min(mouseX, screenWidth - totalWidth - 10));
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

        // Create confirm button (checkmark)
        confirmButton = Button.builder(
                Component.literal("✓"),
                button -> confirmPurchase()
        ).bounds(inputX + inputWidth + spacing, inputY, buttonSize, buttonSize).build();
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

    public void tick() {
        if (visible) {
            if (quantityInput != null) {
                quantityInput.tick();
            }
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

        // Render the confirm button
        confirmButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!visible) return false;

        // Handle Enter key - confirm purchase
        if (keyCode == 257) { // Enter key
            confirmPurchase();
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

        // Check if click is on the confirm button
        if (confirmButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Check if click is inside the input field
        boolean clickedInside = quantityInput.mouseClicked(mouseX, mouseY, button);

        // If clicked outside both the input field and button, close the widget
        if (!clickedInside) {
            close();
            return true;
        }

        return clickedInside;
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
                BulkTradeExecutor.execute(traderIndex, tradeIndex, quantity);
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
