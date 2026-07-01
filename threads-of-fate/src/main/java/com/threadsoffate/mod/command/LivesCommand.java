package com.threadsoffate.mod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.threadsoffate.mod.LivesManager;
import com.threadsoffate.mod.ThreadsOfFate;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class LivesCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("lives")
						.executes(ctx -> {
							ServerPlayer player = ctx.getSource().getPlayerOrException();
							int lives = LivesManager.getLives(ctx.getSource().getServer(), player.getUUID());
							ctx.getSource().sendSuccess(
									() -> Component.literal("You have " + lives + "/" + ThreadsOfFate.MAX_LIVES + " lives.")
											.withStyle(ChatFormatting.LIGHT_PURPLE),
									false
							);
							return 1;
						})
						.then(Commands.argument("target", EntityArgument.player())
								.executes(ctx -> {
									ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
									int lives = LivesManager.getLives(ctx.getSource().getServer(), target.getUUID());
									ctx.getSource().sendSuccess(
											() -> Component.literal(target.getGameProfile().getName() + " has " + lives + "/" + ThreadsOfFate.MAX_LIVES + " lives.")
													.withStyle(ChatFormatting.LIGHT_PURPLE),
											false
									);
									return 1;
								})
						)
		);

		dispatcher.register(
				Commands.literal("threadsoffate")
						.requires(source -> source.hasPermission(2))
						.then(Commands.literal("set")
								.then(Commands.argument("target", EntityArgument.player())
										.then(Commands.argument("amount", IntegerArgumentType.integer(0, ThreadsOfFate.MAX_LIVES))
												.executes(ctx -> {
													ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
													int amount = IntegerArgumentType.getInteger(ctx, "amount");
													LivesManager.setLives(ctx.getSource().getServer(), target.getUUID(), amount);
													ctx.getSource().sendSuccess(
															() -> Component.literal("Set " + target.getGameProfile().getName() + "'s lives to " + amount)
																	.withStyle(ChatFormatting.GOLD),
															true
													);
													return 1;
												})
										)
								)
						)
		);
	}
}
