package se.datasektionen.mc.zones.zone.types;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.command.CommandException;
import net.minecraft.command.argument.RegistryPredicateArgumentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import se.datasektionen.mc.zones.ZoneManagementCommand;
import se.datasektionen.mc.zones.zone.ZoneRegistry;

import static net.minecraft.server.command.CommandManager.argument;

public class BiomeZone extends ZoneType {

	private static final Codec<Either<RegistryEntry<Biome>, TagKey<Biome>>> BIOME_CODEC = Codec.either(
			RegistryElementCodec.of(RegistryKeys.BIOME, Biome.CODEC, false), TagKey.codec(RegistryKeys.BIOME)
	); //For some reason, the vanilla biome registry entry codec allows inline definitions, despite the fact that this breaks the game...

	protected Either<RegistryEntry<Biome>, TagKey<Biome>> biome;
	protected boolean alwaysCheckSourceDim;

	public static Codec<BiomeZone> getCodec(World world) {
		return RecordCodecBuilder.create(instance -> instance.group(
				BIOME_CODEC.fieldOf("biome").forGetter(zone -> zone.biome),
				Codec.BOOL.fieldOf("alwaysCheckSourceDim").forGetter(zone -> zone.alwaysCheckSourceDim)
		).apply(instance, (biome, alwaysCheckSourceDim) -> new BiomeZone(world, biome, alwaysCheckSourceDim)));
	}


	private static final DynamicCommandExceptionType BIOME_FAIL = new DynamicCommandExceptionType(id -> Text.literal(id + " is not a valid biome or tag!"));

	public static ArgumentBuilder<ServerCommandSource, ?> createCommand(
			ArgumentBuilder<ServerCommandSource, ?> builder, ZoneManagementCommand.ZoneAdder addZone
	) {
		return builder.then(
				argument("biome", RegistryPredicateArgumentType.registryPredicate(RegistryKeys.BIOME)).executes(ctx -> {
					return runCommand(ctx, false, addZone);
				}).then(
					argument("alwaysCheckSourceDim", BoolArgumentType.bool()).executes(ctx -> {
						return runCommand(ctx, BoolArgumentType.getBool(ctx, "alwaysCheckSourceDim"), addZone);
					})
				)
		);
	}

	private static int runCommand(CommandContext<ServerCommandSource> ctx, boolean alwaysCheckSourceDim, ZoneManagementCommand.ZoneAdder addZone) throws CommandSyntaxException {
		var biome = RegistryPredicateArgumentType.getPredicate(ctx, "biome", RegistryKeys.BIOME, BIOME_FAIL);
		return addZone.add(world -> new BiomeZone(
			world,
			biome.getKey().mapLeft(
				key -> ctx.getSource().getServer().getRegistryManager().get(RegistryKeys.BIOME)
						.getEntry(key).orElseThrow(() -> new CommandException(
								Text.literal(biome.asString() + " is not a valid biome!")
						))
			),
			alwaysCheckSourceDim
		), ctx);
	}

	public BiomeZone(World world, Either<RegistryEntry<Biome>, TagKey<Biome>> biome, boolean alwaysCheckSourceDim) {
		super(world);
		this.biome = biome;
		this.alwaysCheckSourceDim = alwaysCheckSourceDim;
	}

	@Override
	public boolean contains(BlockPos pos) {
		var actualBiome = world.getBiome(pos);
		return biome.map(
				biome -> actualBiome.getKeyOrValue().equals(biome.getKeyOrValue()),
				actualBiome::isIn
		);
	}

	@Override
	public double getSize() {
		double width = world.getWorldBorder().getSize();
		int biomeCount = world.getRegistryManager().get(RegistryKeys.BIOME).size();
		return width * width * world.getHeight() / (biomeCount * biomeCount * biomeCount);
	}

	@Override
	public ZoneType clone(World otherWorld) {
		if (alwaysCheckSourceDim) {
			return new BiomeZone(world, biome, true);
		} else {
			return new BiomeZone(otherWorld, biome, false);
		}
	}

	@Override
	public ZoneRegistry.ZoneType<?> getType() {
		return ZoneRegistry.biome;
	}

	@Override
	public String toString() {
		return "Biome[" + biome.map(entry -> entry.getKeyOrValue().map(
				key -> key.getValue().toString(),
				Object::toString
		), tag -> tag.id().toString()) + "]";
	}
}
