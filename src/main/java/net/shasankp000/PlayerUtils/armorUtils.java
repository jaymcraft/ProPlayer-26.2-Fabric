package net.shasankp000.PlayerUtils;

import com.mojang.datafixers.util.Pair; // Import the correct Pair class

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;


public class armorUtils {
    public static void autoEquipArmor(ServerPlayer bot) {
        Inventory inventory = bot.getInventory();

        // Prepare a list of equipment updates to notify clients
        List<Pair<EquipmentSlot, ItemStack>> equipmentUpdates = new ArrayList<>();

        // Iterate through all armor slots
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack equippedArmor = bot.getItemBySlot(slot);

            // Find the best armor piece in the inventory for this slot
            ItemStack bestArmor = findBestArmor(inventory, slot);

            // Equip the armor if it's better than what's currently equipped
            if (!bestArmor.isEmpty() && (equippedArmor.isEmpty() || isBetterArmor(bestArmor, equippedArmor))) {
                bot.setItemSlot(slot, bestArmor);
                inventory.removeItem(bestArmor); // Remove the equipped armor from inventory
                System.out.println("Equipped " + bestArmor.getHoverName().getString() + " in slot " + slot.getName());

                // Add this update to the list for notifying clients
                equipmentUpdates.add(new Pair<>(slot, bestArmor)); // Use com.mojang.datafixers.util.Pair

                bot.setItemSlot(slot, bestArmor.copy()); // update the armor slots data for the server for the bot.
            }
        }

        // Send the equipment update packet to all nearby players
        if (!equipmentUpdates.isEmpty()) {
            bot.level().players().forEach(player ->
                    player.connection.send(new ClientboundSetEquipmentPacket(bot.getId(), equipmentUpdates))
            );
        }
    }

    // Helper method to find the best armor for a specific slot
    private static ItemStack findBestArmor(Inventory inventory, EquipmentSlot slot) {
        ItemStack bestArmor = ItemStack.EMPTY;
        int bestProtection = 0;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);
            net.minecraft.world.item.equipment.Equippable equippable = item.get(DataComponents.EQUIPPABLE);
            if (equippable != null && equippable.slot() == slot) {
                int protection = getArmorProtection(item, slot);
                if (protection > bestProtection) {
                    bestProtection = protection;
                    bestArmor = item;
                }
            }
        }
        return bestArmor;
    }

    // Helper method to compare two armor pieces
    private static boolean isBetterArmor(ItemStack newArmor, ItemStack currentArmor) {
        if (newArmor.isEmpty() || !newArmor.has(DataComponents.EQUIPPABLE)) {
            return false;
        }

        if (currentArmor.isEmpty() || !currentArmor.has(DataComponents.EQUIPPABLE)) {
            return true;
        }

        net.minecraft.world.item.equipment.Equippable equippable = newArmor.get(DataComponents.EQUIPPABLE);
        if (equippable == null) {
            return false;
        }

        EquipmentSlot slot = equippable.slot();
        int newProtection = getArmorProtection(newArmor, slot);
        int currentProtection = getArmorProtection(currentArmor, slot);

        return newProtection > currentProtection;
    }

    private static int getArmorProtection(ItemStack stack, EquipmentSlot slot) {
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        return (int) modifiers.compute(Attributes.ARMOR, 0.0, slot);
    }

    public static void autoDeEquipArmor(ServerPlayer bot) {
        // still a work-in-progress.

        Inventory inventory = bot.getInventory();

        // Prepare a list of equipment updates to notify clients
        List<Pair<EquipmentSlot, ItemStack>> equipmentUpdates = new ArrayList<>();



        // Iterate through all armor slots
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack equippedArmor = bot.getItemBySlot(slot);

            System.out.println(equippedArmor.getHoverName().getString());

            // If the bot has armor equipped in this slot
            if (!equippedArmor.isEmpty()) {
                // Add the armor back to the inventory
                if (inventory.add(equippedArmor)) {
                    // Clear the equipped armor slot
                    bot.setItemSlot(slot, ItemStack.EMPTY);

                    // Add this update to the list for notifying clients
                    equipmentUpdates.add(new Pair<>(slot, ItemStack.EMPTY));

                    System.out.println("De-equipped " + equippedArmor.getHoverName().getString() + " from slot " + slot.getName());
                } else {
                    System.out.println("Inventory full! Could not de-equip " + equippedArmor.getHoverName().getString() + " from slot " + slot.getName());
                }
            }
        }

        // Send the equipment update packet to all nearby players
        if (!equipmentUpdates.isEmpty()) {
            bot.level().players().forEach(player ->
                    player.connection.send(new ClientboundSetEquipmentPacket(bot.getId(), equipmentUpdates))
            );
        }
    }


}
