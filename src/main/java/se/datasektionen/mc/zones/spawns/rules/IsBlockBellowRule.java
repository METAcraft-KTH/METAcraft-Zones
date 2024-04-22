package se.datasektionen.mc.zones.spawns.rules;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import se.datasektionen.mc.zones.spawns.SpawnRuleRegistry;

public class IsBlockBellowRule implements SpawnRule {

	public Either<Block, TagKey<Block>> blockBelow;

	public static final MapCodec<IsBlockBellowRule> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.either(
					Registries.BLOCK.getCodec(),
					TagKey.codec(Registries.BLOCK.getKey())
			).fieldOf("blockBelow").forGetter(rule -> rule.blockBelow)
	).apply(instance, IsBlockBellowRule::new));

	public IsBlockBellowRule(Either<Block, TagKey<Block>> blockBelow) {
		this.blockBelow = blockBelow;
	}

	@Override
	public SpawnRuleRegistry.SpawnRuleType<? extends IsBlockBellowRule> getType() {
		return SpawnRuleRegistry.BLOCK_BELOW;
	}

	@Override
	public boolean canSpawn(EntityType<?> entityType, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
		return blockBelow.map(block -> world.getBlockState(pos.down()).isOf(block), tag -> world.getBlockState(pos.down()).isIn(tag));
	}
}
