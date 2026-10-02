package zaeonninezero.nzgmaddon.test;

import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.common.NetworkGunManager;
import com.mrcrayfish.guns.crafting.WorkbenchRecipes;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.util.ItemStackUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import zaeonninezero.nzgmaddon.common.CreativeGunVariantManager;
import zaeonninezero.nzgmaddon.crafting.VariantWorkbenchRecipe;
import zaeonninezero.nzgmaddon.init.ModCreativeTabs;
import zaeonninezero.nzgmaddon.init.initItems;

@GameTestHolder("nzgmaddon")
@PrefixGameTestTemplate(false)
public final class PortGameTests {
    @GameTest(template = "empty")
    public static void allGunsFireAndConsumeAmmo(GameTestHelper helper) {
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "NZGESmokeTest"));
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        var pos = helper.absolutePos(new net.minecraft.core.BlockPos(2, 8, 2));
        player.moveTo(pos.getX(), pos.getY(), pos.getZ(), 0, 0);
        java.util.concurrent.atomic.AtomicInteger projectiles = new java.util.concurrent.atomic.AtomicInteger();
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> listener = event -> {
            if (event.getEntity() instanceof com.mrcrayfish.guns.entity.ProjectileEntity) projectiles.incrementAndGet();
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);
        try {
            for (var entry : initItems.ITEMS.getEntries()) {
                if (!(entry.get() instanceof GunItem gun)) continue;
                ItemStack stack = ModCreativeTabs.loadedStack(gun);
                player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
                int before = projectiles.get();
                com.mrcrayfish.guns.common.network.ServerPlayHandler.handleShoot(
                        new com.mrcrayfish.guns.network.message.C2SMessageShoot(0, 0), player);
                helper.assertTrue(ItemStackUtil.getOrCreateTag(stack).getInt("AmmoCount") == gun.getGun().getGeneral().getMaxAmmo() - 1,
                        "Shot did not consume ammo: " + entry.getId());
                helper.assertTrue(projectiles.get() - before == gun.getGun().getGeneral().getProjectileAmount(),
                        "Incorrect projectile count: " + entry.getId());
            }
        } finally {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);
            player.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void allGunsReloadFromInventory(GameTestHelper helper) {
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "NZGEReloadTest"));
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        for (var entry : initItems.ITEMS.getEntries()) {
            if (!(entry.get() instanceof GunItem gun)) continue;
            player.getInventory().clearContent();
            ItemStack stack = new ItemStack(gun);
            int capacity = gun.getGun().getGeneral().getMaxAmmo();
            player.getInventory().setItem(0, stack);
            var ammo = new ItemStack(BuiltInRegistries.ITEM.get(gun.getGun().getProjectile().getItem()), capacity);
            player.getInventory().setItem(1, ammo);
            com.mrcrayfish.guns.init.ModSyncedDataKeys.RELOADING.setValue(player, true);
            for (int tick = 0; tick < 4000 && com.mrcrayfish.guns.init.ModSyncedDataKeys.RELOADING.getValue(player); tick++) {
                player.tickCount++;
                com.mrcrayfish.guns.common.ReloadTracker.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
            }
            helper.assertTrue(ItemStackUtil.getOrCreateTag(stack).getInt("AmmoCount") == capacity, "Reload failed: " + entry.getId());
            helper.assertTrue(ammo.isEmpty(), "Reload did not consume inventory ammunition: " + entry.getId());
        }
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void gunsAndRegistries(GameTestHelper helper) {
        helper.assertTrue(initItems.ITEMS.getEntries().size() == 41, "Expected 41 registered addon items");
        var guns = NetworkGunManager.get().getRegisteredGuns();
        long count = guns.keySet().stream().filter(id -> id.getNamespace().equals("nzgmaddon")).count();
        helper.assertTrue(count == 21, "Expected 21 loaded addon guns, got " + count);
        for (var entry : initItems.ITEMS.getEntries()) {
            if (!(entry.get() instanceof GunItem item)) continue;
            Gun gun = item.getGun();
            helper.assertTrue(gun.getGeneral().getMaxAmmo() > 0, "Empty gun data: " + entry.getId());
            helper.assertTrue(gun.getGeneral().getRate() > 0, "Invalid rate: " + entry.getId());
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(gun.getProjectile().getItem()), "Missing ammunition: " + entry.getId());
            helper.assertTrue(gun.getProjectile().getDamage() > 0, "Invalid damage: " + entry.getId());
            ItemStack stack = ModCreativeTabs.loadedStack(item);
            helper.assertTrue(Gun.hasAmmo(stack), "Creative gun is not loaded: " + entry.getId());
            ItemStack restored = ItemStack.parseOptional(helper.getLevel().registryAccess(), (CompoundTag) stack.save(helper.getLevel().registryAccess()));
            helper.assertTrue(ItemStack.isSameItemSameComponents(stack, restored), "Gun save/load mismatch: " + entry.getId());
        }
        helper.assertTrue(initItems.REVOLVER.get().getGun().getGeneral().getMaxAmmo() == 6, "Revolver data fell back to defaults");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void recipesAndNetwork(GameTestHelper helper) {
        var recipes = WorkbenchRecipes.getAll(helper.getLevel()).stream()
                .filter(recipe -> recipe.getId().getNamespace().equals("nzgmaddon")).toList();
        helper.assertTrue(recipes.size() == 52, "Expected 52 addon recipes, got " + recipes.size());
        for (var recipe : recipes) {
            helper.assertTrue(recipe instanceof VariantWorkbenchRecipe, "Wrong serializer: " + recipe.getId());
            helper.assertTrue(WorkbenchRecipes.getRecipeById(helper.getLevel(), recipe.getId()) == recipe, "Recipe identity collision: " + recipe.getId());
            for (var ingredient : recipe.getMaterials()) {
                helper.assertTrue(ingredient.getItems().length > 0, "Unresolved material tag: " + recipe.getId());
            }
            var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
            try {
                VariantWorkbenchRecipe.Serializer.STREAM_CODEC.encode(buffer, (VariantWorkbenchRecipe) recipe);
                var restored = VariantWorkbenchRecipe.Serializer.STREAM_CODEC.decode(buffer);
                helper.assertTrue(restored.getId().equals(recipe.getId()), "Recipe ID lost during sync");
                helper.assertTrue(ItemStack.isSameItemSameComponents(restored.getItem(), recipe.getItem()), "Variant lost during sync: " + recipe.getId());
                for (int i = 0; i < recipe.getMaterials().size(); i++) {
                    helper.assertTrue(restored.getMaterials().get(i).getCount() == recipe.getMaterials().get(i).getCount(), "Material count lost");
                    helper.assertTrue(restored.getMaterials().get(i).getItems().length == recipe.getMaterials().get(i).getItems().length, "Material tag alternatives lost");
                }
            } finally { buffer.release(); }
        }
        var pump1 = WorkbenchRecipes.getRecipeById(helper.getLevel(), ResourceLocation.parse("nzgmaddon:pump_shotgun_1")).getItem();
        var pump2 = WorkbenchRecipes.getRecipeById(helper.getLevel(), ResourceLocation.parse("nzgmaddon:pump_shotgun_2")).getItem();
        helper.assertTrue(pump1.get(DataComponents.CUSTOM_MODEL_DATA).value() == 1, "Custom model component lost");
        helper.assertTrue(ItemStackUtil.getOrCreateTag(pump2).getInt("HeatShield") == 1, "HeatShield variant lost");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void creativeVariants(GameTestHelper helper) {
        NonNullList<ItemStack> variants = NonNullList.create();
        CreativeGunVariantManager.addItemVariants(variants);
        helper.assertTrue(variants.size() == 11, "Expected 11 creative variants");
        for (ItemStack stack : variants) {
            helper.assertTrue(Gun.hasAmmo(stack), "Variant not loaded");
            var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
            try {
                ItemStack.STREAM_CODEC.encode(buffer, stack);
                ItemStack restored = ItemStack.STREAM_CODEC.decode(buffer);
                helper.assertTrue(ItemStack.isSameItemSameComponents(stack, restored), "Creative variant did not survive sync");
            } finally { buffer.release(); }
        }
        helper.succeed();
    }
}
