package se.datasektionen.mc.zones.spawns.rules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.biome.Biome;
import se.datasektionen.mc.zones.spawns.SpawnRuleRegistry;

import java.util.function.BiPredicate;

public class Weather implements SpawnRule {

	private final WeatherType weather;

	public static final Codec<Weather> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					WeatherType.CODEC.fieldOf("weather").forGetter(weather -> weather.weather)
			).apply(instance, Weather::new)
	);

	public Weather(WeatherType weather) {
		this.weather = weather;
	}

	@Override
	public SpawnRuleRegistry.SpawnRuleType<? extends SpawnRule> getType() {
		return SpawnRuleRegistry.WEATHER;
	}

	@Override
	public boolean canSpawn(EntityType<?> entityType, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
		return weather.isActive(world, pos);
	}

	public enum WeatherType implements StringIdentifiable {
		CLEAR("clear", (world, pos) ->
				!world.getLevelProperties().isRaining() ||
				world.getBiome(pos).value().getPrecipitation(pos) == Biome.Precipitation.NONE
		),
		RAIN("rain", (world, pos) ->
				world.getLevelProperties().isRaining() &&
				world.getBiome(pos).value().getPrecipitation(pos) == Biome.Precipitation.RAIN
		),
		SNOW("snow", (world, pos) ->
				world.getLevelProperties().isRaining() &&
				world.getBiome(pos).value().getPrecipitation(pos) == Biome.Precipitation.SNOW
		),
		THUNDER("thunder", (world, pos) ->
				world.getLevelProperties().isThundering() &&
				world.getBiome(pos).value().getPrecipitation(pos) == Biome.Precipitation.RAIN
		);

		public static final Codec<WeatherType> CODEC = StringIdentifiable.createCodec(WeatherType::values);

		private final String name;
		private final BiPredicate<ServerWorldAccess, BlockPos> isActive;

		WeatherType(String name, BiPredicate<ServerWorldAccess, BlockPos> isActive) {
			this.name = name;
			this.isActive = isActive;
		}

		@Override
		public String asString() {
			return name;
		}

		public boolean isActive(ServerWorldAccess world, BlockPos pos) {
			return isActive.test(world, pos);
		}
	}
}
