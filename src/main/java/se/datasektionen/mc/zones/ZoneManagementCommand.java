package se.datasektionen.mc.zones;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import se.datasektionen.mc.zones.zone.RealZone;
import se.datasektionen.mc.zones.zone.ZoneRegistry;
import se.datasektionen.mc.zones.zone.data.MessageZoneData;
import se.datasektionen.mc.zones.zone.data.ZoneDataRegistry;
import se.datasektionen.mc.zones.zone.types.IntersectZone;
import se.datasektionen.mc.zones.zone.types.NegateZone;
import se.datasektionen.mc.zones.zone.types.UnionZone;
import se.datasektionen.mc.zones.zone.types.ZoneType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ZoneManagementCommand {

	public static final String NAME = "name";

	static void registerCommand(LiteralArgumentBuilder<ServerCommandSource> builder, CommandRegistryAccess registryAccess) {
		builder.then(
			literal("create").then(
				createCreateCommandsFromRegistry(argument(NAME, StringArgumentType.string()), registryAccess)
			)
		).then(
			literal("extend").then(
				getZoneCreator(zone(), registryAccess, (zoneCreator, ctx) -> {
					var zone = getZone(ctx);
					var newZone = zoneCreator.get();
					if (zone.getZone() instanceof UnionZone union) {
						union.addZone(newZone);
					} else {
						ZoneType prevZone = zone.getZone();
						zone.setZone(new UnionZone(prevZone, newZone));
					}
					ctx.getSource().sendFeedback(
							() -> Text.literal("Expanded zone " + zone.getName() + " to include " + newZone),
							true
					);
					return 1;
				})
			)
		).then(
			literal("restrict-to").then(
				getZoneCreator(zone(), registryAccess, (zoneCreator, ctx) -> {
					var zone = getZone(ctx);
					var newZone = zoneCreator.get();
					if (zone.getZone() instanceof IntersectZone intersect) {
						intersect.addZone(newZone);
					} else {
						ZoneType prevZone = zone.getZone();
						zone.setZone(new IntersectZone(prevZone, newZone));
					}
					ctx.getSource().sendFeedback(
							() -> Text.literal("Limited zone " + zone.getName() + " to " + newZone),
							true
					);
					return 1;
				})
			)
		).then(
			literal("negate").then(
				zone().executes(ctx -> {
					var zone = getZone(ctx);
					if (zone.getZone() instanceof NegateZone negate) {
						zone.setZone(negate.getZone());
					} else {
						zone.setZone(new NegateZone(zone.getZone()));
					}
					ctx.getSource().sendFeedback(() -> Text.literal("Negated " + zone.getName()), true);
					return 1;
				})
			)
		).then(
			literal("set-priority").then(
				zone().then(
					argument("priority", IntegerArgumentType.integer()).executes(ctx -> {
						RealZone zone = getZone(ctx);
						int priority = IntegerArgumentType.getInteger(ctx, "priority");
						zone.setPriority(priority);
						getSettings(ctx).updatePriority(zone);
						ctx.getSource().sendFeedback(
								() -> Text.literal("Set zone priority for " + zone.getName() + " to " + priority),
								true
						);
						return 1;
					})
				)
			)
		).then(
			ZoneCommandUtils.queryZoneMulti(literal("get"), zone -> {
				return ImmutableList.of(
					Text.literal("Name: " + zone.getName()),
					Text.literal("Dimension: " + zone.getDim().getValue()),
					Text.literal("Zone: " + zone.getZone()),
					Text.literal("Priority: " + zone.getPriority()),
					Text.literal("AdditionalDimensions: " + zone.getRemoteZones().stream().map(type -> {
						return type.getDim().getValue().toString();
					}).collect(Collectors.joining(", "))),
					Text.literal("Data: ").append(
						join(zone.getAllData().stream().map(data -> Text.literal(" ").append(data.toText())).iterator(), Text.literal(",\n"))
					)
				);
			})
		).then(
			literal("dimension").then(
				addRemoveSubDim(literal("add"), true)
			).then(
				addRemoveSubDim(literal("remove"), false)
			)
		).then(
			literal("remove").then(
				zone().executes(ctx -> {
					String name = StringArgumentType.getString(ctx, NAME);
					if (getSettings(ctx).removeZone(name)) {
						ctx.getSource().sendFeedback(
							() -> Text.literal("Removed " + name),
							true
						);
						return 1;
					} else {
						ctx.getSource().sendFeedback(
							() -> Text.literal("No zone with " + name + " exists!"),
							false
						);
						return 0;
					}
				})
			)
		).then(
			literal("list").executes(ctx -> {
				if (getSettings(ctx).getZoneNames().isEmpty()) {
					ctx.getSource().sendFeedback(() -> Text.literal("There are no zones"), false);
				}
				getSettings(ctx).getZoneNames().forEach(name -> {
					ctx.getSource().sendFeedback(() -> Text.literal(name), false);
				});
				return 1;
			})
		).then(
			messageCommand("entry-message", MessageZoneData::getEnterMessage, MessageZoneData::setEnterMessage)
		).then(
			messageCommand("exit-message", MessageZoneData::getLeaveMessage, MessageZoneData::setLeaveMessage)
		);
	}

	static Text join(Iterator<? extends Text> text, Text delimiter) {
		MutableText full = Text.empty();
		if (text.hasNext()) {
			full.append(text.next());
		}
		text.forEachRemaining(part -> {
			full.append(delimiter).append(part);
		});
		return full;
	}

	static ArgumentBuilder<ServerCommandSource, ?> messageCommand(
			String name,
			Function<MessageZoneData, Optional<Text>> messageGetter,
			BiConsumer<MessageZoneData, Optional<Text>> messageSetter
	) {
		return literal(name).then(
			literal("get").then(
				zone().executes(ctx -> {
					var opt = getZone(ctx).get(ZoneDataRegistry.MESSAGE).flatMap(messageGetter);
					opt.ifPresentOrElse(text -> {
						ctx.getSource().sendFeedback(() -> Text.literal("Message: ").append(text), false);
					}, () -> {
						ctx.getSource().sendFeedback(() -> Text.literal("No message set for this zone"), false);
					});
					return opt.isPresent() ? 1 : 0;
				})
			)
		).then(
			literal("set").then(
				zone().then(
					argument("text", TextArgumentType.text()).executes(ctx -> {
						var text = TextArgumentType.getTextArgument(ctx,"text");
						var zone = getZone(ctx);
						messageSetter.accept(
							zone.getOrCreate(ZoneDataRegistry.MESSAGE),
							Optional.of(text)
						);
						ctx.getSource().sendFeedback(
								() -> Text.literal("Set " + name + " in zone " + zone.getName() + " to ").append(text),
								true
						);
						return 1;
					})
				)
			)
		).then(
			literal("remove").then(
				zone().executes(ctx -> {
					var zone = getZone(ctx);
					messageSetter.accept(
							zone.getOrCreate(ZoneDataRegistry.MESSAGE),
						Optional.empty()
					);
					ctx.getSource().sendFeedback(
							() -> Text.literal("Removed " + name + " from " + zone.getName()),
							true
					);
					return 1;
				})
			)
		);
	}

	static ArgumentBuilder<ServerCommandSource, ?> addRemoveSubDim(LiteralArgumentBuilder<ServerCommandSource> name, boolean add) {
		return name.then(
			zone().then(
				argument("dim", DimensionArgumentType.dimension()).executes(ctx -> {
					ServerWorld dim = DimensionArgumentType.getDimensionArgument(ctx, "dim");
					var dimKey = dim.getRegistryKey();
					RealZone zone = getZone(ctx);
					if (add) {
						if (zone.getDim() == dimKey || zone.hasRemoteZone(dimKey)) {
							ctx.getSource().sendFeedback(() -> Text.literal(
									"Dimension already covered!"
							), false);
						} else {
							zone.addRemoteDimension(dim);
							ctx.getSource().sendFeedback(() -> Text.literal(
									"Added dimension " + dimKey.getValue() + " to " + zone.getName()
							), true);
						}
					} else {
						if (zone.getDim() == dimKey) {
							ctx.getSource().sendFeedback(() -> Text.literal(
											"Cannot remove source dimension, delete the zone instead!"
									), false
							);
						} else if (zone.hasRemoteZone(dimKey)) {
							zone.removeRemoteDimension(dim);
							ctx.getSource().sendFeedback(() -> Text.literal(
									"Removed dimension " + dimKey.getValue() + " from " + zone.getName()
							), true);
						} else {
							ctx.getSource().sendFeedback(() -> Text.literal(
									"Dimension not covered!"
							), false);
						}
					}
					return 1;
				})
			)
		);
	}

	static RealZone getZone(CommandContext<ServerCommandSource> ctx) throws CommandException {
		return ZoneCommandUtils.getZone(ctx, NAME);
	}

	static RequiredArgumentBuilder<ServerCommandSource, ?> zone() {
		return ZoneCommandUtils.zone(NAME);
	}

	static ArgumentBuilder<ServerCommandSource, ?> createCreateCommandsFromRegistry(
			ArgumentBuilder<ServerCommandSource, ?> builder, CommandRegistryAccess registryAccess
	) {
		return getZoneCreator(builder, registryAccess, (zoneCreator, ctx) -> {
			World world = ctx.getSource().getWorld();
			ZoneType zone = zoneCreator.get();
			String name = StringArgumentType.getString(ctx, NAME);
			if (getSettings(ctx).containsZone(name)) {
				throw new CommandException(Text.literal("Another zone already exists with that name."));
			}
			RealZone container = new RealZone(
					name, world, zone, new HashMap<>(), 0, getSettings(ctx)::markDirty
			);
			zone.setZoneRef(container);
			getSettings(ctx).addZone(container);
			ctx.getSource().sendFeedback(
					() -> Text.literal("Added " + container.getName()), true
			);
			return 1;
		});
	}

	static ArgumentBuilder<ServerCommandSource, ?> getZoneCreator(
			ArgumentBuilder<ServerCommandSource, ?> builder, CommandRegistryAccess registryAccess, ZoneAdder zoneAdder
	) {
		ZoneRegistry.REGISTRY.streamEntries().forEach(entry -> {
			if(entry.value().commandCreator() != null) {
				builder.then(
						entry.value().commandCreator().createCommand(
								literal(Commands.getIDAsString(entry.registryKey().getValue())), registryAccess, zoneAdder
						)
				);
			}
		});
		return builder;
	}

	@FunctionalInterface
	public interface ZoneAdder {
		int add(Supplier<ZoneType> zoneCreator, CommandContext<ServerCommandSource> ctx);
	}

	public static ZoneManager getSettings(CommandContext<ServerCommandSource> ctx) {
		return ZoneManager.getInstance(ctx.getSource().getServer());
	}

}
