package cy.jdkdigital.productivebees.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cy.jdkdigital.productivebees.ProductiveBees;
import cy.jdkdigital.productivebees.integrations.jei.ProduciveBeesJeiPlugin;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class AdvancedBeehiveRecipe implements IRecipe<IInventory>, IProductiveBeesRecipe {

    public static final IRecipeType<AdvancedBeehiveRecipe> ADVANCED_BEEHIVE = IRecipeType.register(ProductiveBees.MODID + ":advanced_beehive");

    public final ResourceLocation id;
    public final ProduciveBeesJeiPlugin.BeeIngredient ingredient;
    public final List<ItemStack> outputs;
    public final double chance;

    public AdvancedBeehiveRecipe(ResourceLocation id, ProduciveBeesJeiPlugin.BeeIngredient ingredient, List<ItemStack> outputs, double chance) {
        this.id = id;
        this.ingredient = ingredient;
        this.outputs = outputs;
        this.chance = chance;
    }

    @Override
    public String toString() {
        return "AdvancedBeehiveRecipe{" +
                "id=" + id +
                ", bee=" + ingredient.getBeeType() +
                ", outputs=" + outputs +
                ", chance=" + chance +
                '}';
    }

    @Override
    public boolean matches(IInventory inv, World worldIn) {
        ProductiveBees.LOGGER.info("Comparing recipe: " + inv + " - " + this.ingredient);
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(IInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        return this.outputs.get(0);
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return new Serializer<>(AdvancedBeehiveRecipe::new);
    }

    @Nonnull
    @Override
    public IRecipeType<?> getType() {
        return ADVANCED_BEEHIVE;
    }

    public static class Serializer<T extends AdvancedBeehiveRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {
        final IRecipeFactory<T> factory;

        public Serializer(Serializer.IRecipeFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        public T read(ResourceLocation id, JsonObject json) {
            ProductiveBees.LOGGER.warn("deserializing using json " + json);
            ProduciveBeesJeiPlugin.BeeIngredient ingredient = null;
            List<ItemStack> outputs = new ArrayList<>();

            int chance = JSONUtils.getInt(json,"chance");

            return this.factory.create(id, ingredient, outputs, chance);
        }

        public T read(@Nonnull ResourceLocation id, @Nonnull PacketBuffer buffer) {
            ProduciveBeesJeiPlugin.BeeIngredient ingredient = ProduciveBeesJeiPlugin.BeeIngredient.read(buffer);
            List<ItemStack> outputs = new ArrayList<>();
            IntStream.range(0, buffer.readInt()).forEach(i -> outputs.add(buffer.readItemStack()));
            double chance = buffer.readDouble();
            return this.factory.create(id, ingredient, outputs, chance);
        }

        public void write(@Nonnull PacketBuffer buffer, T recipe) {
            recipe.ingredient.write(buffer);
            buffer.writeInt(recipe.outputs.size());
            recipe.outputs.forEach(buffer::writeItemStack);
            buffer.writeDouble(recipe.chance);
        }

        public interface IRecipeFactory<T extends AdvancedBeehiveRecipe> {
            T create(ResourceLocation id, ProduciveBeesJeiPlugin.BeeIngredient input, List<ItemStack> stacks, double chance);
        }
    }
}
