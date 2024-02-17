package se.datasektionen.mc.zones.spawns.rules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import se.datasektionen.mc.zones.spawns.SpawnRuleRegistry;

public class HasSkyAccess implements SpawnRule {

	public static final Codec<HasSkyAccess> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Heightmap.Type.CODEC.fieldOf("heightmap").forGetter(type -> type.heightmap)
			).apply(instance, HasSkyAccess::new)
	);

	private final Heightmap.Type heightmap;

	public HasSkyAccess(Heightmap.Type heightmap) {
		this.heightmap = heightmap;
	}

	@Override
	public SpawnRuleRegistry.SpawnRuleType<? extends SpawnRule> getType() {
		return SpawnRuleRegistry.HAS_SKY_ACCESS;
	}

	@Override
	public boolean canSpawn(EntityType<?> entityType, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
		return world.getTopY(heightmap, pos.getX(), pos.getZ()) == pos.getY();
	}
}
