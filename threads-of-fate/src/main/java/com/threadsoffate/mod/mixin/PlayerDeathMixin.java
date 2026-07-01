package com.threadsoffate.mod.mixin;

import com.mojang.authlib.GameProfile;
import com.threadsoffate.mod.LivesManager;
import com.threadsoffate.mod.ThreadsOfFate;
import com.threadsoffate.mod.item.ThreadOfFateItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks the tail end of Player#die to run our lives logic. A mixin (rather than a
 * Fabric API event) is used here because there is no dedicated "player has died"
 * event in Fabric API - the closest, ServerLivingEntityEvents.ALLOW_DEATH, fires
 * BEFORE death resolves and is meant for cancelling it, not reacting after the fact.
 *
 * NOTE: if the method name/descriptor for Player#die has changed in your copy of
 * 26.1, update the `target` string below to match (your IDE's "Generate Sources"
 * step - see docs.fabricmc.net/develop/getting-started/generating-sources - will
 * show you the current signature).
 */
@Mixin(Player.class)
public abstract class PlayerDeathMixin {

	@Inject(method = "die", at = @At("TAIL"))
	private void threadsoffate$onDie(DamageSource damageSource, CallbackInfo ci) {
		Player self = (Player) (Object) this;

		if (self.level().isClientSide || !(self instanceof ServerPlayer player)) {
			return;
		}

		MinecraftServer server = player.getServer();
		if (server == null) {
			return;
		}

		ServerLevel level = player.serverLevel();
		int lives = LivesManager.getLives(server, player.getUUID());
		String name = player.getGameProfile().getName();

		if (lives > 1) {
			int remaining = lives - 1;
			LivesManager.setLives(server, player.getUUID(), remaining);

			ItemStack thread = ThreadOfFateItem.create(player.getUUID(), name);
			dropAt(level, player, thread);

			level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1, player.getZ(), 30, 0.4, 0.5, 0.4, 0.02);
			level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0f, 0.6f);

			server.getPlayerList().broadcastSystemMessage(
					Component.literal(name + "'s thread frays - " + remaining + " life" + (remaining == 1 ? "" : "s") + " remaining. Their Thread of Fate fell where they died.")
							.withStyle(ChatFormatting.RED),
					false
			);
		} else {
			LivesManager.setLives(server, player.getUUID(), 0);

			ItemStack head = new ItemStack(Items.PLAYER_HEAD);
			head.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
			dropAt(level, player, head);

			level.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1, player.getZ(), 50, 0.5, 0.7, 0.5, 0.05);
			level.playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 0.7f, 0.6f);

			server.getPlayerList().broadcastSystemMessage(
					Component.literal(name + "'s thread has run out. They are banned until someone revives them with their head.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
					false
			);

			GameProfile profile = player.getGameProfile();
			LivesManager.ban(server, profile, "Out of lives in Threads of Fate - find someone to revive you.");
			player.connection.disconnect(
					Component.literal("You have run out of lives! Someone must revive you with your head to return.")
			);
		}
	}

	private void dropAt(ServerLevel level, ServerPlayer player, ItemStack stack) {
		ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), stack);
		itemEntity.setDefaultPickUpDelay();
		level.addFreshEntity(itemEntity);
	}
}
