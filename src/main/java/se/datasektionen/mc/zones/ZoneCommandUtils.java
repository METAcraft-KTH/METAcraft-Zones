package se.datasektionen.mc.zones;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import se.datasektionen.mc.zones.zone.RealZone;

import java.util.function.Function;

import static net.minecraft.server.command.CommandManager.argument;

public class ZoneCommandUtils {
	public static final SuggestionProvider<ServerCommandSource> ZONE_NAME_SUGGESTIONS = (ctx, suggestionsBuilder) -> {
		ZoneManager.getInstance(ctx.getSource().getServer()).getZoneNames().forEach(suggestionsBuilder::suggest);
		return suggestionsBuilder.buildFuture();
	};
	public static RealZone getZone(CommandContext<ServerCommandSource> ctx, String arg) throws CommandException {
		String name = StringArgumentType.getString(ctx, arg);
		if (!ZoneManager.getInstance(ctx.getSource().getServer()).containsZone(name)) {
			throw new CommandException(Text.literal("No zone exists with that name"));
		}
		return ZoneManager.getInstance(ctx.getSource().getServer()).getZone(name);
	}

	public static ArgumentBuilder<ServerCommandSource, ?> queryZoneMulti(
			LiteralArgumentBuilder<ServerCommandSource> commandName,
			Function<RealZone, Iterable<? extends Text>> printer
	) {
		return commandName.then(
				zone("zone").executes(ctx -> {
					RealZone zone = getZone(ctx, "zone");
					for (Text text : printer.apply(zone)) {
						ctx.getSource().sendFeedback(() -> text, false);
					}
					return 1;
				})
		);
	}

	public static RequiredArgumentBuilder<ServerCommandSource, ?> zone(String arg) {
		return argument(arg, StringArgumentType.string()).suggests(ZONE_NAME_SUGGESTIONS);
	}

}
