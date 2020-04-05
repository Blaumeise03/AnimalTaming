package de.blaumeise03.animalTaming;

import de.blaumeise03.spigotUtils.AdvancedPlugin;
import de.blaumeise03.spigotUtils.Command;
import de.blaumeise03.spigotUtils.CommandHandler;
import de.blaumeise03.spigotUtils.Configuration;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnimalTaming extends AdvancedPlugin {
    public static Configuration config = null;

    public static boolean allTameAble = true;
    public static AdvancedPlugin plugin;

    @Override
    public void onEnable() {
        super.onEnable();
        plugin = this;
        saveResource("config.yml", false);
        reloadConfig();
        config = new Configuration("config.yml", this);
        config.reload();
        allTameAble = config.getBoolean("allTameable");
        getLogger().info("Players" + (allTameAble ? " can" : " can not") + " tame all animals.");
        registerEvent(new Listeners());
        CommandHandler handler = this.getHandler();
        new Command(handler, "untame", "Entzähmt das angegebene Tier.", new Permission("animalTaming.untame"), false) {
            @Override
            public void onCommand(String[] args, CommandSender sender) {
                if(args.length < 1) {
                    sender.sendMessage(ChatColor.RED + "Du musst eine UUID angeben!");
                    return;
                }
                UUID uuid;
                try{
                    uuid = UUID.fromString(args[0]);
                }catch (IllegalArgumentException e1){
                    sender.sendMessage(ChatColor.RED + "Ungültige UUID \"" + args[0] + "\"!");
                    return;
                }
                Entity e = Bukkit.getEntity(uuid);
                if(e == null){
                    sender.sendMessage(ChatColor.RED + "Entität nicht gefunden!");
                }
                if(sender instanceof Player) {

                    UUID owner = Listeners.getOwnerUUID(e);
                    if(owner == null) {
                        sender.sendMessage(ChatColor.RED + "Entität ist nicht gezähmt!");
                        return;
                    }
                    if(owner.equals(((Player) sender).getUniqueId())){
                        unTame(uuid);
                        sender.sendMessage("§aTier entzähmt!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Dieses Tier gehört dir nicht!");
                        if(sender.hasPermission("animalTaming.adminUntame")){
                            if(args.length > 1 && args[1].equalsIgnoreCase("true")){
                                unTame(uuid);
                                sender.sendMessage("Entity " + uuid.toString() + " mit Admin-Rechten entzähmt!");
                                getLogger().warning("Entity was untamed by a admin (" + sender.getName() + "): " + uuid.toString() + " Owner was: " + owner.toString());
                            }else {
                                BaseComponent base = new TextComponent("Du bist Admin, möchtest du deine Admin-Rechte einsetzten? [Dann klicke hier]");
                                base.setColor(net.md_5.bungee.api.ChatColor.GOLD);
                                base.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/untame " + uuid.toString() + " true"));
                                sender.spigot().sendMessage(base);
                            }
                        }
                    }
                }else {
                    unTame(uuid);
                    sender.sendMessage("Entity " + uuid.toString() + " entzähmt!");
                    getLogger().warning("Entity was untamed by a non-player (" + sender.getName() + "): " + uuid.toString());
                }
            }
        };
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public static boolean unTame(UUID uuid) {
        Entity entity = Bukkit.getEntity(uuid);
        if (entity == null) return false;
        List<String> removeTags = new ArrayList<>();
        for(String s : entity.getScoreboardTags()) {
            if(s.toLowerCase().startsWith("owner")){
                entity.removeScoreboardTag(s);
            }
        }
        return true;
    }
}
