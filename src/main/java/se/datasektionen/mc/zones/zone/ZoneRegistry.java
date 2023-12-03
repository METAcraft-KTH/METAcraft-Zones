package se.datasektionen.mc.zones.zone;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import se.datasektionen.mc.zones.METAcraftZones;
import se.datasektionen.mc.zones.ZoneManagementCommand;
import se.datasektionen.mc.zones.zone.types.*;

public class ZoneRegistry {

	public static final Registry<ZoneType<?>> REGISTRY = FabricRegistryBuilder.<ZoneType<?>>createSimple(
			RegistryKey.ofRegistry(METAcraftZones.getID("zones"))
	).buildAndRegister();

	public static final ZoneType<BoxZone> box = register("box", BoxZone.CODEC, BoxZone::createCommand);
	public static final ZoneType<RegionZone> region = register("region", RegionZone.CODEC, RegionZone::createCommand);
	public static final ZoneType<SphereZone> sphere = register("sphere", SphereZone.getCodec(
			SphereZone::new
	), (zoneCreator, ctx) -> SphereZone.createCommand(zoneCreator, ctx, SphereZone::new));
	public static final ZoneType<CircleZone> circle = register("circle", CircleZone.getCodec(
			CircleZone::new
	), (zoneCreator, ctx) -> SphereZone.createCommand(zoneCreator, ctx, CircleZone::new));

	public static final ZoneType<BiomeZone> biome = register("biome", BiomeZone.CODEC, BiomeZone::createCommand);
	public static final ZoneType<UnionZone> union = register("union", UnionZone.getCodec(UnionZone::new));
	public static final ZoneType<IntersectZone> intersect = register("intersect", IntersectZone.getCodec(IntersectZone::new));
	public static final ZoneType<NegateZone> negate = register("negate", NegateZone.CODEC);
	public static final ZoneType<ZoneZone> zone = register("zone", ZoneZone.CODEC, ZoneZone::createCommand);


	public static void init() {
		//DO NOT REMOVE THIS!!! Necessary for things to load early enough.
	}

	private static <T extends se.datasektionen.mc.zones.zone.types.ZoneType> ZoneType<T> register(
			String name, Codec<T> codec
	) {
		return Registry.register(REGISTRY, new Identifier(name), new ZoneType<>(codec, null));
	}


	private static <T extends se.datasektionen.mc.zones.zone.types.ZoneType> ZoneType<T> register(
			String name, Codec<T> codec, ZoneCommandCreator commandCreator
	) {
		return Registry.register(REGISTRY, new Identifier(name), new ZoneType<>(codec, commandCreator));
	}

	private static <T extends se.datasektionen.mc.zones.zone.types.ZoneType> ZoneType<T> register(
			String name, Codec<T> codec, SimpleZoneCommandCreator commandCreator
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
			Codec<T> codec,
			ZoneCommandCreator commandCreator
	) {}

}
