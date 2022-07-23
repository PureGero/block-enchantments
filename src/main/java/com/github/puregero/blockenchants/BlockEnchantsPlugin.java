package com.github.puregero.blockenchants;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BlockEnchantsPlugin extends JavaPlugin {

    public final Set<Enchantment> allowedToolEnchantments = new HashSet<>();
    public final Set<Enchantment> allowedArmorEnchantments = new HashSet<>();
    public final Map<Enchantment, Integer> maximumLevels = new HashMap<>();

    @Override
    public void onEnable() {
        for (Component component : loadConfig()) {
            this.getLogger().warning(PlainTextComponentSerializer.plainText().serialize(component));
        }

        new BlockEnchantsCommand(this);
        new BlockEnchantsListener(this);
    }

    public Collection<Component> loadConfig() {
        List<Component> errorMessages = new ArrayList<>();
        this.saveDefaultConfig();
        this.reloadConfig();

        loadAllowedEnchants(this.getConfig().getConfigurationSection("allowed-enchants"), errorMessages);
        loadMaxLevels(this.getConfig().getConfigurationSection("max-levels"), errorMessages);

        return errorMessages.stream().map(component -> component.color(NamedTextColor.RED)).toList();
    }

    private void loadAllowedEnchants(ConfigurationSection section, List<Component> errorMessages) {
        allowedArmorEnchantments.clear();
        allowedToolEnchantments.clear();
        loadAllowedEnchants(section.getStringList("all"), errorMessages, allowedArmorEnchantments, allowedToolEnchantments);
        loadAllowedEnchants(section.getStringList("armor"), errorMessages, allowedArmorEnchantments);
        loadAllowedEnchants(section.getStringList("tools"), errorMessages, allowedToolEnchantments);
    }

    private void loadAllowedEnchants(List<String> enchantmentList, List<Component> errorMessages, Set<Enchantment>... allowedEnchantments) {
        for (String key : enchantmentList) {
            try {
                Enchantment enchantment = Enchantment.getByKey(new NamespacedKey("minecraft", key));
                if (enchantment == null) {
                    errorMessages.add(Component.text("Could not find enchantment: " + key));
                } else {
                    for (Set<Enchantment> allowedEnchantmentSet : allowedEnchantments) {
                        allowedEnchantmentSet.add(enchantment);
                    }
                }
            } catch (Exception e) {
                errorMessages.add(Component.text("Could not parse enchantment: " + key + " (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")"));
            }
        }
    }

    private void loadMaxLevels(ConfigurationSection section, List<Component> errorMessages) {
        maximumLevels.clear();
        for (String key : section.getKeys(false)) {
            try {
                Enchantment enchantment = Enchantment.getByKey(new NamespacedKey("minecraft", key));
                if (enchantment == null) {
                    errorMessages.add(Component.text("Could not find enchantment in max-level: " + key + ": " + section.get(key)));
                } else {
                    maximumLevels.put(enchantment, section.getInt(key));
                }
            } catch (Exception e) {
                errorMessages.add(Component.text("Could not parse enchantment in max-level: " + key + ": " + section.get(key) + " (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")"));
            }
        }
    }

}
