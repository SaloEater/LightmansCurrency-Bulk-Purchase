package com.saloeater.lc_bulk_purchase.client;

import com.saloeater.lc_bulk_purchase.LCBulkPurchase;
import com.saloeater.lc_bulk_purchase.client.gui.FloatingTextOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side mod event handler for registering GUI overlays
 */
@Mod.EventBusSubscriber(modid = LCBulkPurchase.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("bulk_purchase_floating_text", FloatingTextOverlay.INSTANCE);
    }
}
