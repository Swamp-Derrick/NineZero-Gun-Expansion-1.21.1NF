package zaeonninezero.nzgmaddon.client.render.gun.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.client.GunModel;
import zaeonninezero.nzgmaddon.client.SpecialModels;
import com.mrcrayfish.guns.client.render.gun.IOverrideModel;
import com.mrcrayfish.guns.client.util.RenderUtil;
import com.mrcrayfish.guns.item.GunItem;
import com.mrcrayfish.guns.item.attachment.IAttachment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import com.mrcrayfish.guns.util.ItemStackUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 * Modified by zaeonNineZero for Nine Zero's Gun Expansion
 * Attachment detection logic based off of code from Mo' Guns by Bomb787 and AlanorMiga (MigaMi)
 */
public class PumpShotgunModel implements IOverrideModel
{

    @Override
    // This class renders a model with support for NBT and attachment based part variations

    // We start by declaring our render function that will handle rendering the core baked model (which is a non-moving part).
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, @Nullable LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay)
    {
        // Render the item's BakedModel, which will serve as the core of our custom model.
        // We select which model variant to use by fetching the value of the CustomModelData tag.
        BakedModel bakedModel = SpecialModels.PUMP_SHOTGUN_BASE.getModel();
        if (getVariant(stack) == 1 || getVariant(stack, "BaseVariant") == 1)
        bakedModel = SpecialModels.PUMP_SHOTGUN_BASE_1.getModel();

        // Render the BakedModel we selected.
        Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.NONE, false, poseStack, buffer, light, overlay, GunModel.wrap(bakedModel));

        // Render the iron sights element.
        // This time we have two criteria needed to render the sights element:
        // 1. A scope must NOT be attached.
        // 2. A stock must be attached.
        ItemStack scopeStack = Gun.getAttachment(IAttachment.Type.SCOPE, stack);
        ItemStack stockStack = Gun.getAttachment(IAttachment.Type.STOCK, stack);
        if(scopeStack.isEmpty() && !stockStack.isEmpty())
        {
            RenderUtil.renderModel(SpecialModels.PUMP_SHOTGUN_SIGHTS.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        }

        // Render the top rail element.
        // This element appears when a scope is attached.
        // The actual model element used depends (again) on the base model variant.
        if(!scopeStack.isEmpty())
        {
            BakedModel topRailModel = SpecialModels.PUMP_SHOTGUN_TOP_RAIL.getModel();
            if (getVariant(stack) == 1)
            topRailModel = SpecialModels.PUMP_SHOTGUN_TOP_RAIL_1.getModel();
            RenderUtil.renderModel(topRailModel, transformType, null, stack, parent, poseStack, buffer, light, overlay);
        }

        // Heat Shield element, an optional part that renders if HeatShield is equal to 1.
        if(getVariant(stack, "HeatShield") != 0)
        {
            RenderUtil.renderModel(SpecialModels.PUMP_SHOTGUN_HEAT_SHIELD.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        }

        // First, some variables for animation building
        boolean isPlayer = entity != null && entity.equals(Minecraft.getInstance().player);
        boolean isFirstPerson = (transformType.firstPerson());
        boolean correctContext = (transformType.firstPerson() || transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND);

        Vec3 pumpTranslations = Vec3.ZERO;

        Vec3 bulletTranslations = Vec3.ZERO;
        Vec3 bulletRotations = Vec3.ZERO;
        Vec3 bulletRotOffset = Vec3.ZERO;

        {
            if(isPlayer && correctContext)
            {
                float cooldownDivider = 3.7F;
                float cooldownOffset1 = 0.7F;
                float intensity = 3.6F +1;

                ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
                float cooldown = tracker.getCooldownPercent(stack.getItem(), partialTicks);
                cooldown *= cooldownDivider;
                float cooldown_a = cooldown-cooldownOffset1;

                float cooldown_b = Math.min(Math.max(cooldown_a*intensity,0),1);
                float cooldown_c = Math.min(Math.max((-cooldown_a*intensity)+intensity,0),1);
                float cooldown_d = Math.min(cooldown_b,cooldown_c);

                pumpTranslations = new Vec3(0, 0, cooldown_d * 1.8);
            }
        }

        // Pump Shotgun slide. This animated part cycles backward then forward after firing.
        // Push pose so we can make do transformations without affecting the models above.
        poseStack.pushPose();
        // Now we apply our transformations. This will only occur under certain conditions.
        ItemStack gripStack = Gun.getAttachment(IAttachment.Type.UNDER_BARREL, stack);
        if(isPlayer && gripStack.isEmpty())
        poseStack.translate(0, 0, pumpTranslations.z * 0.0625);
        // Our transformations are done - now we can render the model.
        RenderUtil.renderModel(SpecialModels.PUMP_SHOTGUN_PUMP.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        // Pop pose to compile everything in the render matrix.
        poseStack.popPose();

    }

    //NBT fetch code for skin variants - ported from the "hasAmmo" function under common/Gun.java
    public static int getVariant(ItemStack gunStack)
    {
        CompoundTag tag = ItemStackUtil.getOrCreateTag(gunStack);
        return gunStack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_MODEL_DATA, net.minecraft.world.item.component.CustomModelData.DEFAULT).value();
    }

    //NBT fetch code for skin variants - ported from the "hasAmmo" function under common/Gun.java
    public static int getVariant(ItemStack gunStack, String tag_name)
    {
        CompoundTag tag = ItemStackUtil.getOrCreateTag(gunStack);
        return tag.getInt(tag_name);
    }
}
