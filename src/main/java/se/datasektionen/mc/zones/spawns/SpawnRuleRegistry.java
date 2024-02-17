package se.datasektionen.mc.zones.spawns;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import se.datasektionen.mc.zones.METAcraftZones;
import se.datasektionen.mc.zones.spawns.rules.*;

public class SpawnRuleRegistry {

	public static final RegistryKey<Registry<SpawnRuleType<?>>> REGISTRY_KEY = RegistryKey.ofRegistry(METAcraftZones.getID("spawn_rule"));
	public static final Registry<SpawnRuleType<?>> REGISTRY = FabricRegistryBuilder.createSimple(REGISTRY_KEY).buildAndRegister();

	public static final SpawnRuleType<NotRule> NOT = register("not", NotRule.CODEC);
	public static final SpawnRuleType<AndRule> AND = register("and", AndRule.createCodec(AndRule::new));
	public static final SpawnRuleType<OrRule> OR = register("or", OrRule.createCodec(OrRule::new));
	public static final SpawnRuleType<LightLevelSpawnRule> LIGHT_LEVEL = register("light_level", LightLevelSpawnRule.CODEC);
	public static final SpawnRuleType<IsBlockBellowRule> BLOCK_BELOW = register("block_below", IsBlockBellowRule.CODEC);
	public static final SpawnRuleType<BlockBelowCanSpawn> BLOCK_BELOW_CAN_SPAWN = register("block_below_can_spawn", BlockBelowCanSpawn.CODEC);
	public static final SpawnRuleType<RNGRule> RANDOM = register("random", RNGRule.CODEC);
	public static final SpawnRuleType<Default> DEFAULT = register("default", Default.CODEC);
	public static final SpawnRuleType<Weather> WEATHER = register("weather", Weather.CODEC);
	public static final SpawnRuleType<HasSkyAccess> HAS_SKY_ACCESS = register("has_sky_access", HasSkyAccess.CODEC);



	public record SpawnRuleType<T extends SpawnRule>(Codec<T> codec) {}

	public static void init() {

	}

	public static <T extends SpawnRule> SpawnRuleType<T> register(String id, Codec<T> codec) {
		return Registry.register(REGISTRY, new Identifier(id), new SpawnRuleType<>(codec));
	}

}
