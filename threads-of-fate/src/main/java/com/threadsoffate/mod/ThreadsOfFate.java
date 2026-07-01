package com.threadsoffate.mod;

import com.threadsoffate.mod.command.LivesCommand;
import com.threadsoffate.mod.recipe.ReviveTotemRecipe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class ThreadsOfFate implements ModInitializer {
	public static final String MOD_ID = "threadsoffate";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Design constants - tweak these to change the game rules.
	public static final int STARTING_LIVES = 3;
	public static final int MAX_LIVES = 7;

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	private int tickCounter = 0;

	@Override
	public void onInitialize() {
		LOGGER.info("Threads of Fate is weaving...");

		ModItems.initialize();
		ReviveTotemRecipe.initialize();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				LivesCommand.register(dispatcher));

		// Every second, scan online players' inventories for their own Thread of Fate
		// so they can reclaim a life just by picking it up. Doing this on a tick event
		// (instead of an inventoryTick override) keeps us independent of exact Item
		// method signatures, which are still shifting in this Minecraft version.
		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
	}

	private void onServerTick(MinecraftServer server) {
		tickCounter++;
		if (tickCounter < 20) {
			return;
		}
		tickCounter = 0;

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			checkForThreadPickup(server, player);
		}
	}

	private void checkForThreadPickup(MinecraftServer server, ServerPlayer player) {
		// `items` is the classic main+hotbar inventory list on the Inventory class.
		// (If this field has been renamed in 26.1, your IDE's autocomplete on
		// player.getInventory(). will show the current name to swap in.)
		List<ItemStack> items = player.getInventory().items;

		for (ItemStack stack : items) {
			if (stack.isEmpty() || !stack.is(ModItems.THREAD_OF_FATE)) {
				continue;
			}

			CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
			if (!data.contains("OwnerUUID")) {
				continue;
			}

			UUID owner = data.copyTag().getUUID("OwnerUUID");
			if (!owner.equals(player.getUUID())) {
				continue;
			}

			int lives = LivesManager.getLives(server, owner);
			if (lives >= MAX_LIVES) {
				continue;
			}

			LivesManager.setLives(server, owner, lives + 1);
			stack.shrink(1);

			ServerLevel level = player.serverLevel();
			level.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1, player.getZ(), 20, 0.4, 0.5, 0.4, 0.02);
			level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.6f, 1.4f);

			player.displayClientMessage(
					net.minecraft.network.chat.Component.literal(
							"Your Thread of Fate unravels back into you. Lives: " + (lives + 1) + "/" + MAX_LIVES
					).withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE),
					true
			);
		}
	}
}
