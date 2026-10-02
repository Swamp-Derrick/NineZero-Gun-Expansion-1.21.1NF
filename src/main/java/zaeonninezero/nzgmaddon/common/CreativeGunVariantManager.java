package zaeonninezero.nzgmaddon.common;

import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.util.ItemStackUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import zaeonninezero.nzgmaddon.init.ModCreativeTabs;
import zaeonninezero.nzgmaddon.init.initItems;

public final class CreativeGunVariantManager {
    public static void addItemVariants(NonNullList<ItemStack> items) {
        items.add(variant(initItems.SUBMACHINE_GUN.get(), "BaseVariant", 1));
        ItemStack pump = ModCreativeTabs.loadedStack(initItems.PUMP_SHOTGUN.get());
        pump.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(1));
        items.add(pump);
        items.add(variant(initItems.PUMP_SHOTGUN.get(), "BaseVariant", 1, "HeatShield", 1));
        items.add(variant(initItems.HEAVY_ASSAULT_RIFLE.get(), "BaseVariant", 1));
        items.add(variant(initItems.BATTLE_RIFLE.get(), "SightVariant", 1));
        items.add(variant(initItems.LEVER_ACTION_RIFLE.get(), "BaseVariant", 1));
        items.add(variant(initItems.INFANTRY_RIFLE.get(), "BaseVariant", 1, "ExtraRails", 1));
        items.add(variant(initItems.AUTOMATIC_SNIPER_RIFLE.get(), "BaseVariant", 1));
        items.add(variant(initItems.AUTOMATIC_SNIPER_RIFLE.get(), "BaseVariant", 1, "HandguardVariant", 1));
        items.add(variant(initItems.BOLT_ACTION_RIFLE.get(), "BaseVariant", 1));
        items.add(variant(initItems.BOLT_ACTION_RIFLE.get(), "BaseVariant", 2));
    }

    private static ItemStack variant(GunItem gun, Object... values) {
        ItemStack stack = ModCreativeTabs.loadedStack(gun);
        var tag = ItemStackUtil.getOrCreateTag(stack);
        for (int i = 0; i < values.length; i += 2) {
            tag.putInt((String) values[i], (Integer) values[i + 1]);
        }
        ItemStackUtil.setTag(stack, tag);
        return stack;
    }

    private CreativeGunVariantManager() {}
}
