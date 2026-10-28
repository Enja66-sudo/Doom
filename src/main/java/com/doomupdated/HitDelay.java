package com.doomupdated;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Properties;

/** Verzoegert deine eigenen Schlaege auf Entities um einstellbare Millisekunden. */
public final class HitDelay {
    public static final int MAX_MS = 500;

    public static boolean enabled = false;
    public static int delayMs = 100;

    private static boolean replaying = false;
    private record Pending(Entity target, long due) {}
    private static final ArrayDeque<Pending> QUEUE = new ArrayDeque<>();

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("doomupdated.properties");
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        try (InputStream in = Files.newInputStream(f)) {
            Properties p = new Properties();
            p.load(in);
            delayMs = Math.max(0, Math.min(MAX_MS, Integer.parseInt(p.getProperty("hitDelayMs", "100"))));
            enabled = Boolean.parseBoolean(p.getProperty("hitDelayEnabled", "false"));
        } catch (IOException | NumberFormatException ignored) {}
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("hitDelayMs", Integer.toString(delayMs));
        p.setProperty("hitDelayEnabled", Boolean.toString(enabled));
        try (OutputStream out = Files.newOutputStream(file())) {
            p.store(out, "Doom Updated");
        } catch (IOException ignored) {}
    }

    public static void toggle(MinecraftClient client) {
        enabled = !enabled;
        QUEUE.clear();
        save();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("Hit Delay: " + (enabled ? "ON (" + delayMs + " ms)" : "OFF")), true);
        }
    }

    public static void init() {
        load();
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (!enabled || delayMs <= 0 || replaying || !world.isClient()) return ActionResult.PASS;
            if (QUEUE.size() < 4) QUEUE.add(new Pending(entity, System.currentTimeMillis() + delayMs));
            return ActionResult.FAIL;
        });
        ClientTickEvents.END_CLIENT_TICK.register(HitDelay::tick);
    }

    private static void tick(MinecraftClient c) {
        if (c.player == null || c.world == null || c.interactionManager == null) {
            QUEUE.clear();
            return;
        }
        long now = System.currentTimeMillis();
        while (!QUEUE.isEmpty() && QUEUE.peek().due() <= now) {
            Entity t = QUEUE.poll().target();
            if (t.isRemoved() || !t.isAlive() || c.player.squaredDistanceTo(t) > 36.0) continue;
            replaying = true;
            try {
                c.interactionManager.attackEntity(c.player, t);
            } finally {
                replaying = false;
            }
        }
    }
}
