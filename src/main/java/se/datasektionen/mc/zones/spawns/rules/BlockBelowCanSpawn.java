package se.datasektionen.mc.zones.spawns.rules;

import com.mojang.serialization.Codec;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import se.datasektionen.mc.zones.spawns.SpawnRuleRegistry;

public class BlockBelowCanSpawn implements SpawnRule {

	public static final BlockBelowCanSpawn INSTANCE = new BlockBelowCanSpawn();

	public static final Codec<BlockBelowCanSpawn> CODEC = Codec.unit(INSTANCE);

	private BlockBelowCanSpawn() {}

	@Override
	public SpawnRuleRegistry.SpawnRuleType<? extends BlockBelowCanSpawn> getType() {
		return SpawnRuleRegistry.BLOCK_BELOW_CAN_SPAWN;
	}

	@Override
	public boolean canSpawn(EntityType<?> entityType, WorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
		return world.getBlockState(pos.down()).allowsSpawning(world, pos, entityType);
	}
}
