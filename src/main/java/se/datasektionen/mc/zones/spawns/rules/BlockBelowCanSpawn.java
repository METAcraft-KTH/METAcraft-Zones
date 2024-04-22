package se.datasektionen.mc.zones.spawns.rules;

import com.mojang.serialization.MapCodec;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import se.datasektionen.mc.zones.spawns.SpawnRuleRegistry;

public class BlockBelowCanSpawn implements SpawnRule {

	public static final BlockBelowCanSpawn INSTANCE = new BlockBelowCanSpawn();

	public static final MapCodec<BlockBelowCanSpawn> CODEC = MapCodec.unit(INSTANCE);

	private BlockBelowCanSpawn() {}

	@Override
	public SpawnRuleRegistry.SpawnRuleType<? extends BlockBelowCanSpawn> getType() {
		return SpawnRuleRegistry.BLOCK_BELOW_CAN_SPAWN;
	}

	@Override
	public boolean canSpawn(EntityType<?> entityType, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
		return world.getBlockState(pos.down()).allowsSpawning(world, pos, entityType);
	}
}
