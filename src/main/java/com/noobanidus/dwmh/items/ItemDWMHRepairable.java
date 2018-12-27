package com.noobanidus.dwmh.items;

import com.noobanidus.dwmh.DWMH;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemDWMHRepairable extends Item {
    private String internalRepair;

    public void registerPredicate (String predicate_name) {
         addPropertyOverride(new ResourceLocation("dwmh", predicate_name), new IItemPropertyGetter() {
            @Override
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                if (stack.getItem() instanceof ItemEnchantedCarrot) {
                    if (ItemEnchantedCarrot.unbreakable && stack.getItemDamage() == ItemEnchantedCarrot.maxUses)
                        return 1;
                    return 0;
                } else if (stack.getItem() instanceof ItemWhistle) {
                    if (stack.getItemDamage() == ItemWhistle.maxUses)
                        return 1;
                    return 0;
                }

                return 0;
            }
        });
    }

    public void setInternalRepair (String item) {
        internalRepair = item;
    }

    public String getInternalRepair () {
        return internalRepair;
    }

    public ItemStack getRepairItem () {
        String intr = getInternalRepair();

        String[] parts = intr.split(":");
        if (parts.length != 3) {
            DWMH.LOG.error(String.format("Repair item specified in configuration invalid: |%s|", intr));
            return ItemStack.EMPTY;
        }

        Item repairInt = Item.REGISTRY.getObject(new ResourceLocation(parts[0], parts[1]));
        if (repairInt == null) {
            DWMH.LOG.error(String.format("Repair item specified in configuration does not exist: |%s|", intr));
            return ItemStack.EMPTY;
        }

        int metadata;
        try {
            metadata = Integer.parseInt(parts[2]);
        } catch (NumberFormatException nfe) {
            DWMH.LOG.error(String.format("Repair item metadata is not a valid integer: |%s|", intr));
            return ItemStack.EMPTY;
        }

        ItemStack repair = new ItemStack(repairInt, 1, metadata);
        return repair;
    }

    @Override
    public boolean getIsRepairable (ItemStack item, ItemStack repairingItem) {
        ItemStack myRepair = getRepairItem();
        if (repairingItem.getItem().equals(myRepair.getItem()) && repairingItem.getMetadata() == myRepair.getMetadata()) {
            return true;
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    public static boolean useableItem (ItemStack item) {
        if (item.getItemDamage() == item.getItem().getMaxDamage()) {
            return false;
        } else {
            return true;
        }
    }

    public static void damageItem (ItemStack item, EntityPlayer player) {
        damageItem(item, player, true);
    }

    public static void damageItem (ItemStack item, EntityPlayer player, boolean unbreakable) {
        // Most calls to this should be wrapped in this but it doesn't hurt
        if (player.capabilities.isCreativeMode) return;

        if (item.getItem() instanceof ItemDWMHRepairable) {
            if (unbreakable && useableItem(item)) {
                item.damageItem(1, player);
            }
        } else {
            DWMH.LOG.error(String.format("Attempted to damage a non-DWMH item! |%s|", item.getDisplayName()));
        }
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        if (stack.getItemDamage() == stack.getMaxDamage()) return false;

        return true;
    }
}