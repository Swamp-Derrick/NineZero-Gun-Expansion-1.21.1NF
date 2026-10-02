package zaeonninezero.nzgmaddon.crafting;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrcrayfish.guns.crafting.WorkbenchIngredient;
import com.mrcrayfish.guns.crafting.WorkbenchRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import zaeonninezero.nzgmaddon.nzgmAddon;

/** Keeps the recipe identity and item components through CGM's workbench and network. */
public final class VariantWorkbenchRecipe extends WorkbenchRecipe {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, nzgmAddon.MOD_ID);
    public static final DeferredHolder<RecipeSerializer<?>, Serializer> SERIALIZER =
            SERIALIZERS.register("workbench", Serializer::new);

    public VariantWorkbenchRecipe(ResourceLocation id, ItemStack item, ImmutableList<WorkbenchIngredient> materials) {
        super(id, item, materials);
    }

    @Override
    public RecipeSerializer<?> getSerializer() { return SERIALIZER.get(); }

    public static final class Serializer implements RecipeSerializer<VariantWorkbenchRecipe> {
        private static final Codec<WorkbenchIngredient> INGREDIENT = Codec.PASSTHROUGH.comapFlatMap(value -> {
            try {
                WorkbenchIngredient ingredient = WorkbenchIngredient.fromJson(value.convert(JsonOps.INSTANCE).getValue().getAsJsonObject());
                if (ingredient.getCount() <= 0) return DataResult.error(() -> "Material count must be positive");
                return DataResult.success(ingredient);
            } catch (RuntimeException error) {
                return DataResult.error(() -> "Invalid workbench material: " + error.getMessage());
            }
        }, ingredient -> new Dynamic<>(JsonOps.INSTANCE, ingredient.toJson()));

        public static final MapCodec<VariantWorkbenchRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("recipe_id").forGetter(VariantWorkbenchRecipe::getId),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(VariantWorkbenchRecipe::getItem),
                INGREDIENT.listOf().fieldOf("materials").forGetter(VariantWorkbenchRecipe::getMaterials)
        ).apply(instance, (id, stack, materials) -> new VariantWorkbenchRecipe(id, stack, ImmutableList.copyOf(materials))));

        public static final StreamCodec<RegistryFriendlyByteBuf, VariantWorkbenchRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public VariantWorkbenchRecipe decode(RegistryFriendlyByteBuf buffer) {
                ResourceLocation id = buffer.readResourceLocation();
                ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
                int count = buffer.readVarInt();
                if (count < 1 || count > 256) throw new IllegalArgumentException("Invalid material count: " + count);
                var materials = ImmutableList.<WorkbenchIngredient>builder();
                for (int i = 0; i < count; i++) {
                    materials.add(INGREDIENT.parse(JsonOps.INSTANCE, JsonParser.parseString(buffer.readUtf())).getOrThrow());
                }
                return new VariantWorkbenchRecipe(id, result, materials.build());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, VariantWorkbenchRecipe recipe) {
                buffer.writeResourceLocation(recipe.getId());
                ItemStack.STREAM_CODEC.encode(buffer, recipe.getItem());
                buffer.writeVarInt(recipe.getMaterials().size());
                recipe.getMaterials().forEach(material -> buffer.writeUtf(material.toJson().toString()));
            }
        };

        @Override
        public MapCodec<VariantWorkbenchRecipe> codec() { return CODEC; }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, VariantWorkbenchRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
