package zaeonninezero.nzgmaddon.test;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.guns.client.render.gun.ModelOverrides;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import zaeonninezero.nzgmaddon.client.SpecialModels;
import zaeonninezero.nzgmaddon.common.CreativeGunVariantManager;
import zaeonninezero.nzgmaddon.init.initItems;

@EventBusSubscriber(modid = "nzgmaddon", value = Dist.CLIENT)
public final class ClientSmokeTests {
    private static int readyTicks;
    private static int totalTicks;
    private static boolean done;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("nzgmaddon.clientSmoke") || done) return;
        Minecraft mc = Minecraft.getInstance();
        if (++totalTicks > 2400) {
            org.slf4j.LoggerFactory.getLogger("NZGE-SmokeTest").error("Timed out waiting for test world; screen={}", mc.screen);
            done = true;
            mc.stop();
            return;
        }
        if (mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen && mc.getOverlay() == null) {
            mc.screen.onClose();
        }
        boolean worldTest = Boolean.getBoolean("nzgmaddon.smokeWorld");
        if ((worldTest ? mc.player == null : !(mc.screen instanceof TitleScreen)) || mc.getOverlay() != null || ++readyTicks < 30) return;
        done = true;
        try {
            var missing = mc.getModelManager().getMissingModel();
            for (SpecialModels model : SpecialModels.values()) {
                if (model.getModel() == missing) throw new AssertionError("Missing special model: " + model);
                // Open models may intentionally have no particle sprite; inspect the actual faces.
                var faces = new java.util.ArrayList<net.minecraft.core.Direction>();
                faces.add(null);
                faces.addAll(java.util.Arrays.asList(net.minecraft.core.Direction.values()));
                for (var face : faces) {
                    for (var quad : model.getModel().getQuads(null, face, net.minecraft.util.RandomSource.create(0))) {
                        if (quad.getSprite().contents().name().equals(net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation()))
                            throw new AssertionError("Missing face texture in special model: " + model);
                    }
                }
            }
            NonNullList<ItemStack> stacks = NonNullList.create();
            initItems.ITEMS.getEntries().forEach(item -> stacks.add(new ItemStack(item.get())));
            CreativeGunVariantManager.addItemVariants(stacks);
            if (worldTest && initItems.REVOLVER.get().getGun().getGeneral().getMaxAmmo() != 6)
                throw new AssertionError("Gun data did not sync to client");
            int rendered = 0;
            var buffers = mc.renderBuffers().bufferSource();
            for (ItemStack stack : stacks) {
                if (mc.getItemRenderer().getModel(stack, null, null, 0) == missing)
                    throw new AssertionError("Missing inventory model: " + stack);
                var override = ModelOverrides.getModel(stack);
                if (override == null) continue;
                if (worldTest) {
                    mc.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
                    mc.player.getCooldowns().addCooldown(stack.getItem(), 10);
                }
                for (ItemDisplayContext context : new ItemDisplayContext[]{ItemDisplayContext.GUI,
                        ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND}) {
                    PoseStack pose = new PoseStack();
                    override.render(0.5f, context, stack, ItemStack.EMPTY, mc.player, pose, buffers, 15728880, OverlayTexture.NO_OVERLAY);
                    if (!pose.clear()) throw new AssertionError("Unbalanced model pose stack: " + stack);
                    rendered++;
                }
            }
            buffers.endBatch();
            String result = "PASS: " + SpecialModels.values().length + " special models; " + stacks.size()
                    + " inventory stacks; " + rendered + " override render calls; player/world=" + worldTest + ".\n";
            java.nio.file.Files.writeString(java.nio.file.Path.of("../artifacts/client-smoke-result.txt"), result);
            org.slf4j.LoggerFactory.getLogger("NZGE-SmokeTest").info(result);
            mc.setScreen(new ModelGallery(stacks));
        } catch (Throwable error) {
            org.slf4j.LoggerFactory.getLogger("NZGE-SmokeTest").error("Client smoke test FAILED", error);
            try { java.nio.file.Files.writeString(java.nio.file.Path.of("../artifacts/client-smoke-result.txt"), "FAIL: " + error); }
            catch (java.io.IOException ignored) {}
            mc.stop();
        }
    }

    private static final class ModelGallery extends net.minecraft.client.gui.screens.Screen {
        private final java.util.List<ItemStack> stacks;
        private int frames;

        ModelGallery(java.util.List<ItemStack> stacks) {
            super(net.minecraft.network.chat.Component.literal("NZGE model validation"));
            this.stacks = java.util.List.copyOf(stacks);
        }

        @Override
        public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(0, 0, width, height, 0xFF161C24);
            graphics.drawString(font, "NZGE / 1.21.1 NeoForge / model validation", 10, 8, 0xFFFFFFFF);
            int columns = 8;
            int cellWidth = (width - 20) / columns;
            int cellHeight = (height - 35) / 7;
            for (int i = 0; i < stacks.size(); i++) {
                int x = 10 + (i % columns) * cellWidth;
                int y = 25 + (i / columns) * cellHeight;
                graphics.fill(x, y, x + cellWidth - 2, y + cellHeight - 2, 0xFF25303E);
                graphics.pose().pushPose();
                graphics.pose().translate(x + cellWidth / 2.0 - 16, y + 2, 0);
                graphics.pose().scale(2, 2, 2);
                graphics.renderItem(stacks.get(i), 0, 0);
                graphics.pose().popPose();
                graphics.drawString(font, Integer.toString(i + 1), x + 2, y + 2, 0xFF9EBDDA, false);
            }
            graphics.flush();
            if (++frames == 3) {
                try (var screenshot = net.minecraft.client.Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
                    screenshot.writeToFile(java.nio.file.Path.of("../artifacts/model-gallery.png"));
                } catch (java.io.IOException error) { throw new RuntimeException(error); }
                minecraft.stop();
            }
        }
    }
}
