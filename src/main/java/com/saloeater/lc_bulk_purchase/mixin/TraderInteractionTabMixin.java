package com.saloeater.lc_bulk_purchase.mixin;

import com.saloeater.lc_bulk_purchase.client.gui.BulkPurchaseInputWidget;
import io.github.lightman314.lightmanscurrency.api.traders.ITraderSource;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.ITraderMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.ITraderScreen;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.common.TraderInteractionTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.saloeater.lc_bulk_purchase.client.BulkTradeExecutor.*;

@Mixin(value = TraderInteractionTab.class, remap = false)
public class TraderInteractionTabMixin {

    @Unique
    private BulkPurchaseInputWidget lc_bulk_purchase$currentInputWidget = null;

    @Inject(method = "OnButtonPress", at = @At("HEAD"), cancellable = true, remap = false)
    private void onButtonPress(TraderData trader, TradeData trade, CallbackInfo ci) {
        // Only intercept if Shift is held
        if (!Screen.hasShiftDown()) {
            return; // Let original method execute
        }

        // Cancel original execution
        ci.cancel();

        // Don't show input if a bulk purchase is already in progress
        if (isExecuting()) {
            return;
        }

        // Null checks
        if (trader == null || trade == null) {
            return;
        }

        // Get menu via accessor
        ITraderMenu menu = ((TraderInteractionTabAccessor) this).getMenu();
        if (menu == null) {
            return;
        }

        // Get trader source
        ITraderSource ts = menu.getTraderSource();
        if (ts == null) {
            menu.getPlayer().closeContainer();
            return;
        }

        // Find trader index
        List<TraderData> traders = ts.getTraders();
        int traderIndex = traders.indexOf(trader);
        if (traderIndex < 0) {
            return;
        }

        // Find trade index
        TraderData t = traders.get(traderIndex);
        if (t == null) {
            return;
        }

        int tradeIndex = t.getTradeData().indexOf(trade);
        if (tradeIndex < 0) {
            return;
        }

        // Get mouse position
        Minecraft mc = Minecraft.getInstance();
        double mouseX = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
        double mouseY = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();

        // Get the screen
        ITraderScreen screen = ((TraderInteractionTabAccessor) this).getScreen();
        if (!(screen instanceof TraderScreen)) {
            return;
        }

        TraderScreen traderScreen = (TraderScreen) screen;

        // Remove old widget if exists
        if (lc_bulk_purchase$currentInputWidget != null) {
            traderScreen.removeChild(lc_bulk_purchase$currentInputWidget);
        }

        // Create and add new input widget
        lc_bulk_purchase$currentInputWidget = new BulkPurchaseInputWidget(
                traderIndex,
                tradeIndex,
                trader,
                trade,
                (int) mouseX,
                (int) mouseY,
                () -> {
                    // On close, remove the widget
                    if (lc_bulk_purchase$currentInputWidget != null) {
                        traderScreen.removeChild(lc_bulk_purchase$currentInputWidget);
                        lc_bulk_purchase$currentInputWidget = null;
                    }
                }
        );

        traderScreen.addChild(lc_bulk_purchase$currentInputWidget);

        // CRITICAL: Set the widget as focused so it receives mouseDragged events
        traderScreen.setFocused(lc_bulk_purchase$currentInputWidget);
    }
}
