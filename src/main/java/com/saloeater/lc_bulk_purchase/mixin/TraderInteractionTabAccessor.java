package com.saloeater.lc_bulk_purchase.mixin;

import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.ITraderMenu;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.TraderClientTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TraderClientTab.class, remap = false)
public interface TraderInteractionTabAccessor {

    @Accessor("menu")
    ITraderMenu getMenu();
}
