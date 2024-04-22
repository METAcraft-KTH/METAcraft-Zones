package se.datasektionen.mc.zones.spawns.rules;

import com.mojang.serialization.MapCodec;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import se.datasektionen.mc.zones.spawns.SpawnRuleRegistry;

public class Default implements SpawnRule {

	public static final Default INSTANCE = new Default();

	public static final MapCodec<Default> CODEC = MapCodec.unit(INSTANCE);

	@Override
	public SpawnRuleRegistry.SpawnRuleType<? extends SpawnRule> getType() {
		return SpawnRuleRegistry.DEFAULT;
	}

	@Override
	public boolean canSpawn(EntityType<?> entityType, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
		return SpawnRestriction.canSpawn(entityType, world, reason, pos, random);
	}
}
