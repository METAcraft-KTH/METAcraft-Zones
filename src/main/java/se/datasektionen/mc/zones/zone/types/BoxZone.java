package se.datasektionen.mc.zones.zone.types;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import se.datasektionen.mc.zones.ZoneManagementCommand;
import se.datasektionen.mc.zones.zone.ZoneRegistry;

import static net.minecraft.server.command.CommandManager.argument;

public class BoxZone extends ZoneType {

	public static Codec<BoxZone> getCodec(World world) {
		return RecordCodecBuilder.create(instance -> instance.group(
				BlockBox.CODEC.fieldOf("box").forGetter(zone -> zone.box)
		).apply(instance, box -> new BoxZone(world, box)));
	}

	public static ArgumentBuilder<ServerCommandSource, ?> createCommand(
			ArgumentBuilder<ServerCommandSource, ?> builder, ZoneManagementCommand.ZoneAdder addZone
	) {
		return builder.then(
			argument("pos1", BlockPosArgumentType.blockPos()).then(
				argument("pos2", BlockPosArgumentType.blockPos()).executes(ctx -> {
					BlockPos pos1 = BlockPosArgumentType.getBlockPos(ctx,"pos1");
					BlockPos pos2 = BlockPosArgumentType.getBlockPos(ctx,"pos2");
					BlockBox box = new BlockBox(
							pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ()
					);
					return addZone.add(world -> new BoxZone(world, box), ctx);
				})
			)
		);
	}

	protected final BlockBox box;

	public BoxZone(World world, BlockBox box) {
		super(world);
		this.box = box;
	}

	@Override
	public boolean contains(BlockPos pos) {
		return box.contains(pos);
	}

	@Override
	public double getSize() {
		return (double) box.getBlockCountX() * box.getBlockCountY() * box.getBlockCountZ();
	}

	@Override
	public ZoneType clone(World otherWorld) {
		return new BoxZone(otherWorld, new BlockBox(
				box.getMinX(), box.getMinY(), box.getMinZ(),
				box.getMaxX(), box.getMaxY(), box.getMaxZ()
		));
	}

	@Override
	public ZoneRegistry.ZoneType<? extends BoxZone> getType() {
		return ZoneRegistry.box;
	}

	@Override
	public String toString() {
		return "Box[" +
				"from:{x=" + box.getMinX() + ", y=" + box.getMinY() + ", z=" + box.getMinZ() + "}, " +
				"to:{x=" + box.getMaxX() + ", y=" + box.getMaxY() + ", z=" + box.getMaxZ() + "}"  +
		"]";
	}
}
