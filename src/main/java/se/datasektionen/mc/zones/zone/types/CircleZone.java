package se.datasektionen.mc.zones.zone.types;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.function.TriFunction;
import se.datasektionen.mc.zones.ZoneManagementCommand;
import se.datasektionen.mc.zones.zone.ZoneRegistry;

import static net.minecraft.server.command.CommandManager.argument;

public class CircleZone extends ZoneType {

	public static <T extends CircleZone> Codec<T> getCodec(World world, TriFunction<World, BlockPos, Double, T> creator) {
		return RecordCodecBuilder.create(instance -> instance.group(
				BlockPos.CODEC.fieldOf("center").forGetter(zone -> zone.center),
				Codec.DOUBLE.fieldOf("radius").forGetter(zone -> zone.radius)
		).apply(instance, (pos, radius) -> creator.apply(world, pos, radius)));
	}

	public static ArgumentBuilder<ServerCommandSource, ?> createCommand(
			ArgumentBuilder<ServerCommandSource, ?> builder, ZoneManagementCommand.ZoneAdder addZone,
			TriFunction<World, BlockPos, Double, ? extends CircleZone> creator
	) {
		return builder.then(
				argument("center", BlockPosArgumentType.blockPos()).then(
						argument("radius", DoubleArgumentType.doubleArg(0)).executes(ctx -> {
							return addZone.add(world -> creator.apply(
									world,
									BlockPosArgumentType.getBlockPos(ctx,"center"),
									DoubleArgumentType.getDouble(ctx,"radius")
							), ctx);
						})
				)
		);
	}

	protected BlockPos center;
	protected double radius;

	public CircleZone(World world, BlockPos center, double radius) {
		super(world);
		this.center = center;
		this.radius = radius;
	}

	@Override
	public boolean contains(BlockPos pos) {
		return center.getSquaredDistance(pos.getX(), center.getY(), pos.getZ()) < MathHelper.square(radius);
	}

	@Override
	public double getSize() {
		return radius * radius * Math.PI * world.getHeight();
	}

	@Override
	public ZoneType clone(World otherWorld) {
		return new CircleZone(otherWorld, center.toImmutable(), radius);
	}

	@Override
	public ZoneRegistry.ZoneType<? extends CircleZone> getType() {
		return ZoneRegistry.circle;
	}

	@Override
	public String toString() {
		return "Circle[" +
				"center:{x=" + center.getX() + ", y=" + center.getY() + ", z=" + center.getZ() + "}, " +
				"radius=" + radius +
		"]";
	}
}
