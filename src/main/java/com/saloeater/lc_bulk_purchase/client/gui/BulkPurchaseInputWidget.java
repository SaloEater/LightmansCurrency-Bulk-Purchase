package com.saloeater.lc_bulk_purchase.client.gui;

import com.saloeater.lc_bulk_purchase.client.BulkTradeExecutor;
import io.github.lightman314.lightmanscurrency.api.misc.IEasyTickable;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
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
    private final int maxStock;
    private final Runnable onClose;

    private EditBox quantityInput;
    private QuantitySlider quantitySlider;
    private Button confirmButton;
    private boolean visible = true;

    public BulkPurchaseInputWidget(int traderIndex, int tradeIndex, TraderData trader, TradeData trade, int mouseX, int mouseY, Runnable onClose) {
        this.minecraft = Minecraft.getInstance();
        this.traderIndex = traderIndex;
        this.tradeIndex = tradeIndex;
        this.onClose = onClose;

        // Get available stock from the trade
        TradeContext context = TradeContext.createStorageMode(trader);
        this.maxStock = trade.getStock(context);

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        // Widget dimensions
        int inputWidth = 60;  // Increased from 40
        int inputHeight = 20;
        int buttonSize = 20;
        int sliderWidth = 80;
        int sliderHeight = 20;
        int spacing = 2;
        int verticalSpacing = 5;

        // Calculate total dimensions
        int totalWidth = Math.max(inputWidth + spacing + buttonSize, sliderWidth);
        int totalHeight = inputHeight + verticalSpacing + sliderHeight;

        // Position below mouse cursor, with bounds checking
        int inputX = Math.max(10, Math.min(mouseX, screenWidth - totalWidth - 10));
        int inputY = Math.max(10, Math.min(mouseY + 5, screenHeight - totalHeight - 10));

        // Set initial value to last value, but cap it to max stock
        int lastValue = 1;
        try {
            lastValue = Integer.parseInt(lastInputValue);
        } catch (NumberFormatException e) {
            // Use default value of 1
        }
        int initialValue = Math.min(lastValue, maxStock);

        // Create the input field
        quantityInput = new EditBox(
                minecraft.font,
                inputX,
                inputY,
                inputWidth,
                inputHeight,
                Component.literal("Quantity (Max: " + maxStock + ")")
        );

        // Configure the input field
        quantityInput.setValue(String.valueOf(initialValue));
        quantityInput.setMaxLength(9);
        quantityInput.setFilter(this::isValidInput);
        quantityInput.setBordered(true);

        // Create the slider
        quantitySlider = new QuantitySlider(
                inputX,
                inputY + inputHeight + verticalSpacing,
                sliderWidth,
                sliderHeight,
                1,              // minValue
                maxStock,       // maxValue
                initialValue,
                quantityInput
        );

        // Add responder to input field for input→slider sync
        quantityInput.setResponder(text -> {
            if (!text.isEmpty()) {
                try {
                    int value = Integer.parseInt(text);
                    value = Math.max(1, Math.min(value, maxStock));
                    quantitySlider.setValue(value); // ForgeSlider uses setValue(double)
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            }
        });

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
            return value > 0; // Allow any positive number
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void tick() {
        if (visible) {
            if (quantityInput != null) {
                quantityInput.tick();

                // Automatically clamp input value to max stock
                String currentValue = quantityInput.getValue();
                if (!currentValue.isEmpty()) {
                    try {
                        int value = Integer.parseInt(currentValue);
                        if (value > maxStock) {
                            quantityInput.setValue(String.valueOf(maxStock));
                            quantitySlider.setValue(maxStock); // ForgeSlider uses setValue(double)
                        }
                    } catch (NumberFormatException e) {
                        // Ignore invalid input
                    }
                }
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

        // Render the slider
        quantitySlider.render(guiGraphics, mouseX, mouseY, partialTick);

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

        // Check if click is on the slider
        boolean clickedSlider = quantitySlider.mouseClicked(mouseX, mouseY, button);
        if (clickedSlider) {
            return true;
        }

        // Check if click is inside the input field
        boolean clickedInput = quantityInput.mouseClicked(mouseX, mouseY, button);

        // If clicked outside all widgets, close the widget
        if (!clickedInput && !clickedSlider) {
            close();
            return true;
        }

        return clickedInput;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!visible) return false;
        // Forward drag events to slider for smooth dragging
        if (quantitySlider.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        // Also forward to input field in case it's being used for text selection
        return quantityInput.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!visible) return false;
        // Forward release events to slider
        if (quantitySlider.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        // Also forward to input field
        return quantityInput.mouseReleased(mouseX, mouseY, button);
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
