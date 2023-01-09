package ru.mcfine.mcfinehudpaper;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClearCompass implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length!=1){
            sender.sendMessage("Wrong amount of args");
            return true;
        }
        Player p = Bukkit.getPlayerExact(args[0]);
        if(p==null){
            sender.sendMessage("No such player: "+args[0]);
            return true;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeBoolean(true);
        p.sendPluginMessage(McfineHUDPaper.plugin, "mcfinehud:cancelcompass", out.toByteArray());
        return true;
    }
}
