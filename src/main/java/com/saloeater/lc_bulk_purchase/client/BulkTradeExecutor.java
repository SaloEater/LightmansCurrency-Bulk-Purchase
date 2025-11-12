package com.saloeater.lc_bulk_purchase.client;

import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketExecuteTrade;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BulkTradeExecutor {

    private static class ExecutionState {
        final int traderIndex;
        final int tradeIndex;
        int remaining;
        long lastExecutionTime;

        ExecutionState(int traderIndex, int tradeIndex, int quantity) {
            this.traderIndex = traderIndex;
            this.tradeIndex = tradeIndex;
            this.remaining = quantity;
            this.lastExecutionTime = System.currentTimeMillis();
        }

        boolean shouldExecute() {
            return remaining > 0 && (System.currentTimeMillis() - lastExecutionTime) > 10;
        }

        void executeOne() {
            // Send trade packet
            new CPacketExecuteTrade(traderIndex, tradeIndex).send();

            // Update state
            remaining--;
            lastExecutionTime = System.currentTimeMillis();
        }

        boolean isComplete() {
            return remaining <= 0;
        }
    }

    private static ExecutionState currentExecution = null;

    /**
     * Start a bulk purchase execution
     */
    public static void execute(int traderIndex, int tradeIndex, int quantity) {
        if (quantity <= 0) {
            return;
        }
        currentExecution = new ExecutionState(traderIndex, tradeIndex, quantity);
    }

    /**
     * Stop the current execution (if any)
     */
    public static void stop() {
        currentExecution = null;
    }

    /**
     * Check if there's an active execution
     */
    public static boolean isExecuting() {
        return currentExecution != null;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // Only process on the end of the tick phase
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        // Check if there's an active execution
        if (currentExecution == null) {
            return;
        }

        // Check if we're still in the trader menu
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) {
            // Menu was closed, stop execution
            stop();
            return;
        }

        // Check if it's time to execute the next trade
        if (currentExecution.shouldExecute()) {
            currentExecution.executeOne();

            // Check if execution is complete
            if (currentExecution.isComplete()) {
                stop();
            }
        }
    }
}
