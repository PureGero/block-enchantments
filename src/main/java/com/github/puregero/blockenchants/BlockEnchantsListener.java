package com.github.puregero.blockenchants;

import com.github.puregero.multilib.MultiLib;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class BlockEnchantsListener implements Listener, Runnable {
    private final BlockEnchantsPlugin plugin;

    public BlockEnchantsListener(BlockEnchantsPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getScheduler().runTaskTimer(plugin, this, 100, 100);
    }

    @Override
    public void run() {
        for (Player player : MultiLib.getLocalOnlinePlayers()) {
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack itemStack = player.getInventory().getItem(i);
                if (itemStack != null) {
                    ItemStack newItem = removeEnchants(itemStack);
                    if (!newItem.equals(itemStack)) {
                        player.getInventory().setItem(i, newItem);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onAnvil(PrepareAnvilEvent event) {
        ItemStack originalItem = event.getResult();
        ItemStack newItem = removeEnchants(originalItem);
        if (newItem != null && !newItem.equals(originalItem)) {
            event.setResult(newItem);
        }
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        for (Map.Entry<Enchantment, Integer> entry : new ArrayList<>(event.getEnchantsToAdd().entrySet())) {
            Set<Enchantment> allowList = getAllowList(event.getItem().getType());
            if (!allowList.contains(entry.getKey())) {
                event.getEnchantsToAdd().remove(entry.getKey());
            } else if (entry.getValue() > plugin.maximumLevels.getOrDefault(entry.getKey(), entry.getKey().getMaxLevel())) {
                event.getEnchantsToAdd().remove(entry.getKey());
                event.getEnchantsToAdd().put(entry.getKey(), plugin.maximumLevels.getOrDefault(entry.getKey(), entry.getKey().getMaxLevel()));
            }
        }
    }

    @EventHandler
    public void onClickVillager(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager villager) {
            for (int i = 0; i < villager.getRecipeCount(); i++) {
                MerchantRecipe recipe = villager.getRecipe(i);
                ItemStack originalItem = recipe.getResult();
                ItemStack newItem = removeEnchants(originalItem);
                if (newItem != null && !newItem.equals(originalItem)) {
                    villager.setRecipe(i, new MerchantRecipe(newItem, recipe.getUses(), recipe.getMaxUses(), recipe.hasExperienceReward(), recipe.getVillagerExperience(), recipe.getPriceMultiplier(), recipe.getDemand(), recipe.getSpecialPrice(), recipe.shouldIgnoreDiscounts()));
                }
            }
        }
    }

    private Set<Enchantment> getAllowList(Material material) {
        if (material.name().contains("CHESTPLATE") || material.name().contains("LEGGINGS") || material.name().contains("BOOTS") || material.name().contains("HELMET")) {
            return plugin.allowedArmorEnchantments;
        } else {
            return plugin.allowedToolEnchantments;
        }
    }

    private ItemStack removeEnchants(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return itemStack;
        }

        ItemMeta meta = itemStack.getItemMeta();
        boolean modifiedEnchants = false;
        for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
            Set<Enchantment> allowList = getAllowList(itemStack.getType());
            if (!allowList.contains(entry.getKey())) {
                meta.removeEnchant(entry.getKey());
                modifiedEnchants = true;
            } else if (entry.getValue() > plugin.maximumLevels.getOrDefault(entry.getKey(), entry.getKey().getMaxLevel())) {
                meta.removeEnchant(entry.getKey());
                meta.addEnchant(entry.getKey(), plugin.maximumLevels.getOrDefault(entry.getKey(), entry.getKey().getMaxLevel()), true);
                modifiedEnchants = true;
            }
        }

        if (modifiedEnchants) {
            itemStack = itemStack.clone();
            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

}
