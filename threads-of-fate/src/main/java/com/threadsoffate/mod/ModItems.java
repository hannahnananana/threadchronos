package com.threadsoffate.mod;

import com.threadsoffate.mod.item.ReviveTotemItem;
import com.threadsoffate.mod.item.ThreadOfFateItem;
import net.fabricmc.fabric.api.itemgroup.v1.CreativeModeTabEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class ModItems {
	public static final Item THREAD_OF_FATE = register(
			"thread_of_fate",
			ThreadOfFateItem::new,
			new Item.Properties().stacksTo(1)
	);

	public static final Item REVIVE_TOTEM = register(
			"revive_totem",
			ReviveTotemItem::new,
			new Item.Properties().stacksTo(1)
	);

	public static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties settings) {
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, ThreadsOfFate.id(name));
		T item = itemFactory.apply(settings.setId(itemKey));
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);
		return item;
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(entries -> {
			entries.accept(THREAD_OF_FATE);
			entries.accept(REVIVE_TOTEM);
		});
	}
}
