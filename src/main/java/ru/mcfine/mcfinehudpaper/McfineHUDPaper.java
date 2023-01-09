package ru.mcfine.mcfinehudpaper;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.papermc.paper.event.block.TargetHitEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;

public final class McfineHUDPaper extends JavaPlugin implements Listener, @NotNull PluginMessageListener {

    public static JavaPlugin plugin = null;
    private static Economy econ = null;
    public static HashSet<Player> players = new HashSet<>();
    private static final HashMap<Player, BukkitTask> playerTasks = new HashMap<>();
    private static long refreshRate = 10L;
    private static boolean pluginEnable = true;

    @Override
    public void onEnable() {
        plugin = this;
        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        try {
            refreshRate = getConfig().getLong("refresh-rate");
            pluginEnable = getConfig().getBoolean("plugin-enable");
        } catch (Exception ignored){}

        if(!pluginEnable){
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "mcfinehud:init");
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "mcfinehud:balance");
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "mcfinehud:target");
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "mcfinehud:compass");
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "mcfinehud:cancelcompass");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "mcfinehud:init", this);

        getServer().getPluginManager().registerEvents(this, this);

        getCommand("setcompass").setExecutor(new SetCompass());
        getCommand("clearcompass").setExecutor(new ClearCompass());

        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player p : players){
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeDouble(econ.getBalance(p.getName()));
                    out.writeChars(p.getWorld().getName());
                    p.sendPluginMessage(plugin, "mcfinehud:balance", out.toByteArray());
                }
            }
        }.runTaskTimerAsynchronously(this, 20L, refreshRate);

    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "mcfinehud:init");
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "mcfinehud:balance");
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "mcfinehud:arrow");
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "mcfinehud:target");
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "mcfinehud:compass");
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "mcfinehud:cancelcompass");
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "mcfinehud:init", this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent ev){
        Player p = ev.getPlayer();

        if(p.hasPermission("mcfinehud.disable")) return;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeBoolean(true);
        final int[] counter = {5};

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if(counter[0] == 0){
                    this.cancel();
                    playerTasks.remove(p);
                    return;
                }
                p.sendPluginMessage(plugin, "mcfinehud:init", out.toByteArray());
                counter[0]--;
            }
        }.runTaskTimer(this, 100L, 100L);

        playerTasks.put(p, task);

    }

    @EventHandler
    public void onPlayerShootTarget(TargetHitEvent ev){
        if(ev.getEntity().getShooter() instanceof Player p){
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeInt(ev.getSignalStrength());
            p.sendPluginMessage(this, "mcfinehud:target", out.toByteArray());
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent ev){
        players.remove(ev.getPlayer());
        if(playerTasks.get(ev.getPlayer()) != null && !playerTasks.get(ev.getPlayer()).isCancelled())playerTasks.get(ev.getPlayer()).cancel();
        playerTasks.remove(ev.getPlayer());
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if(!channel.equals("mcfinehud:init"))return;
        players.add(player);
        if(playerTasks.get(player) != null && !playerTasks.get(player).isCancelled())playerTasks.get(player).cancel();
        playerTasks.remove(player);
    }
}
