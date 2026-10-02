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
import com.mrcrayfish.guns.item.attachment.IAttachment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
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
public class HuntingRifleModel implements IOverrideModel
{

    @Override
    // This class renders a multi-part model that supports animations and removeable parts.

    // Declare our render function that will handle rendering all model components.
    public void render(float partialTicks, ItemDisplayContext transformType, ItemStack stack, ItemStack parent, @Nullable LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay)
    {
        // Render the item's BakedModel, which will serve as the core of our custom model.
        BakedModel bakedModel = SpecialModels.HUNTING_RIFLE_BASE.getModel();
        Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.NONE, false, poseStack, buffer, light, overlay, GunModel.wrap(bakedModel));

        // Render the iron sights element, which is only present when a scope is not attached.
        // We have to grab the gun's scope attachment slot and check whether it is empty or not.
        // If the isEmpty function returns true, then we render the iron sights.
        ItemStack attachmentStack = Gun.getAttachment(IAttachment.Type.SCOPE, stack);
        if(attachmentStack.isEmpty())
        {
            RenderUtil.renderModel(SpecialModels.HUNTING_RIFLE_SIGHTS.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        }

        // First, some variables for animation building
        boolean isPlayer = entity != null && entity.equals(Minecraft.getInstance().player);
        boolean isFirstPerson = (transformType.firstPerson());
        boolean correctContext = (transformType.firstPerson() || transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND);

        Vec3 breechRotations = Vec3.ZERO;
        Vec3 breechRotOffset = Vec3.ZERO;

        Vec3 bulletTranslations = Vec3.ZERO;
        Vec3 bulletRotations = Vec3.ZERO;
        Vec3 bulletRotOffset = Vec3.ZERO;

        {
            if(isPlayer && correctContext)
            {
                breechRotOffset = new Vec3(0,-5.45,4.3);
                breechRotations = new Vec3(25 * ReloadHandler.get().getReloadProgress(partialTicks),0,0);
            }
        }

        // Hunting Rifle breech. This animated part opens while reloading.
        // Push pose so we can make do transformations without affecting the models above.
        poseStack.pushPose();
        // Apply the transformations
        if(isPlayer && isFirstPerson)
        {
            if(breechRotations!=Vec3.ZERO)
            {
                {
                    poseStack.translate(-breechRotOffset.x * 0.0625, breechRotOffset.y * 0.0625, breechRotOffset.z * 0.0625);
                    poseStack.mulPose(Axis.XP.rotationDegrees((float) breechRotations.x));
                    poseStack.translate(breechRotOffset.x * 0.0625, -breechRotOffset.y * 0.0625, -breechRotOffset.z * 0.0625);
                }
            }
        }
        // Render the model.
        RenderUtil.renderModel(SpecialModels.HUNTING_RIFLE_BREECH.getModel(), transformType, null, stack, parent, poseStack, buffer, light, overlay);
        // Pop pose to compile everything in the render matrix.
        poseStack.popPose();

    }
}
