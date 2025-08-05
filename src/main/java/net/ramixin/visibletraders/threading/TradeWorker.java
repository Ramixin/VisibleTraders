package net.ramixin.visibletraders.threading;

import net.ramixin.visibletraders.VisibleTraders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TradeWorker implements Runnable {

    private final List<FutureMerchantOffer> pendingOrders = Collections.synchronizedList(new ArrayList<>());
    private boolean running = true;
    private final Object lock = new Object();

    @Override
    public void run() {
        VisibleTraders.LOGGER.info("Starting trade worker");
        while(running) {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            while(!pendingOrders.isEmpty()) {
                FutureMerchantOffer order = pendingOrders.getFirst();
                order.fulfillFuture();
                pendingOrders.remove(order);
            }
        }
        VisibleTraders.LOGGER.info("Thread closed");
    }

    public synchronized void addOrder(FutureMerchantOffer order) {
        pendingOrders.add(order);
        synchronized (lock) {
            lock.notify();
        }
    }

    public void stop() {
        VisibleTraders.LOGGER.info("Stopping trade worker");
        running = false;
        synchronized (lock) {
            lock.notify();
        }
    }

    public void reset() {
        pendingOrders.clear();
        running = true;
    }

    public synchronized boolean isRunning() {
        return running;
    }

}
