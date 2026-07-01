package com.threadsoffate.mod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.tooltip.TooltipDisplay;

import java.util.function.Consumer;

/**
 * Represents one of a player's lost lives. Stores the owner's UUID + name in
 * DataComponents.CUSTOM_DATA so ThreadsOfFate's tick loop can recognise it and
 * hand the life back if the *same* player picks it up. Anyone else can grab
 * it just to deny them the life, or hold onto it.
 */
public class ThreadOfFateItem extends Item {
	public ThreadOfFateItem(Properties properties) {
		super(properties);
	}

	public static ItemStack create(java.util.UUID ownerUuid, String ownerName) {
		ItemStack stack = new ItemStack(com.threadsoffate.mod.ModItems.THREAD_OF_FATE);
		net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
		tag.putUUID("OwnerUUID", ownerUuid);
		tag.putString("OwnerName", ownerName);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return stack;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
		CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
		if (data.contains("OwnerName")) {
			String owner = data.copyTag().getString("OwnerName");
			textConsumer.accept(Component.literal("A lost life of " + owner).withStyle(ChatFormatting.LIGHT_PURPLE));
			textConsumer.accept(Component.literal("Only they can reclaim it by picking it up.").withStyle(ChatFormatting.GRAY));
		}
	}
}
