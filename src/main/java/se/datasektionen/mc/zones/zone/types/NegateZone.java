package se.datasektionen.mc.zones.zone.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import se.datasektionen.mc.zones.zone.ZoneRegistry;

public class NegateZone extends ZoneType {

	protected final ZoneType zone;

	public static Codec<NegateZone> getCodec(World world) {
		return RecordCodecBuilder.create(instance -> instance.group(
				getRegistryCodec(world).fieldOf("zone").forGetter(zone -> zone.zone)
		).apply(instance, (zone) -> new NegateZone(world, zone)));
	}

	public NegateZone(World world, ZoneType zone) {
		super(world);
		this.zone = zone;
	}

	@Override
	public boolean contains(BlockPos pos) {
		return !zone.contains(pos);
	}

	@Override
	public double getSize() {
		return zone.getSize();
	}

	@Override
	public ZoneType clone(World otherWorld) {
		return new NegateZone(otherWorld, zone.clone(otherWorld));
	}

	@Override
	public String toString() {
		return "Negate:[" + zone + "]";
	}

	@Override
	public ZoneRegistry.ZoneType<?> getType() {
		return ZoneRegistry.negate;
	}
}
