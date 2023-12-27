package se.datasektionen.mc.zones.spawns.rules;

import com.mojang.serialization.Codec;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import se.datasektionen.mc.zones.spawns.SpawnRuleRegistry;

public interface SpawnRule {

	Codec<SpawnRule> REGISTRY_CODEC = SpawnRuleRegistry.REGISTRY.getCodec().dispatch(
			SpawnRule::getType, SpawnRuleRegistry.SpawnRuleType::codec
	);

	SpawnRuleRegistry.SpawnRuleType<? extends SpawnRule> getType();

	boolean canSpawn(EntityType<?> entityType, WorldAccess world, SpawnReason reason, BlockPos pos, Random random);

}
