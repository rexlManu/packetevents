/*
 * MIT License
 *
 * Copyright (c) 2020 retrooper
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.retrooper.packetevents.handler;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.event.impl.PacketReceiveEvent;
import io.github.retrooper.packetevents.event.impl.PacketSendEvent;
import io.github.retrooper.packetevents.event.impl.PlayerInjectEvent;
import io.github.retrooper.packetevents.event.impl.PlayerUninjectEvent;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NettyPacketHandler {
    private static boolean v1_7_nettyMode = false;
    private static final ExecutorService cachedThreadPoolExecutor = Executors.newCachedThreadPool();
    private static final ExecutorService singleThreadedExecutor = Executors.newSingleThreadExecutor();

    static {
        try {
            Class.forName("io.netty.channel.Channel");
        } catch (ClassNotFoundException e) {
            v1_7_nettyMode = true;
        }
    }

    /**
     * Synchronously inject a player
     * @param player Target player to inject
     */
    public static void injectPlayer(final Player player) {
        try {
            final PlayerInjectEvent injectEvent = new PlayerInjectEvent(player);
            PacketEvents.getAPI().getEventManager().callEvent(injectEvent);
            if (!injectEvent.isCancelled()) {
                if (v1_7_nettyMode) {
                    NettyPacketHandler_7.injectPlayer(player);
                } else {
                    NettyPacketHandler_8.injectPlayer(player);
                }
            }
        } catch (Exception ignored) {

        }
    }

    /**
     * Asynchronously inject a player
     * @param player
     * @return {@link java.util.concurrent.Future}
     */
    public static Future<?> injectPlayerAsync(final Player player) {
        return cachedThreadPoolExecutor.submit(() -> {
            try {
                final PlayerInjectEvent injectEvent = new PlayerInjectEvent(player, true);
                PacketEvents.getAPI().getEventManager().callEvent(injectEvent);
                if (!injectEvent.isCancelled()) {
                    if (v1_7_nettyMode) {
                        NettyPacketHandler_7.injectPlayer(player);
                    } else {
                        NettyPacketHandler_8.injectPlayer(player);
                    }
                }
            } catch (Exception ignored) {

            }
        });
    }

    /**
     * Synchronously eject a player.
     * @param player
     */
    public static void ejectPlayer(final Player player) {
        try {
            final PlayerUninjectEvent uninjectEvent = new PlayerUninjectEvent(player);
            PacketEvents.getAPI().getEventManager().callEvent(uninjectEvent);
            if (!uninjectEvent.isCancelled()) {
                if (v1_7_nettyMode) {
                    NettyPacketHandler_7.ejectPlayer(player);
                } else {
                    NettyPacketHandler_8.ejectPlayer(player);
                }
            }
        } catch (Exception ignored) {

        }
    }

    /**
     * Asynchronously eject a player
     * @param player
     * @return {@link java.util.concurrent.Future}
     */
    public static Future<?> ejectPlayerAsync(final Player player) {
        return cachedThreadPoolExecutor.submit(() -> {
            try {
                final PlayerUninjectEvent uninjectEvent = new PlayerUninjectEvent(player, true);
                PacketEvents.getAPI().getEventManager().callEvent(uninjectEvent);
                if (!uninjectEvent.isCancelled()) {
                    if (v1_7_nettyMode) {
                        NettyPacketHandler_7.ejectPlayer(player);
                    } else {
                        NettyPacketHandler_8.ejectPlayer(player);
                    }
                }
            } catch (Exception ignored) {

            }
        });
    }

    /**
     * This function is called each time the server plans to send a packet to the client.
     * @param sender
     * @param packet
     * @return
     */
    public static Object write(final Player sender, final Object packet) {
        final Object[] returningPacket = new Object[1];
        singleThreadedExecutor.submit(new Runnable() {
            @Override
            public void run() {
                final PacketSendEvent packetSendEvent = new PacketSendEvent(sender, packet);
                PacketEvents.getAPI().getEventManager().callEvent(packetSendEvent);
                if (!packetSendEvent.isCancelled()) {
                    returningPacket[0] = packet;
                }
                returningPacket[0] = null;
            }
        });
        return returningPacket[0];
    }

    /**
     * This function is called each time the server receives a packet from the client.
     * @param receiver
     * @param packet
     * @return
     */
    public static Object read(final Player receiver, final Object packet) {
        final Object[] returningPacket = new Object[1];
       singleThreadedExecutor.submit(new Runnable() {
           @Override
           public void run() {
               final PacketReceiveEvent packetReceiveEvent = new PacketReceiveEvent(receiver, packet);
               PacketEvents.getAPI().getEventManager().callEvent(packetReceiveEvent);
               if (!packetReceiveEvent.isCancelled()) {
                   returningPacket[0] = packet;
               }
               returningPacket[0] = null;
           }
       });
       return returningPacket[0];
    }
}