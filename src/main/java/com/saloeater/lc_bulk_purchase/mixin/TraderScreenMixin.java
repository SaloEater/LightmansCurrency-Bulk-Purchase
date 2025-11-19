package com.saloeater.lc_bulk_purchase.mixin;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to add mouseDragged event forwarding to TraderScreen.
 * Without this, slider dragging doesn't work because TraderScreen doesn't forward drag events to children.
 */
@Mixin(value = TraderScreen.class, remap = false)
public abstract class TraderScreenMixin extends AbstractContainerScreen<TraderMenu> {

    public TraderScreenMixin(AbstractContainerMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super((TraderMenu) p_97741_, p_97742_, p_97743_);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        TraderScreen screen = (TraderScreen) (Object) this;

        // Forward drag events to the focused child (if any)
        GuiEventListener focused = screen.getFocused();
        if (focused != null && screen.isDragging() && button == 0) {
            return focused.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }

        return false;
    }
}
