package zaeonninezero.nzgmaddon.client.render.gun.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.client.GunModel;
import com.mrcrayfish.guns.client.handler.ReloadHandler;

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
import net.minecraft.util.Mth;
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
public class MachineGunModel implements IOverrideModel
{

    @Override
    // This class renders a multi-part model that supports animations and removeable parts.

    // Declare our render function that will handle rendering all model components.
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, @Nullable LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay)
    {
        // Render the item's BakedModel, which will serve as the core of our custom model.
        BakedModel bakedModel = SpecialModels.MACHINE_GUN_BASE.getModel();
        Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.NONE, false, poseStack, buffer, light, overlay, GunModel.wrap(bakedModel));

        // Render the iron sights element, which is only present when a scope is not attached.
        // We have to grab the gun's scope attachment slot and check whether it is empty or not.
        // If the isEmpty function returns true, then we render the iron sights.
        ItemStack attachmentStack = Gun.getAttachment(IAttachment.Type.SCOPE, stack);
        if(attachmentStack.isEmpty())
        {
            RenderUtil.renderModel(SpecialModels.MACHINE_GUN_SIGHTS.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        }

        // Heads Up! This section will be pretty big, as the Machine Gun's model has lots of moving parts.
        // Some parts might be light on description/documentation!

        boolean isPlayer = entity != null && entity.equals(Minecraft.getInstance().player);
        boolean isFirstPerson = (transformType.firstPerson());
        boolean correctContext = (transformType.firstPerson() || transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND);

        Vec3 boltTranslations = Vec3.ZERO;

        Vec3 magTranslations = Vec3.ZERO;
        Vec3 magRotations = Vec3.ZERO;
        Vec3 magRotOffset = Vec3.ZERO;

        Vec3 handleRotations = Vec3.ZERO;
        Vec3 handleRotOffset = Vec3.ZERO;

        float bulletMovement = 0F;

        GunItem gunStack = (GunItem) stack.getItem();
        Gun gun = gunStack.getModifiedGun(stack);
        if(isPlayer && correctContext)
        {
            float cooldownDivider = 1.0F*Math.max((float) gun.getGeneral().getRate()/2F,1);
            float cooldownOffset1 = cooldownDivider - 1.0F;
            float intensity = 1.0F +1;

            float cooldownOffset2 = cooldownDivider - 1.0F;
            float intensity2 = 0.05F +1;

            ItemCooldowns tracker = Minecraft.getInstance().player.getCooldowns();
            float cooldown = tracker.getCooldownPercent(stack.getItem(), partialTicks);
            cooldown*=cooldownDivider;
            float cooldown_a = cooldown-cooldownOffset1;

            float cooldown_b = Math.min(Math.max(cooldown_a*intensity,0),1);
            float cooldown_c = Math.min(Math.max((-cooldown_a*intensity)+intensity,0),1);
            float cooldown_d = Math.min(cooldown_b,cooldown_c);

            float cooldown_e = cooldown-cooldownOffset2;
            float cooldown_f = Math.min(Math.max(cooldown_e*intensity2,0),1);

            boltTranslations = boltTranslations.add(0, 0, cooldown_d * 2.3);
            bulletMovement = Math.max(bulletMovement,cooldown_f);

            //handleRotations = new Vec3(0,0,cooldown_f*8);
            handleRotations = handleRotations.add(0,0,cooldown_f*8);
        }

        // Machine Gun Bolt. This animated part kicks backward on firing, then moves back to its resting position.
        poseStack.pushPose();
        // Apply transformations to this part.
        if(isPlayer)
        poseStack.translate(0, 0, boltTranslations.z * 0.0625);
        // Render the transformed model.
        RenderUtil.renderModel(SpecialModels.MACHINE_GUN_BOLT.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        // Pop pose to compile everything in the render matrix.
        poseStack.popPose();

        // MG Bullet 1.
        if (shouldRenderBullet(stack, 5))
        {
        poseStack.pushPose();
        // Transformations
        if(isPlayer && isFirstPerson)
        {
            poseStack.translate(0, (bulletMovement * -0.48) * 0.0625, 0);

        }
        // Render
        RenderUtil.renderModel(SpecialModels.MACHINE_GUN_BULLET1.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        // POP POP
        poseStack.popPose();
        }

        // MG Bullet 2.
        if (shouldRenderBullet(stack, 4))
        {
        poseStack.pushPose();
        // Transformations
        if(isPlayer && isFirstPerson)
        {
            poseStack.translate((bulletMovement * -0.041) * 0.0625, (bulletMovement * -0.475) * 0.0625, 0);

        }
        // Render
        RenderUtil.renderModel(SpecialModels.MACHINE_GUN_BULLET2.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        // POP POP
        poseStack.popPose();
        }

        // MG Bullet 3.
        if (shouldRenderBullet(stack, 3))
        {
        poseStack.pushPose();
        // Transformations
        if(isPlayer && isFirstPerson)
        {
            poseStack.translate((bulletMovement * -0.1) * 0.0625, (bulletMovement * -0.47) * 0.0625, 0);

        }
        // Render
        RenderUtil.renderModel(SpecialModels.MACHINE_GUN_BULLET3.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        // POP POP
        poseStack.popPose();
        }

        // MG Bullet 4.
        if (shouldRenderBullet(stack, 2))
        {
        poseStack.pushPose();
        // Transformations
        if(isPlayer && isFirstPerson)
        {
            poseStack.translate((bulletMovement * -0.24) * 0.0625, (bulletMovement * -0.47) * 0.0625, 0);

        }
        // Render
        RenderUtil.renderModel(SpecialModels.MACHINE_GUN_BULLET4.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        // POP POP
        poseStack.popPose();
        }

        // MG Bullet 5.
        if (shouldRenderBullet(stack, 1))
        {
        poseStack.pushPose();
        // Transformations
        if(isPlayer && isFirstPerson)
        {
            poseStack.translate((bulletMovement * -0.5) * 0.0625, (bulletMovement * -0.18) * 0.0625, 0);

        }
        // Render
        RenderUtil.renderModel(SpecialModels.MACHINE_GUN_BULLET5.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        // POP POP
        poseStack.popPose();
        }

        // Magazine transforms
        poseStack.pushPose();
        // Apply transformations to this part.

        // Box model selection and rendering
        SpecialModels magModel = SpecialModels.MACHINE_GUN_BOX;

        RenderUtil.renderModel(magModel.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        // Pop pose to compile everything in the render matrix.
        poseStack.popPose();

        // Machine Gun Handle. This part rotates slightly to simulate the effects of recoil on it.
        poseStack.pushPose();
        // Transformations
        poseStack.translate(0.811 * 0.0625, -3.04 * 0.0625, 0);
        poseStack.mulPose(Axis.ZN.rotationDegrees((float) handleRotations.z));
        poseStack.translate(-0.811 * 0.0625, 3.04 * 0.0625, 0);
        // Render
        RenderUtil.renderModel(SpecialModels.MACHINE_GUN_HANDLE.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        // POP POP
        poseStack.popPose();

        // Phew! That was a lot of stuff to render.
    }

    //Code check for whether a bullet should be rendered.
    public boolean shouldRenderBullet(ItemStack gunStack, int bullet)
    {
        CompoundTag tag = ItemStackUtil.getOrCreateTag(gunStack);

            boolean displayBullet = tag.getInt("AmmoCount") >= bullet;
            if (tag.getBoolean("IgnoreAmmo"))
            displayBullet = true;

            return displayBullet;

    }
}
