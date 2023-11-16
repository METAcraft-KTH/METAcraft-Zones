package se.datasektionen.mc.zones.zone;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import se.datasektionen.mc.zones.METAcraftZones;
import se.datasektionen.mc.zones.ZoneManagementCommand;
import se.datasektionen.mc.zones.zone.types.*;

import java.util.function.Function;

public class ZoneRegistry {

	public static final Registry<ZoneType<?>> REGISTRY = FabricRegistryBuilder.<ZoneType<?>>createSimple(
			RegistryKey.ofRegistry(METAcraftZones.getID("zones"))
	).buildAndRegister();

	public static final ZoneType<BoxZone> box = register("box", BoxZone::getCodec, BoxZone::createCommand);
	public static final ZoneType<RegionZone> region = register("region", RegionZone::getCodec, RegionZone::createCommand);
	public static final ZoneType<SphereZone> sphere = register("sphere", world -> SphereZone.getCodec(
			world, SphereZone::new
	), (zoneCreator, ctx) -> SphereZone.createCommand(zoneCreator, ctx, SphereZone::new));
	public static final ZoneType<CircleZone> circle = register("circle", world -> CircleZone.getCodec(
			world, CircleZone::new
	), (zoneCreator, ctx) -> SphereZone.createCommand(zoneCreator, ctx, CircleZone::new));

	public static final ZoneType<BiomeZone> biome = register("biome", BiomeZone::getCodec, BiomeZone::createCommand);
	public static final ZoneType<UnionZone> union = register("union", world -> UnionZone.getCodec(world, UnionZone::new));
	public static final ZoneType<IntersectZone> intersect = register("intersect", world -> IntersectZone.getCodec(world, IntersectZone::new));
	public static final ZoneType<NegateZone> negate = register("negate", NegateZone::getCodec);


	public static void init() {
		//DO NOT REMOVE THIS!!! Necessary for things to load early enough.
	}

	private static <T extends se.datasektionen.mc.zones.zone.types.ZoneType> ZoneType<T> register(
			String name, Function<World, Codec<T>> codec
	) {
		return Registry.register(REGISTRY, new Identifier(name), new ZoneType<>(codec, null));
	}


	private static <T extends se.datasektionen.mc.zones.zone.types.ZoneType> ZoneType<T> register(
			String name, Function<World, Codec<T>> codec, ZoneCommandCreator commandCreator
	) {
		return Registry.register(REGISTRY, new Identifier(name), new ZoneType<>(codec, commandCreator));
	}

	private static <T extends se.datasektionen.mc.zones.zone.types.ZoneType> ZoneType<T> register(
			String name, Function<World, Codec<T>> codec, SimpleZoneCommandCreator commandCreator
	) {
		return register(name, codec, (argumentBuilder, registryAccess, addZone) -> commandCreator.createCommand(argumentBuilder, addZone));
	}

	@FunctionalInterface
	public interface ZoneCommandCreator {
		ArgumentBuilder<ServerCommandSource, ?> createCommand(
				ArgumentBuilder<ServerCommandSource, ?> argumentBuilder,
				CommandRegistryAccess registryAccess,
				ZoneManagementCommand.ZoneAdder addZone
		);
	}

	@FunctionalInterface
	public interface SimpleZoneCommandCreator {
		ArgumentBuilder<ServerCommandSource, ?> createCommand(
				ArgumentBuilder<ServerCommandSource, ?> argumentBuilder,
				ZoneManagementCommand.ZoneAdder addZone
		);
	}

	public record ZoneType<T extends se.datasektionen.mc.zones.zone.types.ZoneType>(
			Function<World, Codec<T>> codec,
			ZoneCommandCreator commandCreator
	) {}

}
