package ru.mcfine.mcfinehudpaper;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetCompass implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //arg0 - player
        //arg1 - world_name
        //arg2 - x
        //arg3 - y
        //arg4 - z
        //arg5 - radius margin
        //arg6+ - message if no mod

        if(args.length<5){
            sender.sendMessage("Not enough args.");
            return true;
        }

        Player p = Bukkit.getPlayerExact(args[0]);
        if(p == null){
            sender.sendMessage("Player not found: "+args[0]);
            return true;
        }

        World world = Bukkit.getWorld(args[1]);
        if(world == null){
            sender.sendMessage("World not found: "+args[1]);
            return true;
        }

        double x;
        double y;
        double z;
        double radius;
        try {
            x = Double.parseDouble(args[2]);
            y = Double.parseDouble(args[3]);
            z = Double.parseDouble(args[4]);
        } catch (Exception ex){
            sender.sendMessage("Failed to parse coordinates! "+args[2]+" | "+args[3]+" | "+args[4]);
            return true;
        }

        try {
            radius = Double.parseDouble(args[5]);
        } catch (Exception ig){
            radius=1.0;
        }

        if(McfineHUDPaper.players.contains(p)){
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeDouble(x);
            out.writeDouble(y);
            out.writeDouble(z);
            out.writeDouble(radius);
            out.writeChars(world.getName());
            p.sendPluginMessage(McfineHUDPaper.plugin, "mcfinehud:compass", out.toByteArray());
            Component component = Component.text(" \n ✖ ", NamedTextColor.DARK_GREEN).append(Component.text("На компасе теперь указано направление. \n ", NamedTextColor.GOLD));
            p.sendMessage(component);
        } else{
            Component component = Component.text(" \n Идите на координаты ", NamedTextColor.GOLD).append(
                    Component.text("X: ", NamedTextColor.GRAY).append(Component.text(""+Math.round(x)+"  ", NamedTextColor.YELLOW))
            ).append(
                    Component.text("Y: ", NamedTextColor.GRAY).append(Component.text(""+Math.round(y)+"  ", NamedTextColor.YELLOW))
            ).append(
                    Component.text("Z: ", NamedTextColor.GRAY).append(Component.text(""+Math.round(z)+"  ", NamedTextColor.YELLOW))
            ).append(
                    Component.text(" \n",NamedTextColor.GOLD).append(Component.text(" Рекомендуем скачать наш клиент с кастомным HUD. \n ", NamedTextColor.GRAY))
            );
            p.sendMessage(component);
        }

        return true;
    }
}
