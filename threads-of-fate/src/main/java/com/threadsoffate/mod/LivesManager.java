package com.threadsoffate.mod;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.util.UUIDUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks lives per player UUID as a single persistent, server-wide Data Attachment
 * (rather than a per-entity attachment). This is deliberate: a banned player has no
 * loaded ServerPlayer entity to attach data to, so lives need to live somewhere that
 * is readable/writable regardless of whether the player is currently online.
 */
public final class LivesManager {
	private LivesManager() {
	}

	public static final AttachmentType<Map<UUID, Integer>> LIVES = AttachmentRegistry.create(
			ThreadsOfFate.id("lives"),
			builder -> builder
					.initializer(HashMap::new)
					.persistent(Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.INT))
	);

	public static int getLives(MinecraftServer server, UUID uuid) {
		Map<UUID, Integer> map = server.globalAttachments().getAttachedOrElse(LIVES, new HashMap<>());
		return map.getOrDefault(uuid, ThreadsOfFate.STARTING_LIVES);
	}

	public static void setLives(MinecraftServer server, UUID uuid, int value) {
		int clamped = Math.max(0, Math.min(ThreadsOfFate.MAX_LIVES, value));
		Map<UUID, Integer> current = server.globalAttachments().getAttachedOrElse(LIVES, new HashMap<>());
		// Attachment values should be treated as immutable - copy before mutating.
		Map<UUID, Integer> updated = new HashMap<>(current);
		updated.put(uuid, clamped);
		server.globalAttachments().setAttached(LIVES, updated);
	}

	public static boolean isBanned(MinecraftServer server, GameProfile profile) {
		return server.getPlayerList().getBans().isBanned(profile);
	}

	public static void ban(MinecraftServer server, GameProfile profile, String reason) {
		UserBanList bans = server.getPlayerList().getBans();
		UserBanListEntry entry = new UserBanListEntry(
				profile,
				null,
				"Threads of Fate",
				null,
				reason
		);
		bans.add(entry);
	}

	public static void unban(MinecraftServer server, GameProfile profile) {
		server.getPlayerList().getBans().remove(profile);
	}
}
