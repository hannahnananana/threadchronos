package com.threadsoffate.mod.item;

import com.mojang.authlib.GameProfile;
import com.threadsoffate.mod.LivesManager;
import com.threadsoffate.mod.ThreadsOfFate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.tooltip.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Crafted from Totem of Undying x4, Diamond x4 and the dead player's Player Head
 * (see ReviveTotemRecipe). Right click to spend it and revive whoever's head was used:
 * they are unbanned and restored to 1 life.
 */
public class ReviveTotemItem extends Item {
	public ReviveTotemItem(Properties properties) {
		super(properties);
	}

	public static ItemStack create(UUID targetUuid, String targetName) {
		ItemStack stack = new ItemStack(com.threadsoffate.mod.ModItems.REVIVE_TOTEM);
		CompoundTag tag = new CompoundTag();
		tag.putUUID("TargetUUID", targetUuid);
		tag.putString("TargetName", targetName);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return stack;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}

		if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer)) {
			return InteractionResult.PASS;
		}

		CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
		if (!data.contains("TargetUUID")) {
			return InteractionResult.FAIL;
		}

		CompoundTag tag = data.copyTag();
		UUID targetUuid = tag.getUUID("TargetUUID");
		String targetName = tag.getString("TargetName");

		MinecraftServer server = serverLevel.getServer();
		GameProfile profile = new GameProfile(targetUuid, targetName);

		if (!LivesManager.isBanned(server, profile)) {
			player.displayClientMessage(
					Component.literal(targetName + "'s thread has already been rewoven - they are not banned.")
							.withStyle(ChatFormatting.GRAY),
					true
			);
			return InteractionResult.FAIL;
		}

		LivesManager.unban(server, profile);
		LivesManager.setLives(server, targetUuid, 1);

		stack.shrink(1);

		serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1, player.getZ(), 60, 0.6, 0.8, 0.6, 0.3);
		serverLevel.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 1.0f);

		server.getPlayerList().broadcastSystemMessage(
				Component.literal(targetName + " has been revived by " + player.getName().getString() + "! Their thread is whole again.")
						.withStyle(ChatFormatting.GOLD),
				false
		);

		return InteractionResult.CONSUME;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
		CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
		if (data.contains("TargetName")) {
			String target = data.copyTag().getString("TargetName");
			textConsumer.accept(Component.literal("Revives " + target).withStyle(ChatFormatting.LIGHT_PURPLE));
			textConsumer.accept(Component.literal("Right click to weave their thread anew.").withStyle(ChatFormatting.GRAY));
		}
	}
}
