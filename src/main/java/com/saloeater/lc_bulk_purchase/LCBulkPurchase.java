package com.saloeater.lc_bulk_purchase;

import com.mojang.logging.LogUtils;
import com.saloeater.lc_bulk_purchase.client.BulkTradeExecutor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(LCBulkPurchase.MODID)
public class LCBulkPurchase
{
    public static final String MODID = "lc_bulk_purchase";
    private static final Logger LOGGER = LogUtils.getLogger();

    public LCBulkPurchase()
    {
        var forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.register(this);

        // Register the bulk trade executor for client tick events
        forgeBus.register(BulkTradeExecutor.class);

        LOGGER.info("LC Bulk Purchase mod initialized");
    }
}
