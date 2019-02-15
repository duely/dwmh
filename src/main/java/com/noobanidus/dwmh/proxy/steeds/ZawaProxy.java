package com.noobanidus.dwmh.proxy.steeds;

import com.noobanidus.dwmh.DWMH;
import com.noobanidus.dwmh.config.DWMHConfig;
import com.noobanidus.dwmh.items.ItemEnchantedCarrot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.zawamod.entity.base.ZAWABaseLand;

@SuppressWarnings("unused")
public class ZawaProxy implements ISteedProxy {
    ZAWABaseLand.AIFight aifight = null;
    EntityAINearestAttackableTarget ainearatt = null;

    @Override
    public boolean isTeleportable(Entity entity, EntityPlayer player) {
        if (!isListable(entity, player)) {
            return false;
        }

        return isSaddled((ZAWABaseLand) entity) && globalTeleportCheck(entity, player);
    }

    @Override
    public boolean isListable(Entity entity, EntityPlayer player) {
        if (!isMyMod(entity)) {
            return false;
        }

        ZAWABaseLand zawa = (ZAWABaseLand) entity;
        if (zawa.getOwnerId() == null || !zawa.getOwnerId().equals(player.getUniqueID())) {
            return false;
        }

        return true;
    }

    private boolean isSaddled(ZAWABaseLand entity) {
        NBTTagCompound nbt = new NBTTagCompound();
        entity.writeEntityToNBT(nbt);

        if (nbt.hasKey("Saddle")) return nbt.getBoolean("Saddle");

        return false;
    }

    //  These do nothing
    @Override
    public boolean isTameable(Entity entity, EntityPlayer player) {
        if (!isMyMod(entity)) return false;

        ZAWABaseLand animal = (ZAWABaseLand) entity;
        return !animal.isTamed();
    }

    @Override
    public int tame(Entity entity, EntityPlayer player) {
        if (!isMyMod(entity)) return 0;

        ZAWABaseLand animal = (ZAWABaseLand) entity;

        animal.setTamedBy(player);
        animal.setOwnerId(player.getUniqueID());
        if (!player.capabilities.isCreativeMode) {
            ItemStack item = player.inventory.getCurrentItem();
            ItemEnchantedCarrot.damageItem(item, player);
        }

        if (aifight == null || ainearatt == null) {
            aifight = ReflectionHelper.getPrivateValue(ZAWABaseLand.class, animal, "AIFight");
            ainearatt = ReflectionHelper.getPrivateValue(ZAWABaseLand.class, animal, "AINearAtt");
        }

        if (aifight != null && ainearatt != null) {
            animal.tasks.removeTask(aifight);
            animal.targetTasks.removeTask(ainearatt);
        } else {
            DWMH.LOG.error("Unable to remove AI tasks for recently tamed entity.");
        }

        animal.setHunger(animal.getMaxFood());
        animal.setEnrichment(animal.getMaxEnrichment());
        animal.world.setEntityState(animal, (byte) 7);

            doGenericMessage(entity, player, Generic.TAMING, TextFormatting.GOLD);

        return 1;
    }

    @Override
    public boolean isAgeable(Entity entity, EntityPlayer player) {
        if (!isMyMod(entity)) return false;

        ZAWABaseLand animal = (ZAWABaseLand) entity;

        if (animal.isChild()) return true;

        return false;
    }

    @Override
    public int age(Entity entity, EntityPlayer player) {
        ZAWABaseLand animal = (ZAWABaseLand) entity;
        animal.setGrowingAge(0);
        animal.world.setEntityState(animal, (byte) 7);

            doGenericMessage(entity, player, Generic.AGING);

        return 1;
    }

    // The healing is by default in the interface

    @Override
    public boolean isBreedable(Entity entity, EntityPlayer player) {
        return false;
    }

    @Override
    public int breed(Entity entity, EntityPlayer player) {
        return 0;
    }

    @Override
    public ITextComponent getResponseKey(Entity entity, EntityPlayer player) {
        if (!isMyMod(entity)) return null;

        ITextComponent temp;

        ZAWABaseLand animal = (ZAWABaseLand) entity;

        if (animal.hasHome() && animal.world.getTileEntity(animal.getHomePosition()) != null) {
            temp = new TextComponentTranslation("dwmh.strings.unsummonable.working");
            temp.getStyle().setColor(TextFormatting.DARK_RED);
        } else if (animal.getLeashed()) {
            temp = new TextComponentTranslation("dwmh.strings.unsummonable.leashed");
            temp.getStyle().setColor(TextFormatting.DARK_RED);
        } else if (animal.isBeingRidden() && animal.isRidingSameEntity(player)) {
            temp = new TextComponentTranslation("dwmh.strings.unsummonable.ridden");
            temp.getStyle().setColor(TextFormatting.DARK_RED);
        } else if (animal.isBeingRidden() && !DWMHConfig.Ocarina.otherRiders) {
            temp = new TextComponentTranslation("dwmh.strings.unsummonable.ridden_other");
            temp.getStyle().setColor(TextFormatting.DARK_RED);
        } else if (animal.isBeingRidden() && DWMHConfig.Ocarina.otherRiders) {
            temp = new TextComponentTranslation("dwmh.strings.summonable.ridden_other");
            temp.getStyle().setColor(TextFormatting.DARK_AQUA);
        } else if (isSaddled(animal)) {
            temp = new TextComponentTranslation("dwmh.strings.summonable");
            temp.getStyle().setColor(TextFormatting.AQUA);
        } else {
            temp = new TextComponentTranslation("dwmh.strings.unsummonable.unsaddled");
            temp.getStyle().setColor(TextFormatting.RED);
        }

        return temp;
    }

    @Override
    public boolean isMyMod(Entity entity) {
        if (!(entity instanceof ZAWABaseLand)) return false;

        String clazz = entity.getClass().getName();

        if (DWMH.sets("zawa").contains(clazz)) return true;

        DWMH.sets("ignore").add(clazz);
        return false;
    }

    @Override
    public String proxyName() {
        return "zawa";
    }
}


