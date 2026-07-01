package com.threadsoffate.mod.recipe;

import com.mojang.serialization.MapCodec;
import com.threadsoffate.mod.ModItems;
import com.threadsoffate.mod.ThreadsOfFate;
import com.threadsoffate.mod.item.ReviveTotemItem;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * The crafting-table recipe:
 * <pre>
 * Totem   Diamond Totem
 * Diamond Head    Diamond
 * Totem   Diamond Totem
 * </pre>
 * The output Revive Totem is stamped with whichever player's head was used, so it can
 * only ever revive that one person. This is registered under the vanilla RecipeType.CRAFTING
 * so it works in any normal 3x3 crafting table/inventory grid, with a custom serializer so
 * we can read the head's owner in assemble().
 */
public class ReviveTotemRecipe implements Recipe<CraftingInput> {
	public static final MapCodec<ReviveTotemRecipe> CODEC = MapCodec.unit(ReviveTotemRecipe::new);
	public static final StreamCodec<RegistryFriendlyByteBuf, ReviveTotemRecipe> STREAM_CODEC =
			StreamCodec.unit(new ReviveTotemRecipe());

	public static RecipeSerializer<ReviveTotemRecipe> SERIALIZER;

	public ReviveTotemRecipe() {
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		if (input.width() != 3 || input.height() != 3) {
			return false;
		}

		int[] totemSlots = {0, 2, 6, 8};
		int[] diamondSlots = {1, 3, 5, 7};
		int headSlot = 4;

		if (!input.getItem(headSlot).is(Items.PLAYER_HEAD)) {
			return false;
		}
		for (int slot : totemSlots) {
			if (!input.getItem(slot).is(Items.TOTEM_OF_UNDYING)) {
				return false;
			}
		}
		for (int slot : diamondSlots) {
			if (!input.getItem(slot).is(Items.DIAMOND)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack head = input.getItem(4);
		ResolvableProfile profile = head.get(DataComponents.PROFILE);

		UUID targetUuid = new UUID(0L, 0L);
		String targetName = "Unknown";

		if (profile != null) {
			if (profile.id().isPresent()) {
				targetUuid = profile.id().get();
			}
			if (profile.name().isPresent()) {
				targetName = profile.name().get();
			}
		}

		return ReviveTotemItem.create(targetUuid, targetName);
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width >= 3 && height >= 3;
	}

	@Override
	public ItemStack getResultItem() {
		return new ItemStack(ModItems.REVIVE_TOTEM);
	}

	@Override
	public RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public RecipeType<? extends Recipe<CraftingInput>> getType() {
		return RecipeType.CRAFTING;
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return null;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public boolean showNotification() {
		return true;
	}

	@Override
	public String group() {
		return "threadsoffate";
	}

	public static void initialize() {
		SERIALIZER = Registry.register(
				BuiltInRegistries.RECIPE_SERIALIZER,
				ThreadsOfFate.id("revive_totem"),
				new RecipeSerializer<>(CODEC, STREAM_CODEC)
		);
	}
}
