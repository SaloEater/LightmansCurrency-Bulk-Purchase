package com.saloeater.lc_bulk_purchase.client.gui;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ForgeSlider;

/**
 * A slider widget for selecting quantity values between a min and max range.
 * Syncs with an EditBox input field.
 * Uses ForgeSlider for better integer support and built-in min/max handling.
 */
public class QuantitySlider extends ForgeSlider {

    private final EditBox linkedInput;

    public QuantitySlider(int x, int y, int width, int height,
                          int minValue, int maxValue,
                          int initialValue, EditBox linkedInput) {
        super(x, y, width, height,
              Component.empty(),     // prefix
              Component.empty(),     // suffix
              minValue,              // minValue
              maxValue,              // maxValue
              initialValue,          // currentValue
              true);                 // drawString (show value on slider)
        this.linkedInput = linkedInput;
    }

    @Override
    protected void applyValue() {
        // Sync with EditBox when slider is dragged
        if (linkedInput != null) {
            linkedInput.setValue(String.valueOf(getValueInt()));
        }
    }
}
