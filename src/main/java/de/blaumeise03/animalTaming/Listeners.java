package de.blaumeise03.animalTaming;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Listeners implements Listener {

    private Map<Player, Long> tameDelay = new HashMap<>();

    @EventHandler
    public void onTame(EntityTameEvent e) {
        AnimalTamer tamer = e.getOwner();
        if(!(tamer instanceof Player))
            return;
        LivingEntity entity = e.getEntity();
        entity.addScoreboardTag("Owner-" + tamer.getUniqueId().toString() + "-");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        Entity entity = e.getEntity();
        if(entity instanceof Player)
            return;
        Entity damager = e.getDamager();
        Player p;
        if(damager instanceof Player)
            p = (Player) damager;
        else return;
        UUID id = getOwnerUUID(entity);
        if(id != null && !id.equals(p.getUniqueId())){
            e.setCancelled(true);
            p.sendTitle("", "§cDieses Tier gehört dir nicht!", 5, 30, 10);
            p.sendMessage("§cDu darfst diesem Tier nicht schaden!");
        }

    }


    @EventHandler
    public void onClick(PlayerInteractEntityEvent e) {
        if(!AnimalTaming.allTameAble)
            return;
        Player p = e.getPlayer();
        Entity entity = e.getRightClicked();
        UUID owner = getOwnerUUID(entity);
        if(owner != null && !owner.equals(p.getUniqueId())) {
            if(!p.hasPermission("animalTaming.adminUntame")) {
                p.sendTitle("", "§cDieses Tier gehört dir nicht!", 5, 30, 10);
                e.setCancelled(true);
                return;
            }else {
                if(!tameDelay.containsKey(p) || (System.currentTimeMillis() - tameDelay.get(p)) > 1000)
                    p.sendMessage(ChatColor.YELLOW + "Dieses Tier gehört dir nicht, du hast aber Admin-Berechtigungen!");
            }
        }
        //if(entity instanceof Tameable)
        //    return;
        if(!(entity instanceof Animals))
            return;
        if(tameDelay.containsKey(p)){
            if((System.currentTimeMillis() - tameDelay.get(p)) < 1000){
                return;
            }else tameDelay.remove(p);
        }
        Set<String> tags = entity.getScoreboardTags();

        ItemStack stack = p.getInventory().getItemInMainHand();
        if(stack.getType() == Material.GOLDEN_CARROT){
            for(String s : tags) {
                if(s.toLowerCase().startsWith("owner")){
                    p.sendTitle("", "§cDieses Tier ist bereits gezähmt!", 5, 30, 10);
                    return;
                }
            }
            if(stack.getAmount() <= 1) p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            else {
                stack.setAmount(stack.getAmount() - 1);
                p.getInventory().setItemInMainHand(stack);
            }
            entity.addScoreboardTag("Owner-" + p.getUniqueId().toString() + "-");
            p.sendTitle("", "§aDu hast das Tier gezähmt!", 5, 30, 10);
            tameDelay.put(p, System.currentTimeMillis());
        }
        if(stack.getType() == Material.REDSTONE) {
            if(owner != null && owner.equals(p.getUniqueId()) || p.hasPermission("animalTaming.adminUntame")) {
                BaseComponent base = new TextComponent("Klicke hier um das Tier zu entzähmen!");
                base.setColor(ChatColor.DARK_GREEN);
                base.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/untame " + entity.getUniqueId().toString()));
                p.spigot().sendMessage(base);
                tameDelay.put(p, System.currentTimeMillis());
            }
        }
    }

    @EventHandler
    public void onMount(EntityMountEvent e) {
        Entity mount = e.getMount();
        Entity entity = e.getEntity();
        if(!(entity instanceof Player))
            return;
        Player p = (Player) entity;
        UUID id = getOwnerUUID(mount);
        if(id != null && !id.equals(p.getUniqueId())){
            e.setCancelled(true);
            p.sendTitle("", "§cDieses Tier gehört dir nicht!", 5, 30, 10);
            //p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§cDieses Tier gehört dir nicht!"));
        }
        if(tameDelay.containsKey(p)){
            if((System.currentTimeMillis() - tameDelay.get(p)) < 1000){
                return;
            }else tameDelay.remove(p);
        }
        ItemStack stack = p.getInventory().getItemInMainHand();
        Set<String> tags = entity.getScoreboardTags();
        if(stack.getType() == Material.GOLDEN_CARROT){
            for(String s : tags) {
                if(s.toLowerCase().startsWith("owner")){
                    p.sendTitle("", "§cDieses Tier ist bereits gezähmt!", 5, 30, 10);
                    return;
                }
            }
            if(stack.getAmount() <= 1) p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            else {
                stack.setAmount(stack.getAmount() - 1);
                p.getInventory().setItemInMainHand(stack);
            }
            entity.addScoreboardTag("Owner-" + p.getUniqueId().toString() + "-");
            p.sendTitle("", "§aDu hast das Tier gezähmt!", 5, 30, 10);
            tameDelay.put(p, System.currentTimeMillis());
        }
        if(stack.getType() == Material.REDSTONE) {
            if(id != null && id.equals(p.getUniqueId()) || p.hasPermission("animalTaming.adminUntame")) {
                BaseComponent base = new TextComponent("Klicke hier um das Tier zu entzähmen!");
                base.setColor(ChatColor.DARK_GREEN);
                base.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/untame " + mount.getUniqueId().toString()));
                p.spigot().sendMessage(base);
                tameDelay.put(p, System.currentTimeMillis());
            }
        }
    }

    private static void processInteract(Entity e, Player p){

    }

    static UUID getOwnerUUID(Entity e) {
        Set<String> tags = e.getScoreboardTags();
        UUID id = null;
        for(String s : tags){
            s = s.toLowerCase();
            if(s.startsWith("owner")){
                String idS = s.substring(s.indexOf('-') + 1, s.lastIndexOf('-'));
                try {
                    if (!idS.equals("")) id = UUID.fromString(idS);
                }catch (IllegalArgumentException e1){
                    AnimalTaming.plugin.getLogger().warning("Entity " + e.getUniqueId() + " has a corrupted Owner-Tag! (" + e1.getMessage() + ")");
                }
            }
        }
        return id;
    }
}
